package com.ccclubs.ca.streaming.business.energy.jump;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import com.ccclubs.ca.bean.SocJump;
import com.ccclubs.ca.util.BizConstant;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.base.Function;
import org.apache.flink.shaded.guava18.com.google.common.collect.Ordering;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 实体类更换为SocJump
 */
public class SocJumpFunctionV1 {
    private static Logger logger = Logger.getLogger(SocJumpFunctionV1.class);

    public static DataStream<SocJump> Main(DataStream<CarState> dataStream) {

        DataStream<SocJump> socJumpDataStream = dataStream
                .filter(new SocJumpFilterFunction())
                .keyBy(new SocJumpKeyFunc())
                .window(TumblingEventTimeWindows.of(Time.minutes(10)))
                .process(new SocJumpGoogleSortedProcessFunc())
                .uid("apply-id");

        return socJumpDataStream;
    }
    static class SocJumpFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            Float oilCost = carState.getOilCost();
            return (oilCost == 0)||(oilCost == null);
        }
    }

    static class SocJumpKeyFunc implements KeySelector<CarState, String> {
        @Override
        public String getKey(CarState logSocJump){
            return logSocJump.getVin();
        }
    }

    static class SocJumpGoogleSortedProcessFunc extends ProcessWindowFunction<CarState, SocJump, String, TimeWindow> {
        private transient ValueState<CarState> jumpValueState;

        @Override
        public void open(Configuration parameters) {
            ValueStateDescriptor<CarState> descriptor =
                    new ValueStateDescriptor<CarState>(
                            "SocJump", // the state name
                            TypeInformation.of(new TypeHint<CarState>() {
                            })); // default value of the state, if nothing was set
            jumpValueState = getRuntimeContext().getState(descriptor);
        }

        @Override
        public void process(String key, Context context, Iterable<CarState> iterable, Collector<SocJump> collector) throws Exception {

            Ordering<CarState> jumpOrdering = Ordering.natural().onResultOf(new Function<CarState, Comparable>() {
                @Nullable
                @Override
                public Comparable apply(@Nullable CarState logSocJump) {
                    return logSocJump.getCurrentTime();
                }
            });
            List<CarState> logSocJumps = jumpOrdering.sortedCopy(iterable);

            long startMills = logSocJumps.get(0).getCurrentTime();
            for (CarState logSocJump:logSocJumps) {
                CarState value = jumpValueState.value();
                Float soc = logSocJump.getEvBattery();
                Long currentTime = logSocJump.getCurrentTime();

                if (value != null) {
                    Long valueTime = value.getCurrentTime();
                    if (valueTime - startMills > BizConstant.SOC_JUMP_TIME_OUT) {
                        jumpValueState.clear();
                    } else {
                        Float valueSoc = value.getEvBattery();
                        float changedSoc = Math.abs(valueSoc - soc);
                        if (changedSoc >= BizConstant.SOC_JUMP_THRESHOLD) {
                            SocJump jumpPoint = new SocJump();
                            jumpPoint.setDataType("SocJump");
                            jumpPoint.setVin(key);
                            jumpPoint.setTeNumber(logSocJump.getTeNumber());
                            jumpPoint.setBeforeSoc(valueSoc);
                            jumpPoint.setBeforeTime(valueTime);
                            jumpPoint.setAfterSoc(soc);
                            jumpPoint.setAfterTime(currentTime);
                            jumpPoint.setSpendTime(currentTime-valueTime);
                            jumpPoint.setChangeSoc(soc);
                            jumpPoint.setAccess(logSocJump.getAccess());

                            collector.collect(jumpPoint);
                        }
                        jumpValueState.clear();
                    }
                }
                jumpValueState.update(logSocJump);
            }
            logSocJumps.clear();
        }
    }
}

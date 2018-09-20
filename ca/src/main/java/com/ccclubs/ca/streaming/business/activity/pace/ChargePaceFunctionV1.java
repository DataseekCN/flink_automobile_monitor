package com.ccclubs.ca.streaming.business.activity.pace;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.log4j.Logger;

/**
 * Created by lcy on 2018/6/1.
 */
public class ChargePaceFunctionV1 {
    private static Logger logger = Logger.getLogger(ChargePaceFunctionV1.class);

    public static DataStream<Pace> Main(DataStream<CarState> dataStream) {

        KeyedStream<CarState, String> keyedStream = dataStream
                .filter(new ChargeFilterFunction())
                .keyBy(new ChargeKeyFunc());

        DataStream<Pace> chargePace = keyedStream
                .process(new PaceProcessFunction("ChargePace"));

        return chargePace;
    }

    static class ChargeFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            Integer chargeStatus = carState.getChargingStatus();
            return chargeStatus > 0;
        }
    }

    static class ChargeKeyFunc implements KeySelector<CarState, String> {
        @Override
        public String getKey(CarState carState) throws Exception {
            return carState.getVin();
        }
    }
}


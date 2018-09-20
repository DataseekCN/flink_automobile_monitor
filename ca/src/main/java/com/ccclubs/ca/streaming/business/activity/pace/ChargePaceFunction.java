package com.ccclubs.ca.streaming.business.activity.pace;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import com.ccclubs.ca.util.BizConstant;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Created by lcy on 2018/6/1.
 */
public class ChargePaceFunction {
    private static Logger logger = Logger.getLogger(ChargePaceFunction.class);

    public static DataStream<Pace> Main(DataStream<CarState> dataStream) {

        KeyedStream<CarState, String> keyedStream = dataStream
                .filter(new ChargeFilterFunction())
                .keyBy(new ChargeKeyFunc());

        DataStream<Pace> chargePace = keyedStream
                .window(EventTimeSessionWindows.withGap(Time.seconds(BizConstant.CHARGE_PACE_THRESHOLD)))
                .process(new ChargeProcessFunc());

        return chargePace;
    }

    static class ChargeFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            if (carState != null) {
                Integer chargeStatus = carState.getChargingStatus();
                return chargeStatus > 0;
            } else {
                return false;
            }
        }
    }

    static class ChargeKeyFunc implements KeySelector<CarState, String> {
        @Override
        public String getKey(CarState carState) throws Exception {
            return carState.getVin();
        }
    }

    static class ChargeProcessFunc extends ProcessWindowFunction<CarState, Pace, String, TimeWindow> {
        @Override
        public void process(String key, Context context, Iterable<CarState> iterable, Collector<Pace> collector) throws Exception {
            CarState minState = null;
            CarState maxState = null;
            long minMills = 0l;
            long maxMills = 0l;
            for (Iterator<CarState> iterator = iterable.iterator(); iterator.hasNext(); ) {
                CarState event = iterator.next();
                Long currentTime = event.getCurrentTime();
                if (minMills == 0) {
                    minMills = currentTime;
                    minState = event;
                } else {
                    minMills = Math.min(minMills, currentTime);
                    if (minMills == currentTime) {
                        minState = event;
                    }
                }
                maxMills = Math.max(maxMills, currentTime);
                if (maxMills == currentTime) {
                    maxState = event;
                }
            }

            Pace chargePace = new Pace();
            chargePace.setDataType("ChargePace");
            chargePace.setVin(key);

            chargePace.setStartTime(minMills);
            chargePace.setEndTime(maxMills);
            chargePace.setSpendTime(maxMills - minMills);

            Float startSoc = minState.getEvBattery();
            Float endSoc = maxState.getEvBattery();
            chargePace.setStartSoc(startSoc);
            chargePace.setEndSoc(endSoc);
            if (startSoc != null || endSoc != null) {
                chargePace.setChangeSoc(Math.abs(endSoc - startSoc));
            }

            Float startObdMiles = minState.getObdMiles();
            Float endObdMiles = maxState.getObdMiles();
            chargePace.setStartObdMile(startObdMiles);
            chargePace.setEndObdMile(endObdMiles);
            if (endObdMiles != null || startObdMiles != null) {
                chargePace.setChangeObdMile(endObdMiles - startObdMiles);
            }
            Float startOilCost = minState.getOilCost();
            Float endOilCost = maxState.getOilCost();
            chargePace.setStartOil(startOilCost);
            chargePace.setEndOil(endOilCost);
            if (startOilCost != null || endOilCost != null) {
                chargePace.setChangeOil(Math.abs(endOilCost - startOilCost));
            }

            Double startLatitude = minState.getLatitude();
            Double endLatitude = maxState.getLatitude();
            Double startLongitude = minState.getLongitude();
            Double endLongitude = maxState.getLongitude();
            chargePace.setStartLatitude(startLatitude);
            chargePace.setEndLatitude(endLatitude);
            chargePace.setStartLongitude(startLongitude);
            chargePace.setEndLongitude(endLongitude);

            String startGeoHash = minState.getGeoHash();
            String endGeoHash = maxState.getGeoHash();
            chargePace.setStartGeoHash(startGeoHash);
            chargePace.setEndGeoHash(endGeoHash);

            if (chargePace.getSpendTime() > BizConstant.DISCARD_PACE) {
                collector.collect(chargePace);
            }
        }
    }
}

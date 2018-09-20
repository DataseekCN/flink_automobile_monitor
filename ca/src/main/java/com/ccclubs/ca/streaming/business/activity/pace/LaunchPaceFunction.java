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

public class LaunchPaceFunction {
    private static Logger logger = Logger.getLogger(LaunchPaceFunction.class);

    public static DataStream<Pace> Main(DataStream<CarState> dataStream) {

        KeyedStream<CarState, String> keyedStream = dataStream
                .filter(new LaunchFilterFunction())
                .keyBy(new LaunchKeyFunc());

        DataStream<Pace> launchPace = keyedStream
                .window(EventTimeSessionWindows.withGap(Time.minutes(BizConstant.LAUNCH_PACE_THRESHOLD)))
                .process(new LaunchProcessFunc());

        return launchPace;
    }

    static class LaunchFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            if (carState != null) {
                Integer engine = carState.getEngineStatus();
                return (engine == 1) || (engine == 3);
            } else {
                return false;
            }

        }
    }

    static class LaunchKeyFunc implements KeySelector<CarState, String> {
        @Override
        public String getKey(CarState carState) throws Exception {
            return carState.getVin();
        }
    }

    static class LaunchProcessFunc extends ProcessWindowFunction<CarState, Pace, String, TimeWindow> {

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

            Pace launchPace = new Pace();
            launchPace.setDataType("LaunchPace");
            launchPace.setVin(key);
            launchPace.setStartTime(minMills);
            launchPace.setEndTime(maxMills);
            launchPace.setSpendTime(maxMills - minMills);
            Float startSoc = minState.getEvBattery();
            Float endSoc = maxState.getEvBattery();
            launchPace.setStartSoc(startSoc);
            launchPace.setEndSoc(endSoc);
            if (startSoc != null || endSoc != null) {
                launchPace.setChangeSoc(Math.abs(endSoc - startSoc));
            }
            Float startObdMiles = minState.getObdMiles();
            Float endObdMiles = maxState.getObdMiles();
            launchPace.setStartObdMile(startObdMiles);
            launchPace.setEndObdMile(endObdMiles);
            if (startObdMiles != null || endObdMiles != null) {
                launchPace.setChangeObdMile(endObdMiles - startObdMiles);
            }
            Float startOilCost = minState.getOilCost();
            Float endOilCost = maxState.getOilCost();
            launchPace.setStartOil(startOilCost);
            launchPace.setEndOil(endOilCost);
            if (startOilCost != null || endOilCost != null) {
                launchPace.setChangeOil(Math.abs(endOilCost - startOilCost));
            }

            Double startLatitude = minState.getLatitude();
            Double endLatitude = maxState.getLatitude();
            Double startLongitude = minState.getLongitude();
            Double endLongitude = maxState.getLongitude();
            launchPace.setStartLatitude(startLatitude);
            launchPace.setEndLatitude(endLatitude);
            launchPace.setStartLongitude(startLongitude);
            launchPace.setEndLongitude(endLongitude);

            String startGeoHash = minState.getGeoHash();
            String endGeoHash = maxState.getGeoHash();
            launchPace.setStartGeoHash(startGeoHash);
            launchPace.setEndGeoHash(endGeoHash);

            if (launchPace.getSpendTime() > BizConstant.DISCARD_PACE) {
                collector.collect(launchPace);
            }
        }
    }

}

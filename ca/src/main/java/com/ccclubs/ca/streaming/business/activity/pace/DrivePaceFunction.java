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

public class DrivePaceFunction {
    private static Logger logger = Logger.getLogger(DrivePaceFunction.class);

    public static DataStream<Pace> Main(DataStream<CarState> dataStream) {

        KeyedStream<CarState, String> keyedStream = dataStream
                .filter(new LaunchFilterFunction())
                .keyBy(new LaunchKeyFunc());

        DataStream<Pace> drivePace = keyedStream
                .window(EventTimeSessionWindows.withGap(Time.minutes(BizConstant.DRIVE_PACE_THRESHOLD)))
                .process(new DriveProcessFunc());
        return drivePace;
    }

    static class LaunchFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            if (carState != null) {
                Integer engine = carState.getEngineStatus();
                Float speed = carState.getSpeed();
                return (engine == 1) && (speed > 0);
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

    static class DriveProcessFunc extends ProcessWindowFunction<CarState, Pace, String, TimeWindow> {
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

            Pace drivePace = new Pace();
            drivePace.setDataType("DrivePace");
            drivePace.setVin(key);
            drivePace.setStartTime(minMills);
            drivePace.setEndTime(maxMills);
            drivePace.setSpendTime(maxMills - minMills);
            Float startSoc = minState.getEvBattery();
            Float endSoc = maxState.getEvBattery();
            drivePace.setStartSoc(startSoc);
            drivePace.setEndSoc(endSoc);
            if (startSoc != null || endSoc != null) {
                drivePace.setChangeSoc(Math.abs(endSoc - startSoc));
            }
            Float startObdMiles = minState.getObdMiles();
            Float endObdMiles = maxState.getObdMiles();
            drivePace.setStartObdMile(startObdMiles);
            drivePace.setEndObdMile(endObdMiles);
            if (endObdMiles != null || startObdMiles != null) {
                drivePace.setChangeObdMile(endObdMiles - startObdMiles);
            }
            Float startOilCost = minState.getOilCost();
            Float endOilCost = maxState.getOilCost();
            drivePace.setStartOil(startOilCost);
            drivePace.setEndOil(endOilCost);
            if (startOilCost != null || endOilCost != null) {
                drivePace.setChangeOil(Math.abs(endOilCost - startOilCost));
            }

            Double startLatitude = minState.getLatitude();
            Double endLatitude = maxState.getLatitude();
            Double startLongitude = minState.getLongitude();
            Double endLongitude = maxState.getLongitude();
            drivePace.setStartLatitude(startLatitude);
            drivePace.setEndLatitude(endLatitude);
            drivePace.setStartLongitude(startLongitude);
            drivePace.setEndLongitude(endLongitude);

            String startGeoHash = minState.getGeoHash();
            String endGeoHash = maxState.getGeoHash();
            drivePace.setStartGeoHash(startGeoHash);
            drivePace.setEndGeoHash(endGeoHash);

            if (drivePace.getSpendTime() > BizConstant.DISCARD_PACE) {
                collector.collect(drivePace);
            }
        }
    }

}

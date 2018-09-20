package com.ccclubs.ca.streaming.business.activity.pace;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.log4j.Logger;

public class DrivePaceFunctionV1 {
    private static Logger logger = Logger.getLogger(DrivePaceFunctionV1.class);

    public static DataStream<Pace> Main(DataStream<CarState> dataStream) {

        KeyedStream<CarState, String> keyedStream = dataStream
                .filter(new LaunchFilterFunction())
                .keyBy(new LaunchKeyFunc());

        DataStream<Pace> pace = keyedStream
                .process(new PaceProcessFunction("DrivePace"));

        return pace;
    }

    static class LaunchFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            Integer engine = carState.getEngineStatus();
            Float speed = carState.getSpeed();
            return (engine == 1) && (speed > 0);
        }
    }

    static class LaunchKeyFunc implements KeySelector<CarState, String> {
        @Override
        public String getKey(CarState carState) throws Exception {
            return carState.getVin();
        }
    }
}

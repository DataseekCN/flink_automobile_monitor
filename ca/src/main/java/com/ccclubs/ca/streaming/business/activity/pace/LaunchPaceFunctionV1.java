package com.ccclubs.ca.streaming.business.activity.pace;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.log4j.Logger;

public class LaunchPaceFunctionV1 {
    private static Logger logger = Logger.getLogger(LaunchPaceFunctionV1.class);

    public static DataStream<Pace> Main(DataStream<CarState> dataStream) {

        KeyedStream<CarState, String> keyedStream = dataStream
                .filter(new LaunchFilterFunction())
                .keyBy(new LaunchKeyFunc());

        DataStream<Pace> launchPace = keyedStream
                .process(new PaceProcessFunction("LaunchPace"));

        return launchPace;
    }

    static class LaunchFilterFunction implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            Integer engine = carState.getEngineStatus();
            return (engine == 1) || (engine == 3);
        }
    }

    static class LaunchKeyFunc implements KeySelector<CarState, String> {
        @Override
        public String getKey(CarState carState) throws Exception {
            return carState.getVin();
        }
    }

}

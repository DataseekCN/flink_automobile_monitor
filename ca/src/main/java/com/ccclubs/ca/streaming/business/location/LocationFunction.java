package com.ccclubs.ca.streaming.business.location;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.streaming.util.FlinkUtil;
import com.ccclubs.common.util.GPSUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;

/**
 * Created by taosm on 2018/5/31.
 */
public class LocationFunction {
    private static Logger logger = Logger.getLogger(LocationFunction.class);
    public static DataStream<Row> Main(DataStream<CarState> carStateDataStream,StreamTableEnvironment tableEnv) {

        DataStream<CarState> filterStream = carStateDataStream
                .filter(new LocationFilterFunction())
                .assignTimestampsAndWatermarks(new LocationAssignerWithPeriodicWatermarks());
        tableEnv.registerFunction("modifyDateTimeToGMS8", new FlinkUtil.modifyDateTimeToGMS8());
        tableEnv.registerDataStream("tbl_car_state"
                ,filterStream
                ,"vin,longitude,latitude,geoHash,currentTime as machineTime,currentTime.rowtime");
        Table result = tableEnv.sqlQuery(" " +
                " select\n" +
                " geoHash,\n" +
                " modifyDateTimeToGMS8(TUMBLE_START(currentTime, INTERVAL '30' SECOND)) as wStart " +
                " \n" +
                " from tbl_car_state " +
                " group by TUMBLE(currentTime, INTERVAL '30' SECOND), geoHash \n" +
                " ");

        DataStream<Row> retractStream = tableEnv.toAppendStream(result, Row.class);
        return retractStream;
    }

    static class LocationFilterFunction implements FilterFunction<CarState>{
        @Override
        public boolean filter(CarState carState) throws Exception {
            boolean allexp = false;
            try {
                Integer engineStatus = carState.getEngineStatus();
                Double longitude = carState.getLongitude();
                Double latitude = carState.getLatitude();
                Integer gpsValid = carState.getGpsValid();
                double[] location = GPSUtils.gcj02_To_Gps84(latitude, longitude);
                Double normal_latitude = location[0];
                Double normal_longitude = location[1];
                boolean exp1 = (engineStatus == 1 || engineStatus == 3);
                boolean exp2 = gpsValid == 1;
                boolean exp3 = GPSUtils.outOfChina(normal_latitude, normal_longitude) == false;
                allexp = exp1 && exp2 && exp3;
            }
            catch (Exception ex){
                //ex.printStackTrace();
            }
            return allexp;
        }
    }

    static class LocationAssignerWithPeriodicWatermarks implements AssignerWithPeriodicWatermarks<CarState> {
        private final long maxOutOfOrderness = 5000; // 5 seconds
        private long currentMaxTimestamp;
        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimestamp-maxOutOfOrderness);
        }

    public long extractTimestamp(CarState carState, long l) {
        long timestamp = carState.getCurrentTime();
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
        return timestamp;
    }
}
}

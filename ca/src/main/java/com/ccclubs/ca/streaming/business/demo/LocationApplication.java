package com.ccclubs.ca.streaming.business.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.streaming.util.FlinkUtil;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.GPSUtils;
import com.ccclubs.common.util.GeohashUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Created by taosm on 2018/5/31.
 */
public class LocationApplication {
    private static Logger logger = Logger.getLogger(LocationApplication.class);

    public static void main(String[] args) throws Exception {
        System.setProperty("user.timezone","Asia/Shanghai");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(60000);

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BizConstant.ACCEPT_BROKER);
        consumerProps.setProperty("zookeeper.connect", BizConstant.ACCEPT_ZK);
        consumerProps.setProperty("enable.auto.commit", "true");
        consumerProps.setProperty("group.id", "tao5");
        consumerProps.setProperty("auto.offset.reset","latest");

        FlinkKafkaConsumer011<String> myConsumer =
                new FlinkKafkaConsumer011<String>(java.util.regex.Pattern.compile(BizConstant.ACCEPT_TOPIC),new SimpleStringSchema(),consumerProps);

        DataStream<String> originStream = env.addSource(myConsumer);
        DataStream<CarState> carStateDataStream = originStream.map(new MapFunction<String, CarState>() {
            @Override
            public CarState map(String line) throws Exception {
                JSONObject jsonObject = null;
                CarState carState = null;
                try{
                    jsonObject= JSON.parseObject(line);
                    carState=new CarState();
                    String cssVin = jsonObject.getString("cssVin");
                    Long cssCurrentTime = jsonObject.getLong("cssCurrentTime");
                    Integer cssEngine = jsonObject.getInteger("cssEngine");
                    Double cssLongitude = jsonObject.getDouble("cssLongitude");
                    Double cssLatitude = jsonObject.getDouble("cssLatitude");
                    Integer cssGpsValid = jsonObject.getInteger("cssGpsValid");
                    String geoHash = null;
                    if (cssLatitude!=null && cssLongitude!=null && cssLatitude > 0 && cssLongitude > 0) {
                        geoHash = GeohashUtil.getGeohashCode(cssLatitude, cssLongitude, 6);
                    }
                    carState.setVin(cssVin);
                    carState.setCurrentTime(cssCurrentTime);
                    carState.setEngineStatus(cssEngine);
                    carState.setLongitude(cssLongitude);
                    carState.setLatitude(cssLatitude);
                    carState.setGpsValid(cssGpsValid);
                    carState.setGeoHash(geoHash);
                }
                catch (Exception ex){
                    logger.error(ex.getMessage());
                }
                return carState;
            }
        });

        DataStream<CarState> filterStream = carStateDataStream.filter(new FilterFunction<CarState>() {
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
        }).assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks<CarState>() {
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
        });
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
        retractStream.print();
        env.execute("run map");
    }
}

package com.ccclubs.ca.streaming.appgroup;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import com.ccclubs.ca.streaming.business.activity.pace.ChargePaceFunction;
import com.ccclubs.ca.streaming.business.activity.pace.DrivePaceFunction;
import com.ccclubs.ca.streaming.business.activity.pace.LaunchPaceFunction;
import com.ccclubs.ca.streaming.business.energy.jump.SocJumpFunction;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.GeohashUtil;
import com.ccclubs.common.util.PropertiesHelper;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Properties;

/**
 * Created by lcy on 2018/5/31.
 */
public class MonitorApp {
    private static Logger logger = Logger.getLogger(MonitorApp.class);
    public static void main(String[] args) throws Exception {
        Logger.getLogger("org").setLevel(Level.ERROR);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //检查点
        env.enableCheckpointing(60000L);

        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", propertiesHelper.getValue(BizConstant.ACCEPT_BROKER));
        consumerProps.setProperty("zookeeper.connect", propertiesHelper.getValue(BizConstant.ACCEPT_ZK));
        consumerProps.setProperty("enable.auto.commit", "true");
        consumerProps.setProperty("group.id", propertiesHelper.getValue(BizConstant.BUSINESS_GROUP_ID));

        FlinkKafkaConsumer011<String> myConsumer =
                new FlinkKafkaConsumer011<String>(java.util.regex.Pattern.compile(propertiesHelper.getValue(BizConstant.ACCEPT_TOPIC)), new SimpleStringSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<Pace> myProducer =
                new FlinkKafkaProducer011<Pace>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.SEND_DEFAULT_TOPIC, new MonitorKeyedSchema());

        DataStream<String> streamSource = env.addSource(myConsumer);
        DataStream<CarState> stateStream = streamSource
                .map(new MonitorMapFunction())
                .assignTimestampsAndWatermarks(new PaceBoundedLatenessWatermarkAssigner());

        LaunchPaceFunction launchPaceFunction = new LaunchPaceFunction();
        DrivePaceFunction drivePaceFunction = new DrivePaceFunction();
        ChargePaceFunction chargePaceFunction = new ChargePaceFunction();
        SocJumpFunction socJumpFunction = new SocJumpFunction();

        DataStream<Pace> launchMainStream = launchPaceFunction.Main(stateStream);
        DataStream<Pace> driveMainStream = drivePaceFunction.Main(stateStream);
        DataStream<Pace> chargeMainStream = chargePaceFunction.Main(stateStream);
        DataStream<Pace> socJumpMain = socJumpFunction.Main(stateStream);


        DataStream<Pace> unionStream = launchMainStream.union(driveMainStream, chargeMainStream, socJumpMain);

        unionStream.addSink(myProducer);
        env.execute("business stream");
    }

    static class PaceBoundedLatenessWatermarkAssigner implements AssignerWithPeriodicWatermarks<CarState> {
        private long currentMaxTimeMills = 0l;
        private long maxOutOfOrderness = BizConstant.MAX_OUT_OF_ORDERNESS;

        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimeMills - maxOutOfOrderness);
        }

        @Override
        public long extractTimestamp(CarState message, long l) {
            Long timeMills = message.getCurrentTime();
            currentMaxTimeMills = Math.max(timeMills, currentMaxTimeMills);
            return timeMills;
        }
    }

    static class MonitorKeyedSchema implements KeyedSerializationSchema<Pace> {

        @Override
        public byte[] serializeKey(Pace pace) {
            byte[] retBytes = new byte[0];
            if (pace != null) {
                retBytes = pace.getVin().getBytes();
            }
            return retBytes;
        }

        @Override
        public byte[] serializeValue(Pace pace) {
            byte[] retBytes = new byte[0];
            if (pace != null) {
                retBytes = JSON.toJSONString(pace).getBytes();
            }
            return retBytes;
        }

        @Override
        public String getTargetTopic(Pace pace) {
            if (pace.getDataType().contains("Pace")) {
                return BizConstant.ACTIVITY_PACE_TOPIC;
            } else if (pace.getDataType().contains("Soc")) {
                return BizConstant.ENERGY_JUMP_TOPIC;
            } else {
                return BizConstant.SEND_DEFAULT_TOPIC;
            }
        }
    }


    static class MonitorMapFunction implements MapFunction<String, CarState>{

        @Override
        public CarState map(String line) throws Exception {
            CarState logState = null;
            try {
                JSONObject jsonObject = JSON.parseObject(line);
                String vin = jsonObject.getString("cssVin");
                String teNumber = jsonObject.getString("cssNumber");
                Long currentTime = jsonObject.getLong("cssCurrentTime");
                Integer engineStatus = jsonObject.getInteger("cssEngine");
                Integer chargingStatus = jsonObject.getInteger("cssCharging");
                Double longitude = jsonObject.getDouble("cssLongitude");
                Double latitude = jsonObject.getDouble("cssLatitude");
                Float soc = jsonObject.getFloat("cssEvBattery");
                Float speed = jsonObject.getFloat("cssSpeed");
                Float obdMiles = jsonObject.getFloat("cssObdMile");
                Float oilCost = jsonObject.getFloat("cssOil");
                Integer access = jsonObject.getInteger("cssAccess");
                Float powerReserve = jsonObject.getFloat("cssPower");
                String curOrder = jsonObject.getString("cssOrder");
                String geoHash = null;
                if (latitude!=null && longitude!=null && latitude > 0 && longitude > 0) {
                    geoHash = GeohashUtil.getGeohashCode(latitude, longitude, 7);
                }
                logState = new CarState();
                logState.setVin(vin);
                logState.setTeNumber(teNumber);
                logState.setCurrentTime(currentTime);
                logState.setEngineStatus(engineStatus);
                logState.setChargingStatus(chargingStatus);
                logState.setLongitude(longitude);
                logState.setLatitude(latitude);
                logState.setEvBattery(soc);
                logState.setSpeed(speed);
                logState.setObdMiles(obdMiles);
                logState.setOilCost(oilCost);
                logState.setAccess(access);
                logState.setPowerReserve(powerReserve);
                logState.setCurOrder(curOrder);
                logState.setGeoHash(geoHash);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return logState;
        }
        }


}

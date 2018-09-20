package com.ccclubs.ca.streaming.appgroup;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import com.ccclubs.ca.bean.SocJump;
import com.ccclubs.ca.streaming.business.activity.pace.*;
import com.ccclubs.ca.streaming.business.energy.jump.SocJumpFunction;
import com.ccclubs.ca.streaming.business.energy.jump.SocJumpFunctionV1;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.GeohashUtil;
import com.ccclubs.common.util.PropertiesHelper;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by lcy on 2018/5/31.
 */
public class MonitorAppV1 {
    public static void main(String[] args) throws Exception {
        Logger.getLogger("org").setLevel(Level.ERROR);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //检查点
        env.enableCheckpointing(60000L);

        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", propertiesHelper.getValue(BizConstant.ACCEPT_BROKER));
        consumerProps.setProperty("enable.auto.commit", "true");
        consumerProps.setProperty("group.id", propertiesHelper.getValue(BizConstant.BUSINESS_GROUP_ID));

        FlinkKafkaConsumer011<CarState> myConsumer =
                new FlinkKafkaConsumer011<CarState>(java.util.regex.Pattern.compile(propertiesHelper.getValue(BizConstant.ACCEPT_TOPIC)), new MonitorLogDeserializationSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<Pace> paceProducer =
                new FlinkKafkaProducer011<Pace>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.SEND_DEFAULT_TOPIC, new MonitorKeyedSchema());
        FlinkKafkaProducer011<SocJump> energyProducer =
                new FlinkKafkaProducer011<SocJump>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.SEND_DEFAULT_TOPIC, new MonitorEnergyKeyedSchema());

        DataStream<CarState> streamSource = env.addSource(myConsumer);
        DataStream<CarState> stateStream = streamSource
                .filter(new PaceFilter())
                .assignTimestampsAndWatermarks(new PaceBoundedLatenessWatermarkAssigner());

        LaunchPaceFunctionV1 launchPaceFunction = new LaunchPaceFunctionV1();
        DrivePaceFunctionV1 drivePaceFunction = new DrivePaceFunctionV1();
        ChargePaceFunctionV1 chargePaceFunction = new ChargePaceFunctionV1();
        SocJumpFunctionV1 socJumpFunction = new SocJumpFunctionV1();

        DataStream<Pace> launchMainStream = launchPaceFunction.Main(stateStream);
        DataStream<Pace> driveMainStream = drivePaceFunction.Main(stateStream);
        DataStream<Pace> chargeMainStream = chargePaceFunction.Main(stateStream);
        DataStream<SocJump> socJumpMain = socJumpFunction.Main(stateStream);

        DataStream<Pace> unionStream = launchMainStream.union(driveMainStream, chargeMainStream);

        unionStream.addSink(paceProducer);
        socJumpMain.addSink(energyProducer);
        env.execute("business stream");
    }

    static class PaceFilter implements FilterFunction<CarState> {
        @Override
        public boolean filter(CarState carState) throws Exception {
            Integer engine = carState.getEngineStatus();
            Float speed = carState.getSpeed();
            Integer chargeStatus = carState.getChargingStatus();
            return (engine == 1) || (engine == 3) || (speed > 0) || (chargeStatus > 0);
        }
    }

    static class PaceBoundedLatenessWatermarkAssigner implements AssignerWithPeriodicWatermarks<CarState> {
        private long currentMaxTimeMills = -1L;
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
            } else {
                return BizConstant.SEND_DEFAULT_TOPIC;
            }
        }
    }

    static class MonitorEnergyKeyedSchema implements KeyedSerializationSchema<SocJump> {

        @Override
        public byte[] serializeKey(SocJump socJump) {
            byte[] retBytes = new byte[0];
            if (socJump != null) {
                retBytes = socJump.getVin().getBytes();
            }
            return retBytes;
        }

        @Override
        public byte[] serializeValue(SocJump socJump) {
            byte[] retBytes = new byte[0];
            if (socJump != null) {
                retBytes = JSON.toJSONString(socJump).getBytes();
            }
            return retBytes;
        }

        @Override
        public String getTargetTopic(SocJump socJump) {
            if (socJump.getDataType().contains("Soc")) {
                return BizConstant.ENERGY_JUMP_TOPIC;
            } else {
                return BizConstant.SEND_DEFAULT_TOPIC;
            }
        }
    }

    static class MonitorLogDeserializationSchema implements KeyedDeserializationSchema<CarState> {

        @Override
        public CarState deserialize(byte[] messageKey, byte[] message, String topic, int partition, long offset) throws IOException {
            CarState logState = null;
            try {
                String message_str = new String(message);
                JSONObject jsonObject = JSON.parseObject(message_str);
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
                if (latitude != null && longitude != null && latitude > 0 && longitude > 0) {
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

        @Override
        public boolean isEndOfStream(CarState carState) {
            return false;
        }

        @Override
        public TypeInformation<CarState> getProducedType() {
            return TypeInformation.of(CarState.class);
        }
    }


}

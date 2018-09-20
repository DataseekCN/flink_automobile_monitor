package com.ccclubs.ca.streaming.appgroup;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.base.BoundedLatenessWatermarkAssigner;
import com.ccclubs.ca.base.CarStateDeserializationSchema;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import com.ccclubs.ca.bean.SocJump;
import com.ccclubs.ca.streaming.business.activity.pace.ChargePaceFunctionV1;
import com.ccclubs.ca.streaming.business.activity.pace.DrivePaceFunctionV1;
import com.ccclubs.ca.streaming.business.activity.pace.LaunchPaceFunctionV1;
import com.ccclubs.ca.streaming.business.energy.jump.SocJumpFunctionV1;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.GeohashUtil;
import com.ccclubs.common.util.PropertiesHelper;
import org.apache.flink.api.common.functions.FilterFunction;
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
public class ActivityApp {
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
                new FlinkKafkaConsumer011<CarState>(java.util.regex.Pattern.compile(propertiesHelper.getValue(BizConstant.ACCEPT_TOPIC)), new CarStateDeserializationSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<Pace> paceProducer =
                new FlinkKafkaProducer011<Pace>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.SEND_DEFAULT_TOPIC, new MonitorKeyedSchema());

        DataStream<CarState> streamSource = env.addSource(myConsumer);
        DataStream<CarState> stateStream = streamSource
                .filter(new PaceFilter())
                .assignTimestampsAndWatermarks(new BoundedLatenessWatermarkAssigner());

        LaunchPaceFunctionV1 launchPaceFunction = new LaunchPaceFunctionV1();
        DrivePaceFunctionV1 drivePaceFunction = new DrivePaceFunctionV1();
        ChargePaceFunctionV1 chargePaceFunction = new ChargePaceFunctionV1();

        DataStream<Pace> launchMainStream = launchPaceFunction.Main(stateStream);
        DataStream<Pace> driveMainStream = drivePaceFunction.Main(stateStream);
        DataStream<Pace> chargeMainStream = chargePaceFunction.Main(stateStream);

        DataStream<Pace> unionStream = launchMainStream.union(driveMainStream, chargeMainStream);

        unionStream.addSink(paceProducer);
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

}

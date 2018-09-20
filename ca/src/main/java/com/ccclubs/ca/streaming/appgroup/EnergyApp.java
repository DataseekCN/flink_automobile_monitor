package com.ccclubs.ca.streaming.appgroup;

import com.alibaba.fastjson.JSON;
import com.ccclubs.ca.base.BoundedLatenessWatermarkAssigner;
import com.ccclubs.ca.base.CarStateDeserializationSchema;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.SocJump;
import com.ccclubs.ca.streaming.business.energy.jump.SocJumpFunctionV1;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.PropertiesHelper;

import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Properties;


public class EnergyApp {
    private static Logger logger = Logger.getLogger(EnergyApp.class);

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
        consumerProps.setProperty("group.id", propertiesHelper.getValue(BizConstant.ENERGY_GROUP_ID));

        FlinkKafkaConsumer011<CarState> myConsumer =
                new FlinkKafkaConsumer011<CarState>(java.util.regex.Pattern.compile(propertiesHelper.getValue(BizConstant.ACCEPT_TOPIC)), new CarStateDeserializationSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<SocJump> energyProducer =
                new FlinkKafkaProducer011<SocJump>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.SEND_DEFAULT_TOPIC, new MonitorEnergyKeyedSchema());

        DataStream<CarState> streamSource = env.addSource(myConsumer);
        DataStream<CarState> stateStream = streamSource
                .assignTimestampsAndWatermarks(new BoundedLatenessWatermarkAssigner());

        SocJumpFunctionV1 socJumpFunction = new SocJumpFunctionV1();
        DataStream<SocJump> socJumpMain = socJumpFunction.Main(stateStream);

        socJumpMain.addSink(energyProducer);
        env.execute("SocJump");
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
}

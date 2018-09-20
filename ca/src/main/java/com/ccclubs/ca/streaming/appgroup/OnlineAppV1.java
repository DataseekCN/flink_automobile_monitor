package com.ccclubs.ca.streaming.appgroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.*;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.GeohashUtil;
import com.ccclubs.common.util.PropertiesHelper;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.Collector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class OnlineAppV1 {
    private static Logger logger = Logger.getLogger(OnlineAppV1.class);

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
        consumerProps.setProperty("group.id", propertiesHelper.getValue(BizConstant.NETWORK_GROUP_ID));

        FlinkKafkaConsumer011<OnlineStateEventDTO> myConsumer =
                new FlinkKafkaConsumer011<OnlineStateEventDTO>("receive", new OnlineStateDeserializationSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<String> myProducer =
                new FlinkKafkaProducer011<String>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.TELECOM_NETWORK_TOPIC, new SimpleStringSchema());

        DataStream<OnlineStateEventDTO> streamSource = env.addSource(myConsumer);
        DataStream<String> streamOperator = streamSource
                .assignTimestampsAndWatermarks(new OnlineBoundedLatenessWatermarkAssigner())
                .keyBy(new OnlineKeyFunc())
                .process(new OnlineFunction());

        streamOperator.addSink(myProducer);
        env.execute("online pace");
    }

    static class OnlineStateDeserializationSchema implements KeyedDeserializationSchema<OnlineStateEventDTO> {

        @Override
        public OnlineStateEventDTO deserialize(byte[] messageKey, byte[] message, String topic, int partition, long offset) throws IOException {
            OnlineStateEventDTO onlineStateEventDTO = null;
            try {
                String message_str = new String(message);
                JSONObject jsonObject = JSON.parseObject(message_str);
                String vin = jsonObject.getString("vin");
                String simNo = jsonObject.getString("simNo");
                String teNumber = jsonObject.getString("teNumber");
                //服务端Ip
                String serverIp = jsonObject.getString("serverIp");
                Boolean onlineStatus = jsonObject.getBoolean("online");
                Long timestamp = jsonObject.getLong("timestamp");
                //网关类型
                String gatewayType = jsonObject.getString("gatewayType");
                //掉线类型
                Integer offlineType = jsonObject.getInteger("offlineType");
                Short access = jsonObject.getShort("access");
                //终端序列号
                String teNo = jsonObject.getString("teNo");
                //终端批次
                String batchNo = jsonObject.getString("batchNo");
                //终端类型
                Byte teType = jsonObject.getByte("teType");
                //终端型号
                String teModelNo = jsonObject.getString("teModelNo");
                //车型
                Integer carModel = jsonObject.getInteger("carModel");

                String iccid = jsonObject.getString("iccid");

                JSONArray states = jsonObject.getJSONArray("states");

                onlineStateEventDTO = new OnlineStateEventDTO();
                onlineStateEventDTO.setVin(vin);
                onlineStateEventDTO.setSimNo(simNo);
                onlineStateEventDTO.setTeNumber(teNumber);
                onlineStateEventDTO.setServerIp(serverIp);
                onlineStateEventDTO.setOnline(onlineStatus);
                onlineStateEventDTO.setTimestamp(timestamp);
                onlineStateEventDTO.setGatewayType(gatewayType);
                onlineStateEventDTO.setOfflineType(offlineType);
                onlineStateEventDTO.setAccess(access);
                onlineStateEventDTO.setTeNo(teNo);
                onlineStateEventDTO.setBatchNo(batchNo);
                onlineStateEventDTO.setTeType(teType);
                onlineStateEventDTO.setTeModelNo(teModelNo);
                onlineStateEventDTO.setCarModel(carModel);
                onlineStateEventDTO.setIccid(iccid);
                onlineStateEventDTO.setStates(states);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return onlineStateEventDTO;
        }

        @Override
        public boolean isEndOfStream(OnlineStateEventDTO onlineStateEventDTO) {
            return false;
        }

        @Override
        public TypeInformation<OnlineStateEventDTO> getProducedType() {
            return TypeInformation.of(OnlineStateEventDTO.class);
        }
    }

    static class OnlineBoundedLatenessWatermarkAssigner implements AssignerWithPeriodicWatermarks<OnlineStateEventDTO> {
        private long currentMaxTimeMills = -1L;
        private long maxOutOfOrderness = BizConstant.MAX_OUT_OF_ORDERNESS;

        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimeMills - maxOutOfOrderness);
        }

        @Override
        public long extractTimestamp(OnlineStateEventDTO OnlineStateEventDTO, long l) {
            Long timestamp = OnlineStateEventDTO.getTimestamp();
            currentMaxTimeMills = Math.max(timestamp, currentMaxTimeMills);
            return timestamp;
        }
    }

    static class OnlineKeyFunc implements KeySelector<OnlineStateEventDTO, String> {
        @Override
        public String getKey(OnlineStateEventDTO onlineStateEventDTO) throws Exception {
            return onlineStateEventDTO.getVin();
        }
    }

    public static class OnlineFunction extends ProcessFunction<OnlineStateEventDTO, String> {
        private transient ValueState<OnlinePaceV1> onlineState = null;

        @Override
        public void open(Configuration config) {
            onlineState = getRuntimeContext().getState(new ValueStateDescriptor<>("online-pace", OnlinePaceV1.class));
        }

        @Override
        public void processElement(OnlineStateEventDTO onlineStateEventDTO, Context context, Collector<String> collector) throws Exception {

            String vin = onlineStateEventDTO.getVin();

            String teNumber = onlineStateEventDTO.getTeNumber();

            String simNo = onlineStateEventDTO.getSimNo();

            Long timestamp = onlineStateEventDTO.getTimestamp();

            Boolean onlineStatus = onlineStateEventDTO.isOnline();
            //服务端Ip
            String serverIp = onlineStateEventDTO.getServerIp();
            //网关类型
            String gatewayType = onlineStateEventDTO.getGatewayType();

            Integer offlineType = onlineStateEventDTO.getOfflineType();

            Short access = onlineStateEventDTO.getAccess();

            String teNo = onlineStateEventDTO.getTeNo();

            String teModelNo = onlineStateEventDTO.getTeModelNo();

            Integer carModel = onlineStateEventDTO.getCarModel();

            String batchNo = onlineStateEventDTO.getBatchNo();

            Byte teType = onlineStateEventDTO.getTeType();

            String iccid = onlineStateEventDTO.getIccid();

            JSONArray states = onlineStateEventDTO.getStates();
            List<StateDTO> stateDTOS = null;

            String geoHash = null;
            if ((states != null)  && (states.size() > 0)) {
                stateDTOS = JSONObject.parseArray(states.toJSONString(), StateDTO.class);
                Collections.sort(stateDTOS, new Comparator<StateDTO>() {

                    @Override
                    public int compare(StateDTO o1, StateDTO o2) {
//                        return o1.getCurrentTime().toString() - o2.getCurrentTime().toString();
                        return o1.getCurrentTime().compareTo(o2.getCurrentTime());
                    }
                });
                StateDTO stateDTO = stateDTOS.get(stateDTOS.size() - 1);

                BigDecimal longitude = stateDTO.getLongitude();
                BigDecimal latitude = stateDTO.getLatitude();

                if (latitude != null && longitude != null) {
                    geoHash = GeohashUtil.getGeohashCode(Double.valueOf(latitude.toString()), Double.valueOf(longitude.toString()), 7);
                }
            }

            OnlinePaceV1 value = onlineState.value();
            if (value == null) {
                value = new OnlinePaceV1();
                value.setDataType("OnlinePace");
                value.setVin(vin);
                value.setTeNumber(teNumber);
                value.setSimNo(simNo);
                value.setStartTime(timestamp);
                value.setServerIp(serverIp);
                value.setOnlineStatus(onlineStatus);
                value.setGatewayType(gatewayType);
                value.setOfflineType(offlineType);
                value.setAccess(access);
                value.setTeNo(teNo);
                value.setBatchNo(batchNo);
                value.setTeType(teType);
                value.setTeModelNo(teModelNo);
                value.setCarModel(carModel);
                value.setIccid(iccid);
                value.setStartGeoHash(geoHash);
                value.setStates(stateDTOS);
            } else {
                Boolean historyStatus = value.getOnlineStatus();
                Long startTime = value.getStartTime();
                value.setEndTime(timestamp);
                value.setEndGeoHash(geoHash);
                value.setSpendTime(timestamp - startTime);
                if (historyStatus != onlineStatus) {
                    collector.collect(JSON.toJSONString(value));
                    value.setDataType("OnlinePace");
                    value.setVin(vin);
                    value.setTeNumber(teNumber);
                    value.setSimNo(simNo);
                    value.setStartTime(timestamp);
                    value.setServerIp(serverIp);
                    value.setOnlineStatus(onlineStatus);
                    value.setGatewayType(gatewayType);
                    value.setOfflineType(offlineType);
                    value.setAccess(access);
                    value.setTeNo(teNo);
                    value.setBatchNo(batchNo);
                    value.setTeType(teType);
                    value.setTeModelNo(teModelNo);
                    value.setCarModel(carModel);
                    value.setIccid(iccid);
                    value.setStartGeoHash(geoHash);
                    value.setStates(stateDTOS);
                }
            }
            onlineState.update(value);
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

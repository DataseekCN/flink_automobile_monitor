package com.ccclubs.ca.streaming.appgroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.OnlinePace;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.DateTimeUtil;
import com.ccclubs.common.util.PropertiesHelper;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
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
import org.apache.flink.util.Collector;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Properties;

public class OnlineApp {
    private static Logger logger = Logger.getLogger(OnlineApp.class);

    public static void main(String[] args) throws Exception {
        Logger.getLogger("org").setLevel(Level.ERROR);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        //检查点
        env.enableCheckpointing(60000L);
        env.getConfig().setAutoWatermarkInterval(10000L);

        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();

        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", propertiesHelper.getValue(BizConstant.ACCEPT_BROKER));
        consumerProps.setProperty("enable.auto.commit", "true");
        consumerProps.setProperty("group.id", propertiesHelper.getValue(BizConstant.NETWORK_GROUP_ID));

        FlinkKafkaConsumer011<String> myConsumer =
                new FlinkKafkaConsumer011<String>("receive", new SimpleStringSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<String> myProducer =
                new FlinkKafkaProducer011<String>(propertiesHelper.getValue(BizConstant.INTRANET_SEND_BROKER), BizConstant.TELECOM_NETWORK_TOPIC, new SimpleStringSchema());

        DataStream<String> streamSource = env.addSource(myConsumer);
        DataStream<String> streamOperator = streamSource
                .assignTimestampsAndWatermarks(new OnlineBoundedLatenessWatermarkAssigner())
                .keyBy(new OnlineKeyFunc())
                .process(new OnlineFunction());

        streamOperator.addSink(myProducer);
        env.execute("online rate");
    }

    static class OnlineBoundedLatenessWatermarkAssigner implements AssignerWithPeriodicWatermarks<String> {
        private long currentMaxTimeMills = -1L;
        private long maxOutOfOrderness = BizConstant.MAX_OUT_OF_ORDERNESS;

        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimeMills - maxOutOfOrderness);
        }

        @Override
        public long extractTimestamp(String line, long l) {
            JSONObject jsonObject = JSON.parseObject(line);
            Long timestamp = jsonObject.getLong("timestamp");
            currentMaxTimeMills = Math.max(timestamp, currentMaxTimeMills);
            return timestamp;
        }
    }

    static class OnlineKeyFunc implements KeySelector<String, String> {
        @Override
        public String getKey(String line) throws Exception {
            JSONObject jsonObject = JSON.parseObject(line);
            Long timestamp = jsonObject.getLong("timestamp");
            String key = DateTimeUtil.getDateTimeByFormat(timestamp, DateTimeUtil.format2) + "|" + jsonObject.getString("vin");
            return key;
        }
    }

    public static class OnlineFunction extends ProcessFunction<String, String> {
        private transient ValueState<OnlinePace> onlineState = null;
        private transient ValueState<Long> sendTime = null;

        @Override
        public void open(Configuration config) {
            onlineState = getRuntimeContext().getState(new ValueStateDescriptor<>("online pace", OnlinePace.class));
            sendTime = getRuntimeContext().getState(new ValueStateDescriptor<>("send time", Long.class));
        }

        @Override
        public void processElement(String event, Context context, Collector<String> collector) throws Exception {
            JSONObject jsonObject = JSON.parseObject(event);
            String vin = jsonObject.getString("vin");
            String teNumber = jsonObject.getString("teNumber");
            String gatewayType = jsonObject.getString("gatewayType");
            Long timestamp = jsonObject.getLong("timestamp");
            Boolean onlineStatus = jsonObject.getBoolean("online");
            Integer online = 0;
            Integer offline = 0;
            if (onlineStatus) {
                online = 1;
            } else {
                offline = 1;
            }
            OnlinePace value = onlineState.value();
            if (value == null) {
                value = new OnlinePace();
                value.setDataType("OnlineRate");
                value.setVin(vin);
                value.setTeNumber(teNumber);
                value.setGatewayType(gatewayType);
                value.setStartTime(timestamp);
                value.setEndTime(timestamp);
                value.setOnlineCount(online);
                value.setOfflineCount(offline);
                value.setCount(1);
                //Online
                if (online == 1) {
                    value.setOnlineStartTime(timestamp);
                    value.setOnlineEndTime(timestamp);
                    value.setOfflineStartTime(0L);
                    value.setOfflineEndTime(0L);
                    value.setOnlineStatistics(true);
                    value.setOnlineSpendTime(0L);
                    value.setOfflineSpendTime(0L);
                }
                //Offline
                else {
                    value.setOfflineStartTime(timestamp);
                    value.setOfflineEndTime(timestamp);
                    value.setOnlineStartTime(0L);
                    value.setOnlineEndTime(0L);
                    value.setOnlineStatistics(false);
                    value.setOnlineSpendTime(0L);
                    value.setOfflineSpendTime(0L);
                }
            } else {
                Long startTime = value.getStartTime();
                Long endTime = value.getEndTime();
                Long onlineStartTime = value.getOnlineStartTime();
                Long onlineEndTime = value.getOnlineEndTime();
                Long offlineStartTime = value.getOfflineStartTime();
                Long offlineEndTime = value.getOfflineEndTime();
                Long onlineSpendTime = value.getOnlineSpendTime();
                Long offlineSpendTime = value.getOfflineSpendTime();
                Boolean onlineStatistics = value.getOnlineStatistics();

                //Online
                if (online == 1) {
                    if (onlineStatistics) {
                        //每次SpendTime增值
                        long minOnline = Math.min(onlineStartTime, timestamp);
                        long maxOnline = Math.max(onlineEndTime, timestamp);
                        long oldCurrentOnlineSpend = onlineEndTime - onlineStartTime;
                        long newCurrentOnlineSpend = maxOnline - minOnline;
                        long changeCurrentOnlineSpend = newCurrentOnlineSpend - oldCurrentOnlineSpend;

                        value.setOnlineStartTime(minOnline);
                        value.setOnlineEndTime(maxOnline);
                        value.setOnlineSpendTime(onlineSpendTime+changeCurrentOnlineSpend);
                    }
                    //Offline
                    else {
                        long newCurrentOfflineSpend = Math.max(timestamp, offlineEndTime) - offlineStartTime;
                        long oldCurrentOfflineSpend = offlineEndTime - offlineStartTime;
                        long changeCurrentOfflineSpend = newCurrentOfflineSpend - oldCurrentOfflineSpend;

                        value.setOfflineSpendTime(offlineSpendTime+changeCurrentOfflineSpend);
                        value.setOfflineStartTime(0L);
                        value.setOfflineEndTime(0L);
                        value.setOnlineStartTime(timestamp);
                        value.setOnlineEndTime(timestamp);
                        value.setOnlineStatistics(true);
                    }
                }
                //Offline
                else {
                    //Online
                    //在线转掉线时应以在线的结束时间为掉线的开始时间
                    if (onlineStatistics) {
                        value.setOfflineStartTime(onlineEndTime);
                        value.setOfflineEndTime(timestamp);
                        value.setOfflineSpendTime(offlineSpendTime+Math.abs(timestamp-onlineEndTime));
                        value.setOnlineStatistics(false);
                        value.setOnlineStartTime(0L);
                        value.setOnlineEndTime(0L);
                    } else {
                        long minOffline = Math.min(offlineStartTime, timestamp);
                        long maxOffline = Math.max(offlineEndTime, timestamp);
                        long oldCurrentOfflineSpend = offlineEndTime - offlineStartTime;
                        long newCurrentOfflineSpend = maxOffline - minOffline;
                        long changeCurrentOfflineSpend = newCurrentOfflineSpend - oldCurrentOfflineSpend;

                        value.setOfflineStartTime(minOffline);
                        value.setOfflineEndTime(maxOffline);
                        value.setOfflineSpendTime(offlineSpendTime+changeCurrentOfflineSpend);
                    }
                }
                online = value.getOnlineCount() + online;
                offline = value.getOfflineCount() + offline;
                value.setStartTime(Math.min(startTime, timestamp));
                value.setEndTime(Math.max(endTime, timestamp));
                value.setOnlineCount(online);
                value.setOfflineCount(offline);
                value.setCount((value.getCount() + 1));
            }
            value.setSpendTime(value.getEndTime() - value.getStartTime());
            onlineState.update(value);

            if (value.getSpendTime() > 0) {
                Double rate = value.getOnlineSpendTime().doubleValue() / value.getSpendTime().doubleValue();
                BigDecimal onlineRate = new BigDecimal(rate.toString()).setScale(2,BigDecimal.ROUND_HALF_UP);
                value.setOnlineRate(onlineRate);
                Long sendMills = sendTime.value();
                if ((sendMills == null) || (timestamp - sendMills >= 60000)) {
                    collector.collect(JSON.toJSONString(value));
                    sendTime.update(timestamp);
                }
            }
            context.timerService().registerEventTimeTimer(value.getEndTime() + (30 * 60 * 1000));
        }

        @Override
        public void onTimer(long timestamp, OnTimerContext context, Collector<String> out) throws Exception {
            OnlinePace onlineValue = onlineState.value();
            Long sendMills = sendTime.value();
            if (onlineValue != null) {
                if ((sendMills == null) && (onlineValue.getSpendTime() > 0)) {
                    onlineValue.setOnlineRate(new BigDecimal(onlineValue.getOnlineSpendTime() / onlineValue.getSpendTime()).setScale(2));
                    out.collect(JSON.toJSONString(onlineValue));
                    sendTime.update(timestamp);
                } else if ((sendMills != null) && (sendMills < onlineValue.getEndTime())) {
                    onlineValue.setOnlineRate(new BigDecimal(onlineValue.getOnlineSpendTime() / onlineValue.getSpendTime()).setScale(2));
                    out.collect(JSON.toJSONString(onlineValue));
                    sendTime.update(timestamp);
                }
            }

            onlineState.clear();
            sendTime.clear();
        }
    }
}

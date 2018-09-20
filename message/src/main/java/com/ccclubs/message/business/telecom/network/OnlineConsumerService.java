package com.ccclubs.message.business.telecom.network;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.common.util.DateTimeUtil;
import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.message.bean.OnlinePace;
import com.ccclubs.message.bean.Pace;
import com.ccclubs.message.util.MessageConst;
import com.ccclubs.storage.mysql.MysqlTool;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by taosm on 2018/6/4.
 */
public class OnlineConsumerService {
    private static OnlineConsumerService onlineConsumerService;
    private KafkaConsumer<String, String> consumer;
    private static Logger logger = Logger.getLogger(OnlineConsumerService.class);
    MysqlTool mysqlTool = null;

    private OnlineConsumerService() {
    }

    public void doInit() {
        mysqlTool = MysqlTool.getInstance();
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String KAFKA_BROKERS = propertiesHelper.getValue(MessageConst.KAFKA_INTERNAL_BROKERS_KEY);
        String topic = propertiesHelper.getValue(MessageConst.KAFKA_ONLINE_TOPIC_KEY);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "network_mysql_group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));
    }

    public synchronized static OnlineConsumerService getInstance() {
        if (onlineConsumerService == null) {
            onlineConsumerService = new OnlineConsumerService();
            onlineConsumerService.doInit();
        }
        return onlineConsumerService;
    }

    public void doConsumer() {
        if (consumer != null) {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(5000);
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition topicPartition : partitions) {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(topicPartition);
                    JSONArray onlinePaceArray = new JSONArray();
                    for (ConsumerRecord<String, String> consumerRecord : partitionRecords) {
                        try {
                            JSONObject jsonObject = JSON.parseObject(consumerRecord.value());
                            String dataType = jsonObject.getString("dataType");

                            if (MessageConst.PACE_ONLINEPACE.equalsIgnoreCase(dataType)) {
                                String vin = jsonObject.getString("vin");
                                if (vin != null) {
                                    if (vin.length() == 19) {
                                        logger.info(jsonObject);
                                    }
                                    onlinePaceArray.add(jsonObject);
                                }
                            }
                        } catch (Exception ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    doOnlinePaceInsert(onlinePaceArray);

                    onlinePaceArray.clear();
                }
            }
        }
    }

    public void doOnlinePaceInsert(JSONArray jsonArray) {
        String sql = " insert into " +
                "data_center.network_online " +
                "(stats_date,vin,te_number,start_time,end_time,spend_time," +
                "start_timestr,end_timestr," +
                "online_count,offline_count,count," +
                "online_time,offline_time," +
                "online_rate,online_status)" +
                "values(?,?,?,?,?,?, ?,?, ?,?,?, ?,?, ?,?)  " +
                "ON DUPLICATE KEY UPDATE " +
                "end_time=values(end_time),spend_time=values(spend_time)," +
                "end_timestr=values(end_timestr)," +
                "online_count=values(online_count),offline_count=values(offline_count),count=values(count)," +
                "online_time=values(online_time),offline_time=values(offline_time)," +
                "online_rate=values(online_rate),online_status=values(online_status)";
        doPaceInsert(sql, jsonArray);
    }


    private void doPaceInsert(String sql, JSONArray jsonArray) {
        Connection connection = mysqlTool.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                OnlinePace pace = JSON.parseObject(jsonObject.toJSONString(), OnlinePace.class);
                //Vin码
                String vin = pace.getVin();
                //车机号
                String teNumber = pace.getTeNumber();
                //开始时间
                Long startTime = pace.getStartTime();

                String statsDate = DateTimeUtil.getDateTimeByFormat(startTime, DateTimeUtil.format2);
                //结束时间
                Long endTime = pace.getEndTime();

                String startTimeStr = DateTimeUtil.getDateTimeByFormat(startTime, DateTimeUtil.format1);

                String endTimeStr = DateTimeUtil.getDateTimeByFormat(endTime, DateTimeUtil.format1);
                //消耗时间
                Long spendTime = pace.getSpendTime();

                Integer onlineCount = pace.getOnlineCount();

                Integer offlineCount = pace.getOfflineCount();

                Long onlineSpendTime = pace.getOnlineSpendTime();

                Long offlineSpendTime = pace.getOfflineSpendTime();

                Integer count = pace.getCount();

                BigDecimal onlineRate = pace.getOnlineRate();

                Boolean onlineStatistics = pace.getOnlineStatistics();

                Integer onlineStatus;
                if (onlineStatistics) {
                    onlineStatus = 1;
                } else {
                    onlineStatus = 0;
                }

                int parameterIndex = 1;
                if (vin != null && startTime != null && endTime != null) {
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, statsDate);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, vin);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, teNumber);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, spendTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startTimeStr);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endTimeStr);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, onlineCount);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, offlineCount);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, count);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, onlineSpendTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, offlineSpendTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, onlineRate);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, onlineStatus);
                    preparedStatement.addBatch();
                }
            }
            mysqlTool.insertRecords(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        OnlineConsumerService onlineConsumerService = new OnlineConsumerService();
        onlineConsumerService.doInit();
        onlineConsumerService.doConsumer();
    }

}

package com.ccclubs.message.business.realtime.status;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.common.util.DateTimeUtil;
import com.ccclubs.common.util.GeohashUtil;
import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.message.bean.CarState;
import com.ccclubs.message.bean.Pace;
import com.ccclubs.message.business.activity.pace.PaceConsumerService;
import com.ccclubs.message.util.MessageConst;
import com.ccclubs.storage.mysql.MysqlTool;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by taosm on 2018/6/6.
 */
public class StatusConsumerService {
    private static StatusConsumerService statusConsumerService;
    private KafkaConsumer<String, String> consumer;
    private static Logger logger = Logger.getLogger(PaceConsumerService.class);
    MysqlTool mysqlTool = null;

    private StatusConsumerService() {
    }

    public void doInit() {
        mysqlTool = MysqlTool.getInstance();
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String KAFKA_BROKERS = propertiesHelper.getValue(MessageConst.KAFKA_CA_BROKERS_KEY);
        String topic = propertiesHelper.getValue(MessageConst.KAFKA_CA_TOPIC_KEY);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "status_mysql_group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));
    }

    public synchronized static StatusConsumerService getInstance() {
        if (statusConsumerService == null) {
            statusConsumerService = new StatusConsumerService();
            statusConsumerService.doInit();
        }
        return statusConsumerService;
    }

    public void doConsumer() {
        if (consumer != null) {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(5000);
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition topicPartition : partitions) {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(topicPartition);
                    JSONArray realStatusArray = new JSONArray();
                    for (ConsumerRecord<String, String> consumerRecord : partitionRecords) {
                        try {
                            JSONObject jsonObject = JSON.parseObject(consumerRecord.value());
                            Double longitude = jsonObject.getDouble("cssLongitude");
                            Double latitude = jsonObject.getDouble("cssLatitude");
                            if (longitude != null && latitude != null) {
                                realStatusArray.add(jsonObject);
                            }
                        } catch (Exception ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    doRealStatusInsert(realStatusArray);

                    realStatusArray.clear();
                }
            }
        }
    }

    public void doRealStatusInsert(JSONArray jsonArray) {
        String sql = " insert into " +
                "data_center.real_time_status " +
                "(vin,upload_time," +
                "ev_battery,speed,obd_miles," +
                "charge_status,engine_status," +
                "geohash) " +
                "values(?,?, ?,?,?, ?,?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "upload_time=values(upload_time)," +
                "ev_battery=values(ev_battery),speed=values(speed),obd_miles=values(obd_miles)," +
                "charge_status=values(charge_status),engine_status=values(engine_status)," +
                "geohash=values(geohash)";
        doStatusInsert(sql, jsonArray);
    }

    private void doStatusInsert(String sql, JSONArray jsonArray) {
        Connection connection = mysqlTool.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;

                String vin = jsonObject.getString("cssVin");
                Long currentTime = jsonObject.getLong("cssCurrentTime");
                String currentTimeStr = DateTimeUtil.getDateTimeByFormat(currentTime, DateTimeUtil.format1);
                Integer engineStatus = jsonObject.getInteger("cssEngine");
                Integer chargingStatus = jsonObject.getInteger("cssCharging");
                Double longitude = jsonObject.getDouble("cssLongitude");
                Double latitude = jsonObject.getDouble("cssLatitude");
                Float evBattery = jsonObject.getFloat("cssEvBattery");
                Float speed = jsonObject.getFloat("cssSpeed");
                Float obdMiles = jsonObject.getFloat("cssObdMile");
                String geoHash = null;
                if (latitude != null && longitude != null && latitude > 0 && longitude > 0) {
                    geoHash = GeohashUtil.getGeohashCode(latitude, longitude, 7);
                }


                int parameterIndex = 1;
                if (vin != null && currentTime != null) {
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, vin);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, currentTimeStr);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, evBattery);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, speed);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, obdMiles);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, chargingStatus);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, engineStatus);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, geoHash);
                    preparedStatement.addBatch();
                }
            }
            mysqlTool.insertRecords(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

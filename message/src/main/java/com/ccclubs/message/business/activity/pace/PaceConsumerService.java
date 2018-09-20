package com.ccclubs.message.business.activity.pace;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.common.util.DateTimeUtil;
import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.message.bean.Pace;
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
 * Created by taosm on 2018/6/4.
 */
public class PaceConsumerService {
    private static PaceConsumerService paceConsumerService;
    private KafkaConsumer<String, String> consumer;
    private static Logger logger = Logger.getLogger(PaceConsumerService.class);
    MysqlTool mysqlTool = null;

    private PaceConsumerService() {
    }

    public void doInit() {
        mysqlTool = MysqlTool.getInstance();
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String KAFKA_BROKERS = propertiesHelper.getValue(MessageConst.KAFKA_INTERNAL_BROKERS_KEY);
        String topic = propertiesHelper.getValue(MessageConst.KAFKA_PACE_TOPIC_KEY);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "pace_mysql_group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));
    }

    public synchronized static PaceConsumerService getInstance() {
        if (paceConsumerService == null) {
            paceConsumerService = new PaceConsumerService();
            paceConsumerService.doInit();
        }
        return paceConsumerService;
    }

    public void doConsumer() {
        if (consumer != null) {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(5000);
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition topicPartition : partitions) {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(topicPartition);
                    JSONArray launchPaceArray = new JSONArray();
                    JSONArray drivePaceArray = new JSONArray();
                    JSONArray chargePaceArray = new JSONArray();
                    for (ConsumerRecord<String, String> consumerRecord : partitionRecords) {
                        try {
                            JSONObject jsonObject = JSON.parseObject(consumerRecord.value());
                            String dataType = jsonObject.getString("dataType");
                            Long spendTime = jsonObject.getLong("spendTime");
                            if (spendTime > 2 * 60 * 1000) {
                                if (MessageConst.PACE_CHARGEPACE.equalsIgnoreCase(dataType)) {
                                    chargePaceArray.add(jsonObject);
                                } else if (MessageConst.PACE_LAUNCHPACE.equalsIgnoreCase(dataType)) {
                                    launchPaceArray.add(jsonObject);
                                } else if (MessageConst.PACE_DRIVEPACE.equalsIgnoreCase(dataType)) {
                                    drivePaceArray.add(jsonObject);
                                }
                            }
                        } catch (Exception ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    doLaunchPaceInsert(launchPaceArray);
                    doDrivePaceInsert(drivePaceArray);
                    doSocPaceInsert(chargePaceArray);

                    launchPaceArray.clear();
                    drivePaceArray.clear();
                    chargePaceArray.clear();
                }
            }
        }
    }

    public void doLaunchPaceInsert(JSONArray jsonArray) {
        String sql = " insert into " +
                "data_center.pace_launch " +
                "(vin,start_time,end_time,spend_time," +
                "start_timestr,end_timestr," +
                "start_soc,end_soc,change_soc," +
                "start_miles,end_miles,change_miles," +
                "start_oil,end_oil,change_oil," +
                "start_longitude,start_latitude,end_longitude,end_latitude," +
                "start_geohash,end_geohash) " +
                "values(?,?,?,?, ?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?,?, ?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "end_time=values(end_time),spend_time=values(spend_time),end_timestr=values(end_timestr)," +
                "end_soc=values(end_soc),change_soc=values(change_soc)," +
                "end_miles=values(end_miles),change_miles=values(change_miles)," +
                "end_oil=values(end_oil),change_oil=values(change_oil)," +
                "end_longitude=values(end_longitude),end_latitude=values(end_latitude)," +
                "end_geohash=values(end_geohash)";
        doPaceInsert(sql, jsonArray);
    }


    public void doDrivePaceInsert(JSONArray jsonArray) {
        String sql = " insert into " +
                "data_center.pace_drive " +
                "(vin,start_time,end_time,spend_time," +
                "start_timestr,end_timestr," +
                "start_soc,end_soc,change_soc," +
                "start_miles,end_miles,change_miles," +
                "start_oil,end_oil,change_oil," +
                "start_longitude,start_latitude,end_longitude,end_latitude," +
                "start_geohash,end_geohash)" +
                "values(?,?,?,?, ?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?,?, ?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "end_time=values(end_time),spend_time=values(spend_time),end_timestr=values(end_timestr)," +
                "end_soc=values(end_soc),change_soc=values(change_soc)," +
                "end_miles=values(end_miles),change_miles=values(change_miles)," +
                "end_oil=values(end_oil),change_oil=values(change_oil)," +
                "end_longitude=values(end_longitude),end_latitude=values(end_latitude)," +
                "end_geohash=values(end_geohash)";
        doPaceInsert(sql, jsonArray);
    }

    public void doSocPaceInsert(JSONArray jsonArray) {
        String sql = " insert into " +
                "data_center.pace_charge " +
                "(vin,start_time,end_time,spend_time," +
                "start_timestr,end_timestr," +
                "start_soc,end_soc,change_soc," +
                "start_miles,end_miles,change_miles," +
                "start_oil,end_oil,change_oil," +
                "start_longitude,start_latitude,end_longitude,end_latitude," +
                "start_geohash,end_geohash)" +
                "values(?,?,?,?, ?,?, ?,?,?, ?,?,?, ?,?,?, ?,?,?,?, ?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "end_time=values(end_time),spend_time=values(spend_time),end_timestr=values(end_timestr)," +
                "end_soc=values(end_soc),change_soc=values(change_soc)," +
                "end_miles=values(end_miles),change_miles=values(change_miles)," +
                "end_oil=values(end_oil),change_oil=values(change_oil)," +
                "end_longitude=values(end_longitude),end_latitude=values(end_latitude)," +
                "end_geohash=values(end_geohash)";
        doPaceInsert(sql, jsonArray);
    }

    private void doPaceInsert(String sql, JSONArray jsonArray) {
        Connection connection = mysqlTool.getConnection();
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (Object obj : jsonArray) {
                JSONObject jsonObject = (JSONObject) obj;
                Pace pace = JSON.parseObject(jsonObject.toJSONString(), Pace.class);
                //Vin码
                String vin = pace.getVin();
                //开始时间
                Long startTime = pace.getStartTime();
                //结束时间
                Long endTime = pace.getEndTime();
                //消耗时间
                Long spendTime = pace.getSpendTime();

                String startTimeStr = DateTimeUtil.getDateTimeByFormat(startTime, DateTimeUtil.format1);

                String endTimeStr = DateTimeUtil.getDateTimeByFormat(endTime, DateTimeUtil.format1);
                //开始电量
                Float startSoc = pace.getStartSoc();
                //结束电量
                Float endSoc = pace.getEndSoc();
                //变化电量
                Float changeSoc = pace.getChangeSoc();
                //开始OBD里程
                Float startObdMile = pace.getStartObdMile();
                //结束OBD里程
                Float endObdMile = pace.getEndObdMile();
                //变化OBD里程
                Float changeObdMile = pace.getChangeObdMile();
                //开始油量
                Float startOil = pace.getStartOil();
                //结束油量
                Float endOil = pace.getEndOil();
                //变化油量
                Float changeOil = pace.getChangeOil();
                Float startLongitude = pace.getStartLongitude();
                Float startLatitude = pace.getStartLatitude();
                Float endLongitude = pace.getEndLongitude();
                Float endLatitude = pace.getEndLatitude();
                String startGeoHash = pace.getStartGeoHash();
                String endGeoHash = pace.getEndGeoHash();
                int parameterIndex = 1;
                if (vin != null && startTime != null && endTime != null) {
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, vin);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, spendTime);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startTimeStr);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endTimeStr);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startSoc);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endSoc);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, changeSoc);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startObdMile);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endObdMile);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, changeObdMile);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startOil);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endOil);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, changeOil);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startLongitude);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startLatitude);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endLongitude);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endLatitude);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, startGeoHash);
                    MysqlTool.doPrepareStatementSet(preparedStatement, parameterIndex++, endGeoHash);
                    preparedStatement.addBatch();
                }
            }
            mysqlTool.insertRecords(preparedStatement);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

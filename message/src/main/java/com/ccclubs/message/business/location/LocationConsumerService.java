package com.ccclubs.message.business.location;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.message.bean.GeoBean;
import com.ccclubs.message.util.MessageConst;
import com.ccclubs.storage.redis.RedisTool;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by taosm on 2018/6/6.
 */
public class LocationConsumerService {
    private static LocationConsumerService locationConsumerService;
    private KafkaConsumer<String, String> consumer;
    private static Logger logger = Logger.getLogger(LocationConsumerService.class);

    RedisTool redisTool = null;

    public void doInit() {
        redisTool = RedisTool.getInstance();
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String KAFKA_BROKERS = propertiesHelper.getValue(MessageConst.KAFKA_BROKERS_KEY);
        String topic = propertiesHelper.getValue(MessageConst.KAFKA_LOCATION_TOPIC_KEY);
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "30000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<String, String>(props);
        consumer.subscribe(Arrays.asList(topic));
    }

    public synchronized static LocationConsumerService getInstance() {
        if (locationConsumerService == null) {
            locationConsumerService = new LocationConsumerService();
            locationConsumerService.doInit();
        }
        return locationConsumerService;
    }

    public void doConsumer() {
        if (consumer != null) {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(5000);
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition topicPartition : partitions) {
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(topicPartition);
                    JSONArray locationArray = new JSONArray();
                    for (ConsumerRecord<String, String> consumerRecord : partitionRecords) {
                        try {
                            JSONObject jsonObject = JSON.parseObject(consumerRecord.value());
                            String dataType = jsonObject.getString("dataType");
                            //地图收集
                            if (MessageConst.LOCATION_KEY.equalsIgnoreCase(dataType)) {
                                locationArray.add(jsonObject);
                            }
                        } catch (Exception ex) {
                            logger.error(ex.getMessage());
                        }
                    }
                    doLocationInsert(locationArray);

                }
            }
        }
    }

    public void doLocationInsert(JSONArray jsonArray) {
        Set<String> set = new HashSet<>();
        for (Object obj : jsonArray) {
            JSONObject jsonObject = (JSONObject) obj;
            GeoBean geoBean = JSON.parseObject(jsonObject.toJSONString(), GeoBean.class);
            //geoHash
            String geoHash = geoBean.getGeoHash();

            if (geoHash!=null){
                set.add(geoHash);
            }
        }
        redisTool.doInsert("geohash",set);
    }
}

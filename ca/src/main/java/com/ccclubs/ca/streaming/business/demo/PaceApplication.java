package com.ccclubs.ca.streaming.business.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.common.util.DateTimeUtil;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.assigners.EventTimeSessionWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.util.Collector;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created by taosm on 2018/5/30.
 */
public class PaceApplication {
    private static Logger logger = Logger.getLogger(PaceApplication.class);
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        env.enableCheckpointing(60000);

        Properties consumerProps = new Properties();
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String topic = propertiesHelper.getValue(MonitorConsts.KAFKA_CONSUMER_TOPIC_CACDZ_KEY);
        String brokers= propertiesHelper.getValue(MonitorConsts.KAFKA_CONSUMER_BROKER_CACDZ_KEY);
        String zk = propertiesHelper.getValue(MonitorConsts.KAFKA_CONSUMER_ZK_CACDZ_KEY);
        consumerProps.setProperty("bootstrap.servers", brokers);
        consumerProps.setProperty("zookeeper.connect", zk);
        consumerProps.setProperty("enable.auto.commit", "true");
        consumerProps.setProperty("group.id", "tao1");
        FlinkKafkaConsumer011<String> myConsumer =
                new FlinkKafkaConsumer011<String>(java.util.regex.Pattern.compile(topic),new SimpleStringSchema(),consumerProps);

        myConsumer.setStartFromLatest();
        DataStream<String> originStream = env.addSource(myConsumer);
        DataStream<JSONObject> jsonStream = originStream.map(new MapFunction<String, JSONObject>() {
            @Override
            public JSONObject map(String line) throws Exception {
                JSONObject jsonObject = null;
                try{
                    jsonObject= JSON.parseObject(line);
                }
                catch (Exception ex){
                    logger.error(ex.getMessage());
                }
                return jsonObject;
            }
        });


        DataStream<JSONObject> filterStream =jsonStream.filter(new MyFilterFunction());

        KeyedStream<JSONObject,String> keyStream =filterStream
                .assignTimestampsAndWatermarks(new BoundedOutOfOrderGenerator())
                .keyBy(new MyKeySelector());

        DataStream<JSONObject> resultStream = keyStream.window(EventTimeSessionWindows.withGap(Time.minutes(5)))
                .process(new ProcessWindowFunction<JSONObject, JSONObject, String, TimeWindow>() {
                    @Override
                    public void process(String key, Context context, Iterable<JSONObject> iterable, Collector<JSONObject> collector) throws Exception {
                        JSONObject jsonObject =  new JSONObject();
                        jsonObject.put("vin",key);
                        long minTimeMills=0l;
                        long maxTimeMills=0l;
                        Iterator<JSONObject> iterator = iterable.iterator();
                        while (iterator.hasNext()){
                            JSONObject obj = iterator.next();
                            Long cssCurrentTime = obj.getLong("cssCurrentTime");
                            if(minTimeMills==0){
                                minTimeMills=cssCurrentTime;
                            }
                            else {
                                minTimeMills = Math.min(minTimeMills, cssCurrentTime);
                            }
                            maxTimeMills=Math.max(maxTimeMills,cssCurrentTime);
                        }
                        jsonObject.put("startTime", DateTimeUtil.getDateTimeByFormat(minTimeMills,DateTimeUtil.format1));
                        jsonObject.put("endTime",DateTimeUtil.getDateTimeByFormat(maxTimeMills,DateTimeUtil.format1));

                        collector.collect(jsonObject);
                    }
                });

        resultStream.print();
        //jsonStream.print();
        env.execute();
    }
    //过滤器
    static class MyFilterFunction implements FilterFunction<JSONObject>{
        @Override
        public boolean filter(JSONObject jsonObject) throws Exception {
            Integer cssEngine = jsonObject.getInteger("cssEngine");
            if(cssEngine==1||cssEngine==3){
                return true;
            }
            else {
                return false;
            }
        }
    }

    //key选择器
    static class MyKeySelector implements KeySelector<JSONObject,String>{
        @Override
        public String getKey(JSONObject jsonObject) throws Exception {
            String cssVin = jsonObject.getString("cssVin");
            return cssVin;
        }
    }

    //水印生成器
    static class BoundedOutOfOrderGenerator implements AssignerWithPeriodicWatermarks<JSONObject>{
        private final long maxOutOfOrderness = 5000l;
        private long currentMaxTimestamp = 0l;

        @Nullable
        @Override
        public Watermark getCurrentWatermark() {
            return new Watermark(currentMaxTimestamp-maxOutOfOrderness);
        }

        @Override
        public long extractTimestamp(JSONObject jsonObject, long l) {
            long cssCurrentTime = jsonObject.getLong("cssCurrentTime");
            currentMaxTimestamp=Math.max(currentMaxTimestamp,cssCurrentTime);
            return cssCurrentTime;
        }
    }
}

package com.ccclubs.ca.streaming.business.demo;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;

/**
 * Created by taosm on 2018/5/29.
 */
public class Demo1 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<TestBean> beanDataStream = env.addSource(new TestSource());
        DataStream<TestBean> ruleStream = env.addSource(new RuleSource());
        DataStream<TestBean> connectedStream = beanDataStream.union(ruleStream);

        FlinkKafkaProducer011<TestBean> myProducer = new FlinkKafkaProducer011<TestBean>(
                "192.168.3.102:9092",
                "test-topic",
                new KeyedSerializationSchema<TestBean>() {
                    @Override
                    public byte[] serializeKey(TestBean testBean) {
                        return testBean.name.getBytes();
                    }

                    @Override
                    public byte[] serializeValue(TestBean testBean) {
                        return testBean.name.getBytes();
                    }

                    @Override
                    public String getTargetTopic(TestBean testBean) {
                        String topic="test-topic";
                        if(testBean.name.equals("tuzi")){
                            topic="topic-tuzi";
                        }
                        else if(testBean.name.equals("wugui")){
                            topic="topic-wugui";
                        }
                        return topic;
                    }
                }
        );
//        MapStateDescriptor<String,TestRule> ruleDescriptor=new MapStateDescriptor<String, TestRule>(
//                "RuleBroadcastState",
//                BasicTypeInfo.STRING_TYPE_INFO,
//                TypeInformation.of(new TypeHint<TestRule>() {
//
//                })
//        );
//

//        BroadcastStream<TestRule> ruleBroadcastStream = ruleStream.broadcast(ruleDescriptor);

//        DataStream<Long> valueStream = beanDataStream.map(new MapFunction<TestBean, Long>() {
//            @Override
//            public Long map(TestBean testBean) throws Exception {
//                return testBean.value;
//            }
//        });

//        DataStream<Long> output = beanDataStream.connect(ruleBroadcastStream).process(
//                new BroadcastProcessFunction<TestBean, TestRule, Long>() {
//                    private final MapStateDescriptor<String,TestRule> ruleStateDescriptor =
//                            new MapStateDescriptor<>(
//                                    "RuleBroadcastState",
//                                    BasicTypeInfo.STRING_TYPE_INFO,
//                                    TypeInformation.of(new TypeHint<TestRule>() {}));
//
//                    @Override
//                    public void processElement(TestBean testBean, ReadOnlyContext readOnlyContext, Collector<Long> collector) throws Exception {
//                        TestRule testRule = readOnlyContext.getBroadcastState(ruleStateDescriptor).get("ou");
//                        if(testRule!=null){
//                            System.out.println("我逮到规则啦："+testRule.ruleValue);
//                        }
//                    }
//
//                    @Override
//                    public void processBroadcastElement(TestRule testRule, Context context, Collector<Long> collector) throws Exception {
//                        System.out.println("我广播的规则值为:"+testRule.ruleValue);
//                        context.getBroadcastState(ruleStateDescriptor).put("ou",testRule);
//                    }
//                }
//        );
        connectedStream.addSink(myProducer);
        env.execute();
    }


}

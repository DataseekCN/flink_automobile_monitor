package com.ccclubs.ca.streaming.business.demo;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * Created by taosm on 2018/5/29.
 */
public class TestSource implements SourceFunction<TestBean> {
    public void run(SourceContext<TestBean> ctx) throws Exception {
        TestBean tb = null;
        Long value=0l;
        while (true){
            Thread.sleep(1000);
            tb=new TestBean();
            tb.name="tuzi";
            tb.value=value++;
            ctx.collect(tb);
        }
    }

    public void cancel() {

    }
}

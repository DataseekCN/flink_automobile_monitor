package com.ccclubs.ca.streaming.business.demo;

import com.ccclubs.ca.streaming.business.demo.TestBean;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * Created by taosm on 2018/5/29.
 */
public class RuleSource implements SourceFunction<TestBean> {


    @Override
    public void run(SourceContext<TestBean> sourceContext) throws Exception {
        TestBean tb = null;
        Long value=0l;
        while (true){
            Thread.sleep(1000);
            tb=new TestBean();
            tb.name="wugui";
            tb.value=value++;
            sourceContext.collect(tb);
        }

    }

    @Override
    public void cancel() {

    }
}

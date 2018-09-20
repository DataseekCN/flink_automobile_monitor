package com.ccclubs.ca.streaming.business.demo;

import java.io.Serializable;

/**
 * Created by taosm on 2018/5/29.
 */
public class TestBean implements Serializable {
    public String name;
    public Long value;
    public Long current_mills;

    @Override
    public String toString() {
        return "TestBean{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", current_mills=" + current_mills +
                '}';
    }
}

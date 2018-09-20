package com.ccclubs.common.util;

import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Created by taosm on 2018/5/30.
 */
public class PropertiesHelper {
    public JSONObject pro = null;
    private static PropertiesHelper propertiesHelper;

    private PropertiesHelper(){};

    public synchronized static PropertiesHelper getInstance(){
        if(propertiesHelper==null) {
            propertiesHelper = new PropertiesHelper();
            Properties prop = new Properties();
            JSONObject pro = propertiesHelper.pro;
            try {
                prop.load(PropertiesHelper.class.getClassLoader().getResourceAsStream("monitor.properties"));
                Iterator iter = prop.entrySet().iterator();
                pro=new JSONObject();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = entry.getKey() + "";
                    String value = entry.getValue() + "";
                    pro.put(key,value);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            propertiesHelper.pro=pro;
        }
        return propertiesHelper;
    }

    public String getValue(String key){
        String value = null;
        if(pro!=null){
            value = pro.getString(key);
        }
        return value;
    }

    public static void main(String[] args) {
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String value = propertiesHelper.getValue("kafka.consumer.topic.cacdz");
        System.out.println(value);
    }
}

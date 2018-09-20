package com.ccclubs.ca.streaming.util;

import com.ccclubs.common.util.DateTimeUtil;
import com.ccclubs.common.util.GeohashUtil;
import org.apache.flink.table.functions.ScalarFunction;

import java.sql.Timestamp;

/**
 * Created by taosm on 2018/5/31.
 */
public class FlinkUtil {
    public static class modifyDateTimeToGMS8 extends ScalarFunction{
        public String eval(Timestamp timeStamp){
            return DateTimeUtil.modifyDateTimeWithOffset(timeStamp,DateTimeUtil.format1,8*60*60*1000);
        }
    }
}

package com.ccclubs.common.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Administrator on 2017/5/31 0031.
 */
public class DateTimeUtil {
    public static final String format1="yyyy-MM-dd HH:mm:ss";
    public static final String format2="yyyy-MM-dd";
    public static final String format3="yyyy-MM";
    public static final String format4="yyyy-MM-dd HH:mm";
    public static final String format5 = "yyyy/MM/dd HH:mm:ss";

    public static long date2UnixFormat(String dateStr, String format){
        long timeMills=-1;
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            timeMills = sdf.parse(dateStr).getTime();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return timeMills;
    }


    public static String getDateTimeByFormat(long timeMills,String format){
        String retDateTime="";
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            Date date = new Date();
            date.setTime(timeMills);
            retDateTime=sdf.format(date);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return retDateTime;
    }

    public static String modifyDateTimeWithOffset(Timestamp timeStamp,String format,long offset){
        String retDateTime="";
        try{
            if(timeStamp!=null) {
                long timeMills = timeStamp.getTime();
                timeMills+=offset;
                retDateTime = getDateTimeByFormat(timeMills,DateTimeUtil.format1);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return retDateTime;
    }


    public static void main(String[] args) {

    }
}

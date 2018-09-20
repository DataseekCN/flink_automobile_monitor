package com.ccclubs.common.util;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

import java.math.BigDecimal;

/**
 * Created by taosm on 2018/5/15.
 */
public class GeohashUtil {
    //根据经纬度返回GeoHash字符串
    public static String getGeohashCode(double lat,double lon, int precision){
        String geohashCode = null;
        try {
            double[] new_locaton = GPSUtils.gcj02_To_Gps84(lat, lon);
            GeoHash geoHash = GeoHash.withCharacterPrecision(new_locaton[0], new_locaton[1], precision);
            geohashCode = geoHash.toBase32();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return geohashCode;
    }

    //将GeoHash字符串转为经纬度
    public static double[] getLocation(String geohashCode){
        GeoHash geoHash = GeoHash.fromGeohashString(geohashCode);
        WGS84Point point = geoHash.getPoint();
        BigDecimal b_lat = new BigDecimal(point.getLatitude());
        BigDecimal b_lon = new BigDecimal(point.getLongitude());
        double new_lat = b_lat.setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
        double new_lon = b_lon.setScale(6,BigDecimal.ROUND_HALF_UP).doubleValue();
        double[] new_locaton = GPSUtils.gps84_To_Gcj02(new_lat,new_lon);
        return new_locaton;
    }

    public static void main(String[] args) {
        double lat = 34.759342;
        double lon = 111.857143;
        int precision = 6;
        String geohashCode = GeohashUtil.getGeohashCode(lat,lon,precision);
        System.out.println(geohashCode);
//        double[] location = GPSUtils.gcj02_To_Gps84(lat,lon);
//        double[] wtmkmns = GeohashUtil.getLocation("wqpjdr");
        double[] wtmkmns = GeohashUtil.getLocation("wqpvwp");
        System.out.println(wtmkmns[1]+","+wtmkmns[0]);
    }
}

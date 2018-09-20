package com.ccclubs.ca.bean;

import java.io.Serializable;

/**
 * Created by lcy on 2018/5/31.
 */
public class Pace implements Serializable{
    //数据类型
    private String dataType;
    //Vin码
    private String vin;
    //开始时间
    private Long startTime;
    //结束时间
    private Long endTime;
    //消耗时间
    private Long spendTime;
    //开始电量
    private Float startSoc;
    //结束电量
    private Float endSoc;
    //变化电量
    private Float changeSoc;
    //开始OBD里程
    private Float startObdMile;
    //结束OBD里程
    private Float endObdMile;
    //变化OBD里程
    private Float changeObdMile;
    //开始油量
    private Float startOil;
    //结束油量
    private Float endOil;
    //变化油量
    private Float changeOil;

    private Double startLatitude;

    private Double endLatitude;

    private Double startLongitude;

    private Double endLongitude;

    private String startGeoHash;

    private String endGeoHash;

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Float getStartSoc() {
        return startSoc;
    }

    public void setStartSoc(Float startSoc) {
        this.startSoc = startSoc;
    }

    public Float getEndSoc() {
        return endSoc;
    }

    public void setEndSoc(Float endSoc) {
        this.endSoc = endSoc;
    }

    public Float getStartObdMile() {
        return startObdMile;
    }

    public void setStartObdMile(Float startObdMile) {
        this.startObdMile = startObdMile;
    }

    public Float getEndObdMile() {
        return endObdMile;
    }

    public void setEndObdMile(Float endObdMile) {
        this.endObdMile = endObdMile;
    }

    public Float getStartOil() {
        return startOil;
    }

    public void setStartOil(Float startOil) {
        this.startOil = startOil;
    }

    public Float getEndOil() {
        return endOil;
    }

    public void setEndOil(Float endOil) {
        this.endOil = endOil;
    }

    public Long getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(Long spendTime) {
        this.spendTime = spendTime;
    }

    public Float getChangeSoc() {
        return changeSoc;
    }

    public void setChangeSoc(Float changeSoc) {
        this.changeSoc = changeSoc;
    }

    public Float getChangeObdMile() {
        return changeObdMile;
    }

    public void setChangeObdMile(Float changeObdMile) {
        this.changeObdMile = changeObdMile;
    }

    public Float getChangeOil() {
        return changeOil;
    }

    public void setChangeOil(Float changeOil) {
        this.changeOil = changeOil;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public String getStartGeoHash() {
        return startGeoHash;
    }

    public void setStartGeoHash(String startGeoHash) {
        this.startGeoHash = startGeoHash;
    }

    public String getEndGeoHash() {
        return endGeoHash;
    }

    public void setEndGeoHash(String endGeoHash) {
        this.endGeoHash = endGeoHash;
    }
}

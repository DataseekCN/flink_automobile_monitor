package com.ccclubs.ca.bean;

import java.io.Serializable;

/**
 * Created by lcy on 2018/5/31.
 */
public class SocJump implements Serializable{
    //数据类型
    private String dataType;
    //Vin码
    private String vin;
    //车机号
    private String teNumber;
    //开始时间
    private Long beforeTime;
    //结束时间
    private Long afterTime;
    //消耗时间
    private Long spendTime;
    //开始电量
    private Float beforeSoc;
    //结束电量
    private Float afterSoc;
    //变化电量
    private Float changeSoc;
    //3-长安
    private Integer access;

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

    public String getTeNumber() {
        return teNumber;
    }

    public void setTeNumber(String teNumber) {
        this.teNumber = teNumber;
    }

    public Long getBeforeTime() {
        return beforeTime;
    }

    public void setBeforeTime(Long beforeTime) {
        this.beforeTime = beforeTime;
    }

    public Long getAfterTime() {
        return afterTime;
    }

    public void setAfterTime(Long afterTime) {
        this.afterTime = afterTime;
    }

    public Long getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(Long spendTime) {
        this.spendTime = spendTime;
    }

    public Float getBeforeSoc() {
        return beforeSoc;
    }

    public void setBeforeSoc(Float beforeSoc) {
        this.beforeSoc = beforeSoc;
    }

    public Float getAfterSoc() {
        return afterSoc;
    }

    public void setAfterSoc(Float afterSoc) {
        this.afterSoc = afterSoc;
    }

    public Float getChangeSoc() {
        return changeSoc;
    }

    public void setChangeSoc(Float changeSoc) {
        this.changeSoc = changeSoc;
    }

    public Integer getAccess() {
        return access;
    }

    public void setAccess(Integer access) {
        this.access = access;
    }
}

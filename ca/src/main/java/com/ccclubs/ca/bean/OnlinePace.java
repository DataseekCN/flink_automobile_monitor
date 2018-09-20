package com.ccclubs.ca.bean;


import java.math.BigDecimal;

public class OnlinePace{
    //数据类型
    private String dataType;
    //Vin码
    private String vin;

    private String teNumber;
    //开始时间
    private Long startTime;
    //结束时间
    private Long endTime;
    //消耗时间
    private Long spendTime;

    private Integer onlineCount;

    private Integer offlineCount;

    private Long onlineStartTime;

    private Long onlineEndTime;

    private Long onlineSpendTime;

    private Long offlineStartTime;

    private Long offlineEndTime;

    private Long offlineSpendTime;

    /**
     * 网关类型：808,GB,MQTT
     */
    private String gatewayType;

    private Integer count;

    private BigDecimal onlineRate;

    /**
     * true:    在线
     * false:   下线
     */
    private Boolean OnlineStatistics;


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

    public Long getSpendTime() {
        return spendTime;
    }

    public void setSpendTime(Long spendTime) {
        this.spendTime = spendTime;
    }

    public Integer getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(Integer onlineCount) {
        this.onlineCount = onlineCount;
    }

    public Integer getOfflineCount() {
        return offlineCount;
    }

    public void setOfflineCount(Integer offlineCount) {
        this.offlineCount = offlineCount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public BigDecimal getOnlineRate() {
        return onlineRate;
    }

    public void setOnlineRate(BigDecimal onlineRate) {
        this.onlineRate = onlineRate;
    }

    public Long getOnlineStartTime() {
        return onlineStartTime;
    }

    public void setOnlineStartTime(Long onlineStartTime) {
        this.onlineStartTime = onlineStartTime;
    }

    public Long getOnlineEndTime() {
        return onlineEndTime;
    }

    public void setOnlineEndTime(Long onlineEndTime) {
        this.onlineEndTime = onlineEndTime;
    }

    public Long getOnlineSpendTime() {
        return onlineSpendTime;
    }

    public void setOnlineSpendTime(Long onlineSpendTime) {
        this.onlineSpendTime = onlineSpendTime;
    }

    public Long getOfflineStartTime() {
        return offlineStartTime;
    }

    public void setOfflineStartTime(Long offlineStartTime) {
        this.offlineStartTime = offlineStartTime;
    }

    public Long getOfflineEndTime() {
        return offlineEndTime;
    }

    public void setOfflineEndTime(Long offlineEndTime) {
        this.offlineEndTime = offlineEndTime;
    }

    public Long getOfflineSpendTime() {
        return offlineSpendTime;
    }

    public void setOfflineSpendTime(Long offlineSpendTime) {
        this.offlineSpendTime = offlineSpendTime;
    }

    public Boolean getOnlineStatistics() {
        return OnlineStatistics;
    }

    public void setOnlineStatistics(Boolean onlineStatistics) {
        OnlineStatistics = onlineStatistics;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }
}

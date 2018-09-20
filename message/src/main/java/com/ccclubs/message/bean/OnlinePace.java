package com.ccclubs.message.bean;


import java.math.BigDecimal;

public class OnlinePace {
    //Vin码
    private String vin;
    //车机号
    private String teNumber;
    //开始时间
    private Long startTime;
    //结束时间
    private Long endTime;
    //消耗时间
    private Long spendTime;

    private Integer onlineCount;

    private Integer offlineCount;

    private Long onlineSpendTime;

    private Long offlineSpendTime;

    private Integer count;

    private BigDecimal onlineRate;

    private Boolean  onlineStatistics;

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

    public Long getOnlineSpendTime() {
        return onlineSpendTime;
    }

    public void setOnlineSpendTime(Long onlineSpendTime) {
        this.onlineSpendTime = onlineSpendTime;
    }

    public Long getOfflineSpendTime() {
        return offlineSpendTime;
    }

    public void setOfflineSpendTime(Long offlineSpendTime) {
        this.offlineSpendTime = offlineSpendTime;
    }

    public Boolean getOnlineStatistics() {
        return onlineStatistics;
    }

    public void setOnlineStatistics(Boolean onlineStatistics) {
        this.onlineStatistics = onlineStatistics;
    }
}

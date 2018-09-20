package com.ccclubs.ca.bean;


import java.util.List;

public class OnlinePaceV1 {
    //数据类型
    private String dataType;
    //车架号
    private String vin;
    //车机号
    private String teNumber;
    //开始时间
    private Long startTime;
    //结束时间
    private Long endTime;
    //消耗时间
    private Long spendTime;
    //开始GeoHash
    private String startGeoHash;
    //结束GeoHash
    private String endGeoHash;

    /**
     * 手机 卡号
     */
    private String simNo;

    /**
     * 服务端IP
     */
    private String serverIp;

    /**
     * 网关类型：808,GB,MQTT
     */
    private String gatewayType;

    /**
     * true:    在线
     * false:   下线
     */
    private Boolean OnlineStatus;

    /**
     * 车辆下线类型
     * 1: 服务端主动断开（网关检测到错误）
     * 2: 超时断开（终端在某时间段内没有和网关交互）
     * 3: 客户端主动断开（网关检测到客户端主动断开事件）
     */
    private Integer offlineType;

    /**
     * access
     */
    private Short access;

    /**
     * 终端序列号
     */
    private String teNo;

    /**
     * 终端批次 XXXX-X
     */
    private String batchNo;

    /**
     * 终端类型
     */
    private Byte teType;

    /**
     * 终端型号
     */
    private String teModelNo;

    /**
     * ICCID
     */
    private String iccid;

    /**
     * 车型
     */
    private Integer carModel;

    /**
     * 状态数据前溯30条
     */
    private List<StateDTO> states;

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

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

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
    }

    public Boolean getOnlineStatus() {
        return OnlineStatus;
    }

    public void setOnlineStatus(Boolean onlineStatus) {
        OnlineStatus = onlineStatus;
    }

    public Integer getOfflineType() {
        return offlineType;
    }

    public void setOfflineType(Integer offlineType) {
        this.offlineType = offlineType;
    }

    public Short getAccess() {
        return access;
    }

    public void setAccess(Short access) {
        this.access = access;
    }

    public String getTeNo() {
        return teNo;
    }

    public void setTeNo(String teNo) {
        this.teNo = teNo;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public Byte getTeType() {
        return teType;
    }

    public void setTeType(Byte teType) {
        this.teType = teType;
    }

    public String getTeModelNo() {
        return teModelNo;
    }

    public void setTeModelNo(String teModelNo) {
        this.teModelNo = teModelNo;
    }

    public Integer getCarModel() {
        return carModel;
    }

    public void setCarModel(Integer carModel) {
        this.carModel = carModel;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public List<StateDTO> getStates() {
        return states;
    }

    public void setStates(List<StateDTO> states) {
        this.states = states;
    }
}

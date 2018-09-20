package com.ccclubs.ca.bean;

import com.alibaba.fastjson.JSONArray;

import java.io.Serializable;
import java.util.List;

/**
 * 上下线事件及状态数据
 *
 * @author jianghaiyang
 * @create 2018-07-03
 **/
public class OnlineStateEventDTO implements Serializable {
    /**
     * 车辆vin码
     */
    private String vin;

    /**
     * 手机 卡号
     */
    private String simNo;

    /**
     * 车机号
     */
    private String teNumber;

    /**
     * 服务端IP
     */
    private String serverIp;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 车辆在线状态通知
     * true:    在线
     * false:   下线
     */
    private boolean online;

    /**
     * 发送该通知的时间戳
     */
    private Long timestamp;

    /**
     * 网关类型：808,GB,MQTT
     */
    private String gatewayType;

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
//    private List<StateDTO> states;
    private JSONArray states;

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getSimNo() {
        return simNo;
    }

    public void setSimNo(String simNo) {
        this.simNo = simNo;
    }

    public String getTeNumber() {
        return teNumber;
    }

    public void setTeNumber(String teNumber) {
        this.teNumber = teNumber;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getGatewayType() {
        return gatewayType;
    }

    public void setGatewayType(String gatewayType) {
        this.gatewayType = gatewayType;
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

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public Integer getOfflineType() {
        return offlineType;
    }

    public void setOfflineType(Integer offlineType) {
        this.offlineType = offlineType;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public JSONArray getStates() {
        return states;
    }

    public void setStates(JSONArray states) {
        this.states = states;
    }

    public Integer getCarModel() {
        return carModel;
    }

    public void setCarModel(Integer carModel) {
        this.carModel = carModel;
    }
}

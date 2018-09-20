package com.ccclubs.ca.bean;

/**
 * Created by lcy on 2018/6/30.
 */
public class OnlineState implements Comparable<OnlineState> {
    private String vin;

    private String teNumber;
    private Long timestamp;
    private Boolean onlineStatus;
    //服务端Ip
    private String serverIp;
    //网关类型
    private String gatewayType;
    //终端类型
    private String teType;
    //终端型号
    private String teModel;
    //车型
    private Integer csvModel;
    //经纬度
    private Double longitude;
    private Double latitude;
    private Float powerReserve;
    private Float soc;
    private Float speed;
    private Integer gpsValid;
    private String geoHash;

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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(Boolean onlineStatus) {
        this.onlineStatus = onlineStatus;
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

    public String getTeType() {
        return teType;
    }

    public void setTeType(String teType) {
        this.teType = teType;
    }

    public String getTeModel() {
        return teModel;
    }

    public void setTeModel(String teModel) {
        this.teModel = teModel;
    }

    public Integer getCsvModel() {
        return csvModel;
    }

    public void setCsvModel(Integer csvModel) {
        this.csvModel = csvModel;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Float getPowerReserve() {
        return powerReserve;
    }

    public void setPowerReserve(Float powerReserve) {
        this.powerReserve = powerReserve;
    }

    public Float getSoc() {
        return soc;
    }

    public void setSoc(Float soc) {
        this.soc = soc;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Integer getGpsValid() {
        return gpsValid;
    }

    public void setGpsValid(Integer gpsValid) {
        this.gpsValid = gpsValid;
    }

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

    @Override
    public int compareTo(OnlineState other) {
        return Long.compare(this.timestamp, other.getTimestamp());
    }
}

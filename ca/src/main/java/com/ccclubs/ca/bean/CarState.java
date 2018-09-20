package com.ccclubs.ca.bean;


import com.alibaba.fastjson.annotation.JSONField;
import org.apache.flink.table.shaded.org.joda.time.DateTime;

import java.io.Serializable;

public class CarState implements Serializable {
    private String vin;

    private Long currentTime;

    private DateTime machineTime;

    private String teNumber;

    private String teNo;

    private String iccid;

    private String mobile;

    private Integer access;

    private Long addTime;

    private Integer rentFlg;

    private String warnCode;

    private String rfid;

    private String userRfid;

    private Float obdMiles;

    private Float engineTempe;

    private Float totalMiles;

    private Float speed;

    private Float motorSpeed;

    private Float oilCost;

    private Float powerReserve;

    private Float evBattery;

    private Integer chargingStatus;

    private Float fuelMiles;

    private Float elecMiles;

    private Float endurMiles;

    private Float tempe;

    private Integer gpsNum;

    private Integer gpsStrength;

    private Integer gpsValid;

    private Integer netStrength;

    private Double longitude;

    private Double latitude;

    private String geoHash;

    private Float directionAngle;

    private Integer circularMode;

    private Integer ptcStatus;

    private Integer compreStatus;

    private Integer fanMode;

    private Integer savingMode;

    private Integer doorStatus;

    private Integer engineStatus;

    private Integer keyStatus;

    private Integer lightStatus;

    private Integer lockStatus;

    private String netType;

    private String baseLac;

    private String baseCi;

    private String curOrder;

    private Integer gear;

    private Integer autopilotStatus;

    private Integer handbrakeStatus;

    private String sourceHex;

    private Integer year;

    private Integer month;

    private Integer day;

    public String getGeoHash() {
        return geoHash;
    }

    public void setGeoHash(String geoHash) {
        this.geoHash = geoHash;
    }

//    @JSONField(name = "vin")
    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

//    @JSONField(name = "current_time")
    public Long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Long currentTime) {
        this.currentTime = currentTime;
    }

//    @JSONField(name = "te_number")
    public String getTeNumber() {
        return teNumber;
    }

    public void setTeNumber(String teNumber) {
        this.teNumber = teNumber;
    }

//    @JSONField(name = "te_no")
    public String getTeNo() {
        return teNo;
    }

    public void setTeNo(String teNo) {
        this.teNo = teNo;
    }

//    @JSONField(name = "iccid")
    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

//    @JSONField(name = "mobile")
    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

//    @JSONField(name = "access")
    public Integer getAccess() {
        return access;
    }

    public void setAccess(Integer access) {
        this.access = access;
    }

//    @JSONField(name = "add_time")
    public Long getAddTime() {
        return addTime;
    }

    public void setAddTime(Long addTime) {
        this.addTime = addTime;
    }

//    @JSONField(name = "rent_flg")
    public Integer getRentFlg() {
        return rentFlg;
    }

    public void setRentFlg(Integer rentFlg) {
        this.rentFlg = rentFlg;
    }

//    @JSONField(name = "warn_code")
    public String getWarnCode() {
        return warnCode;
    }

    public void setWarnCode(String warnCode) {
        this.warnCode = warnCode;
    }

//    @JSONField(name = "rfid")
    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

//    @JSONField(name = "user_rfid")
    public String getUserRfid() {
        return userRfid;
    }

    public void setUserRfid(String userRfid) {
        this.userRfid = userRfid;
    }

//    @JSONField(name = "obd_miles")
    public Float getObdMiles() {
        return obdMiles;
    }

    public void setObdMiles(Float obdMiles) {
        this.obdMiles = obdMiles;
    }

//    @JSONField(name = "engine_tempe")
    public Float getEngineTempe() {
        return engineTempe;
    }

    public void setEngineTempe(Float engineTempe) {
        this.engineTempe = engineTempe;
    }

//    @JSONField(name = "total_miles")
    public Float getTotalMiles() {
        return totalMiles;
    }

    public void setTotalMiles(Float totalMiles) {
        this.totalMiles = totalMiles;
    }

//    @JSONField(name = "speed")
    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

//    @JSONField(name = "motor_speed")
    public Float getMotorSpeed() {
        return motorSpeed;
    }

    public void setMotorSpeed(Float motorSpeed) {
        this.motorSpeed = motorSpeed;
    }

//    @JSONField(name = "oil_cost")
    public Float getOilCost() {
        return oilCost;
    }

    public void setOilCost(Float oilCost) {
        this.oilCost = oilCost;
    }

//    @JSONField(name = "power_reserve")
    public Float getPowerReserve() {
        return powerReserve;
    }

    public void setPowerReserve(Float powerReserve) {
        this.powerReserve = powerReserve;
    }

//    @JSONField(name = "ev_battery")
    public Float getEvBattery() {
        return evBattery;
    }

    public void setEvBattery(Float evBattery) {
        this.evBattery = evBattery;
    }

//    @JSONField(name = "charging_status")
    public Integer getChargingStatus() {
        return chargingStatus;
    }

    public void setChargingStatus(Integer chargingStatus) {
        this.chargingStatus = chargingStatus;
    }

//    @JSONField(name = "fuel_miles")
    public Float getFuelMiles() {
        return fuelMiles;
    }

    public void setFuelMiles(Float fuelMiles) {
        this.fuelMiles = fuelMiles;
    }

//    @JSONField(name = "elec_miles")
    public Float getElecMiles() {
        return elecMiles;
    }

    public void setElecMiles(Float elecMiles) {
        this.elecMiles = elecMiles;
    }

//    @JSONField(name = "endur_miles")
    public Float getEndurMiles() {
        return endurMiles;
    }

    public void setEndurMiles(Float endurMiles) {
        this.endurMiles = endurMiles;
    }

//    @JSONField(name = "tempe")
    public Float getTempe() {
        return tempe;
    }

    public void setTempe(Float tempe) {
        this.tempe = tempe;
    }

//    @JSONField(name = "gps_num")
    public Integer getGpsNum() {
        return gpsNum;
    }

    public void setGpsNum(Integer gpsNum) {
        this.gpsNum = gpsNum;
    }

//    @JSONField(name = "gps_strength")
    public Integer getGpsStrength() {
        return gpsStrength;
    }

    public void setGpsStrength(Integer gpsStrength) {
        this.gpsStrength = gpsStrength;
    }

//    @JSONField(name = "gps_valid")
    public Integer getGpsValid() {
        return gpsValid;
    }

    public void setGpsValid(Integer gpsValid) {
        this.gpsValid = gpsValid;
    }

//    @JSONField(name = "net_strength")
    public Integer getNetStrength() {
        return netStrength;
    }

    public void setNetStrength(Integer netStrength) {
        this.netStrength = netStrength;
    }

//    @JSONField(name = "longitude")
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

//    @JSONField(name = "latitude")
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

//    @JSONField(name = "direction_angle")
    public Float getDirectionAngle() {
        return directionAngle;
    }

    public void setDirectionAngle(Float directionAngle) {
        this.directionAngle = directionAngle;
    }

//    @JSONField(name = "circular_mode")
    public Integer getCircularMode() {
        return circularMode;
    }

    public void setCircularMode(Integer circularMode) {
        this.circularMode = circularMode;
    }

//    @JSONField(name = "ptc_status")
    public Integer getPtcStatus() {
        return ptcStatus;
    }

    public void setPtcStatus(Integer ptcStatus) {
        this.ptcStatus = ptcStatus;
    }

//    @JSONField(name = "compre_status")
    public Integer getCompreStatus() {
        return compreStatus;
    }

    public void setCompreStatus(Integer compreStatus) {
        this.compreStatus = compreStatus;
    }

//    @JSONField(name = "fan_mode")
    public Integer getFanMode() {
        return fanMode;
    }

    public void setFanMode(Integer fanMode) {
        this.fanMode = fanMode;
    }

//    @JSONField(name = "saving_mode")
    public Integer getSavingMode() {
        return savingMode;
    }

    public void setSavingMode(Integer savingMode) {
        this.savingMode = savingMode;
    }

//    @JSONField(name = "door_status")
    public Integer getDoorStatus() {
        return doorStatus;
    }

    public void setDoorStatus(Integer doorStatus) {
        this.doorStatus = doorStatus;
    }

//    @JSONField(name = "engine_status")
    public Integer getEngineStatus() {
        return engineStatus;
    }

    public void setEngineStatus(Integer engineStatus) {
        this.engineStatus = engineStatus;
    }

//    @JSONField(name = "key_status")
    public Integer getKeyStatus() {
        return keyStatus;
    }

    public void setKeyStatus(Integer keyStatus) {
        this.keyStatus = keyStatus;
    }

//    @JSONField(name = "light_status")
    public Integer getLightStatus() {
        return lightStatus;
    }

    public void setLightStatus(Integer lightStatus) {
        this.lightStatus = lightStatus;
    }

//    @JSONField(name = "lock_status")
    public Integer getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(Integer lockStatus) {
        this.lockStatus = lockStatus;
    }

//    @JSONField(name = "net_type")
    public String getNetType() {
        return netType;
    }

    public void setNetType(String netType) {
        this.netType = netType;
    }

//    @JSONField(name = "base_lac")
    public String getBaseLac() {
        return baseLac;
    }

    public void setBaseLac(String baseLac) {
        this.baseLac = baseLac;
    }

//    @JSONField(name = "base_ci")
    public String getBaseCi() {
        return baseCi;
    }

    public void setBaseCi(String baseCi) {
        this.baseCi = baseCi;
    }

//    @JSONField(name = "cur_order")
    public String getCurOrder() {
        return curOrder;
    }

    public void setCurOrder(String curOrder) {
        this.curOrder = curOrder;
    }

//    @JSONField(name = "gear")
    public Integer getGear() {
        return gear;
    }

    public void setGear(Integer gear) {
        this.gear = gear;
    }

//    @JSONField(name = "autopilot_status")
    public Integer getAutopilotStatus() {
        return autopilotStatus;
    }

    public void setAutopilotStatus(Integer autopilotStatus) {
        this.autopilotStatus = autopilotStatus;
    }

//    @JSONField(name = "handbrake_status")
    public Integer getHandbrakeStatus() {
        return handbrakeStatus;
    }

    public void setHandbrakeStatus(Integer handbrakeStatus) {
        this.handbrakeStatus = handbrakeStatus;
    }

//    @JSONField(name = "source_hex")
    public String getSourceHex() {
        return sourceHex;
    }

    public void setSourceHex(String sourceHex) {
        this.sourceHex = sourceHex;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public DateTime getMachineTime() {
        return machineTime;
    }

    public void setMachineTime(DateTime machineTime) {
        this.machineTime = machineTime;
    }

    @Override
    public String toString() {
        return "CarState{" +
                "vin='" + vin + '\'' +
                ", currentTime=" + currentTime +
                ", obdMiles=" + obdMiles +
                ", speed=" + speed +
                ", oilCost=" + oilCost +
                ", evBattery=" + evBattery +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", geoHash='" + geoHash + '\'' +
                ", gpsValid='" + gpsValid + '\'' +
                ", engineStatus=" + engineStatus +
                '}';
    }
}

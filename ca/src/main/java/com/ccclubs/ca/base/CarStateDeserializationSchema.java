package com.ccclubs.ca.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.common.util.GeohashUtil;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;

import java.io.IOException;

/**
 * Created by lcy on 2018/6/25.
 */
public class CarStateDeserializationSchema implements KeyedDeserializationSchema<CarState> {

    @Override
    public CarState deserialize(byte[] messageKey, byte[] message, String topic, int partition, long offset) throws IOException {
        CarState logState = null;
        try {
            String message_str = new String(message);
            JSONObject jsonObject = JSON.parseObject(message_str);
            String vin = jsonObject.getString("cssVin");
            String teNumber = jsonObject.getString("cssNumber");
            Long currentTime = jsonObject.getLong("cssCurrentTime");
            Integer engineStatus = jsonObject.getInteger("cssEngine");
            Integer chargingStatus = jsonObject.getInteger("cssCharging");
            Double longitude = jsonObject.getDouble("cssLongitude");
            Double latitude = jsonObject.getDouble("cssLatitude");
            Float soc = jsonObject.getFloat("cssEvBattery");
            Float speed = jsonObject.getFloat("cssSpeed");
            Float obdMiles = jsonObject.getFloat("cssObdMile");
            Float oilCost = jsonObject.getFloat("cssOil");
            Integer access = jsonObject.getInteger("cssAccess");
            Float powerReserve = jsonObject.getFloat("cssPower");
            String curOrder = jsonObject.getString("cssOrder");
            String geoHash = null;
            if (latitude != null && longitude != null && latitude > 0 && longitude > 0) {
                geoHash = GeohashUtil.getGeohashCode(latitude, longitude, 7);
            }

            logState = new CarState();
            logState.setVin(vin);
            logState.setTeNumber(teNumber);
            logState.setCurrentTime(currentTime);
            logState.setEngineStatus(engineStatus);
            logState.setChargingStatus(chargingStatus);
            logState.setLongitude(longitude);
            logState.setLatitude(latitude);
            logState.setEvBattery(soc);
            logState.setSpeed(speed);
            logState.setObdMiles(obdMiles);
            logState.setOilCost(oilCost);
            logState.setAccess(access);
            logState.setPowerReserve(powerReserve);
            logState.setCurOrder(curOrder);
            logState.setGeoHash(geoHash);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return logState;
    }

    @Override
    public boolean isEndOfStream(CarState carState) {
        return false;
    }

    @Override
    public TypeInformation<CarState> getProducedType() {
        return TypeInformation.of(CarState.class);
    }
}

package com.ccclubs.ca.streaming.business.activity.pace;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.Pace;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

public class PaceProcessFunction extends ProcessFunction<CarState, Pace> {
    private transient ValueState<Pace> paceValueState = null;
    private transient ValueState<Long> sendTime = null;
    private String dataType;

    public PaceProcessFunction(String dataType) {
        this.dataType = dataType;
    }

    @Override
    public void processElement(CarState carState, Context context, Collector<Pace> collector) throws Exception {
        String vin = carState.getVin();
        Long currentTime = carState.getCurrentTime();
        Float evBattery = carState.getEvBattery();
        Float obdMiles = carState.getObdMiles();
        Float oilCost = carState.getOilCost();
        Double longitude = carState.getLongitude();
        Double latitude = carState.getLatitude();
        String geoHash = carState.getGeoHash();

        Pace value = paceValueState.value();
        if (value == null) {
            value = new Pace();
            value.setDataType(dataType);
            value.setVin(vin);
            value.setStartTime(currentTime);
            value.setStartSoc(evBattery);
            value.setStartObdMile(obdMiles);
            value.setStartOil(oilCost);
            value.setStartLatitude(latitude);
            value.setStartLongitude(longitude);
            value.setStartGeoHash(geoHash);
            value.setEndTime(currentTime);
            value.setEndSoc(evBattery);
            value.setEndObdMile(obdMiles);
            value.setEndOil(oilCost);
            value.setEndLatitude(latitude);
            value.setEndLongitude(longitude);
            value.setEndGeoHash(geoHash);
        } else {
            Long startTime = value.getStartTime();
            Long endTime = value.getEndTime();
            if (startTime > currentTime) {
                value.setStartTime(currentTime);
                value.setStartSoc(evBattery);
                value.setStartObdMile(obdMiles);
                value.setStartOil(oilCost);
                value.setStartLatitude(latitude);
                value.setStartLongitude(longitude);
                value.setStartGeoHash(geoHash);
            } else if (endTime < currentTime) {
                value.setEndTime(currentTime);
                value.setEndSoc(evBattery);
                value.setEndObdMile(obdMiles);
                value.setEndOil(oilCost);
                value.setEndLatitude(latitude);
                value.setEndLongitude(longitude);
                value.setEndGeoHash(geoHash);
            }
        }
        Long spendTime = value.getEndTime() - value.getStartTime();
        value.setSpendTime(spendTime);
        if ((spendTime != 0) && (spendTime > 2 * 60 * 1000)) {
            Long sendMills = sendTime.value();
            if ((sendMills == null) || (currentTime - sendMills >= 60000)) {
                Float startSoc = value.getStartSoc();
                Float endSoc = value.getEndSoc();
                if (startSoc != null && endSoc != null) {
                    value.setChangeSoc(Math.abs(endSoc - startSoc));
                }
                Float startObdMiles = value.getStartObdMile();
                Float endObdMiles = value.getEndObdMile();
                if (startObdMiles != null && endObdMiles != null) {
                    value.setChangeObdMile(endObdMiles - startObdMiles);
                }
                Float startOilCost = value.getStartOil();
                Float endOilCost = value.getEndOil();
                if (startOilCost != null && endOilCost != null) {
                    value.setChangeOil(Math.abs(endOilCost - startOilCost));
                }
                collector.collect(value);
                sendTime.update(currentTime);
            }
        }
        paceValueState.update(value);
        context.timerService().registerEventTimeTimer(value.getEndTime() + (5 * 60 * 1000));
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<Pace> out) throws Exception {
        Pace value = paceValueState.value();
        Long sendMills = sendTime.value();
        if (value != null) {
            if ((sendMills == null) && (value.getSpendTime() > 2 * 60 * 1000)) {
                Float startSoc = value.getStartSoc();
                Float endSoc = value.getEndSoc();
                if (startSoc != null && endSoc != null) {
                    value.setChangeSoc(Math.abs(endSoc - startSoc));
                }
                Float startObdMiles = value.getStartObdMile();
                Float endObdMiles = value.getEndObdMile();
                if (startObdMiles != null && endObdMiles != null) {
                    value.setChangeObdMile(endObdMiles - startObdMiles);
                }
                Float startOilCost = value.getStartOil();
                Float endOilCost = value.getEndOil();
                if (startOilCost != null && endOilCost != null) {
                    value.setChangeOil(Math.abs(endOilCost - startOilCost));
                }
                out.collect(value);

            } else if ((sendMills != null) && (sendMills != value.getEndTime())) {
                Float startSoc = value.getStartSoc();
                Float endSoc = value.getEndSoc();
                if (startSoc != null && endSoc != null) {
                    value.setChangeSoc(Math.abs(endSoc - startSoc));
                }
                Float startObdMiles = value.getStartObdMile();
                Float endObdMiles = value.getEndObdMile();
                if (startObdMiles != null && endObdMiles != null) {
                    value.setChangeObdMile(endObdMiles - startObdMiles);
                }
                Float startOilCost = value.getStartOil();
                Float endOilCost = value.getEndOil();
                if (startOilCost != null && endOilCost != null) {
                    value.setChangeOil(Math.abs(endOilCost - startOilCost));
                }
                out.collect(value);
            }
        }
        sendTime.clear();
        paceValueState.clear();
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        paceValueState = getRuntimeContext().getState(new ValueStateDescriptor<>(dataType.toLowerCase() + " pace", Pace.class));
        sendTime = getRuntimeContext().getState(new ValueStateDescriptor<>(dataType.toLowerCase() + " send time", Long.class));
    }
}

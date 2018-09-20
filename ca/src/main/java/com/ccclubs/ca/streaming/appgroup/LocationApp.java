package com.ccclubs.ca.streaming.appgroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.bean.GeoBean;
import com.ccclubs.ca.bean.Pace;
import com.ccclubs.ca.streaming.business.activity.pace.ChargePaceFunction;
import com.ccclubs.ca.streaming.business.activity.pace.DrivePaceFunction;
import com.ccclubs.ca.streaming.business.activity.pace.LaunchPaceFunction;
import com.ccclubs.ca.streaming.business.energy.jump.SocJumpFunction;
import com.ccclubs.ca.streaming.business.location.LocationFunction;
import com.ccclubs.ca.util.BizConstant;
import com.ccclubs.common.util.GeohashUtil;
import com.ccclubs.common.util.PropertiesHelper;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.api.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by taosm on 2018/5/31.
 */
public class LocationApp {
    private static Logger logger = Logger.getLogger(LocationApp.class);

    public static void main(String[] args) throws Exception {
        Logger.getLogger("org").setLevel(Level.ERROR);
        System.setProperty("user.timezone", "Asia/Shanghai");
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        env.enableCheckpointing(60000);

        Properties consumerProps = new Properties();

        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();

        consumerProps.setProperty("bootstrap.servers", propertiesHelper.getValue(BizConstant.ACCEPT_BROKER));
        consumerProps.setProperty("enable.auto.commit", "true");
        consumerProps.setProperty("group.id", propertiesHelper.getValue(BizConstant.GEO_INFO_GROUP_ID));

        FlinkKafkaConsumer011<String> myConsumer =
                new FlinkKafkaConsumer011<String>(java.util.regex.Pattern.compile(propertiesHelper.getValue(BizConstant.ACCEPT_TOPIC)), new SimpleStringSchema(), consumerProps);
        myConsumer.setStartFromLatest();

        FlinkKafkaProducer011<Row> myProducer =
                new FlinkKafkaProducer011<Row>(propertiesHelper.getValue(BizConstant.SEND_BROKER), BizConstant.SEND_DEFAULT_TOPIC, new LocationKeyedSchema());

        DataStream<String> originStream = env.addSource(myConsumer);
        DataStream<CarState> carStateDataStream = originStream.map(new LocationMapFunction());

        LocationFunction locationFunction = new LocationFunction();
        DataStream<Row> locationMain = locationFunction.Main(carStateDataStream, tableEnv);

        locationMain.addSink(myProducer);
        env.execute("run map");
    }

    static class LocationKeyedSchema implements KeyedSerializationSchema<Row> {

        @Override
        public byte[] serializeKey(Row row) {
            return row.getField(0).toString().getBytes();
        }

        @Override
        public byte[] serializeValue(Row row) {
            GeoBean geoBean = new GeoBean();
            String geoHash = row.getField(0).toString();
            String updateDateTime = row.getField(1).toString();
            geoBean.setDataType("GeoInfo");
            geoBean.setGeoHash(geoHash);
            geoBean.setUpdateDateTime(updateDateTime);
            return JSON.toJSONString(geoBean).getBytes();
        }

        @Override
        public String getTargetTopic(Row row) {
            return BizConstant.GEO_INFO_TOPIC;
        }
    }


    static class LocationMapFunction implements MapFunction<String, CarState> {
        @Override
        public CarState map(String line) throws Exception {
            JSONObject jsonObject = null;
            CarState carState = null;
            try {
                jsonObject = JSON.parseObject(line);
                carState = new CarState();
                String cssVin = jsonObject.getString("cssVin");
                Long cssCurrentTime = jsonObject.getLong("cssCurrentTime");
                Integer cssEngine = jsonObject.getInteger("cssEngine");
                Double cssLongitude = jsonObject.getDouble("cssLongitude");
                Double cssLatitude = jsonObject.getDouble("cssLatitude");
                Integer cssGpsValid = jsonObject.getInteger("cssGpsValid");
                String geoHash = null;
                if (cssLatitude != null && cssLongitude != null && cssLatitude > 0 && cssLongitude > 0) {
                    geoHash = GeohashUtil.getGeohashCode(cssLatitude, cssLongitude, 6);
                }

                carState.setVin(cssVin);
                carState.setCurrentTime(cssCurrentTime);
                carState.setEngineStatus(cssEngine);
                carState.setLongitude(cssLongitude);
                carState.setLatitude(cssLatitude);
                carState.setGpsValid(cssGpsValid);
                carState.setGeoHash(geoHash);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
            return carState;
        }
    }
}

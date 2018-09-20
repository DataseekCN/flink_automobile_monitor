package com.ccclubs.message.util;

/**
 * Created by taosm on 2018/6/4.
 */
public class MessageConst {
    //kafka相关配置

    public static final String KAFKA_BROKERS_KEY="kafka.send.own.brokers";

    public static final String KAFKA_INTERNAL_BROKERS_KEY="kafka.send.prod.brokers";
    //阶段数据统计topic
    public static final String KAFKA_PACE_TOPIC_KEY="kafka.pace.topic";

    public static final String KAFKA_LOCATION_TOPIC_KEY="kafka.location.topic";

    public static final String KAFKA_CA_BROKERS_KEY="kafka.accept.brokers";

    public static final String KAFKA_CA_TOPIC_KEY="kafka.accept.ca.state.topic";

    public static final String KAFKA_ONLINE_TOPIC_KEY="kafka.network.topic";

    //阶段区分关键字(充电阶段)
    public static final String PACE_CHARGEPACE="ChargePace";
    //阶段区分关键字(驾驶阶段)
    public static final String PACE_DRIVEPACE="DrivePace";
    //阶段区分关键字(启停阶段)
    public static final String PACE_LAUNCHPACE="LaunchPace";
    //阶段区分关键字(在线阶段)
    public static final String PACE_ONLINEPACE="OnlineRate";
    //电池跳变 关键字
    public static final String BATTERY_SOCJUMP="SocJump";
    //地图数据 关键字
    public static final String LOCATION_KEY="GeoInfo";
}

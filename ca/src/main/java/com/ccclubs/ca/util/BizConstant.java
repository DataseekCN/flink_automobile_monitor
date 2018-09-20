package com.ccclubs.ca.util;

/**
 * Created by lcy on 2018/5/31.
 */
public class BizConstant {
    //电池变化阈值
    public static final Integer SOC_JUMP_THRESHOLD = 10;
    //电池跳变最大统计时间间隔
    public static final Integer SOC_JUMP_TIME_OUT = 600000;
    //最大延迟时间
    public static final Long MAX_OUT_OF_ORDERNESS = 300000L;
    //启停阶段时间间隔阈值
    public static final Integer LAUNCH_PACE_THRESHOLD = 5;

    public static final Integer DRIVE_PACE_THRESHOLD = 5;

    public static final Integer CHARGE_PACE_THRESHOLD = 5;
    //废弃阶段
    public static final Integer DISCARD_PACE = 120000;

    /**
     * Kafka配置
     */

    /**
     * 接收Kafka
     */
    public static final String ACCEPT_TOPIC = "kafka.accept.ca.state.topic";

    public static final String BUSINESS_GROUP_ID = "kafka.group.business";

    public static final String ENERGY_GROUP_ID = "kafka.group.business";

    public static final String NETWORK_GROUP_ID = "kafka.group.network";

    public static final String GEO_INFO_GROUP_ID = "kafka.group.geo";

    //外网
    public static final String ACCEPT_BROKER = "kafka.accept.brokers";

    public static final String ACCEPT_ZK = "kafka.accept.zk";

    //内网
    public static final String INTRANET_ACCEPT_BROKER = "kafka.accept.brokers";

    public static final String INTRANET_ACCEPT_ZK = "kafka.accept.zk";
    /**
     *  发送kafka
     */
    public static final String SEND_BROKER = "kafka.send.own.brokers";
    //内网
    public static final String INTRANET_SEND_BROKER = "kafka.send.prod.brokers";
    //电池跳变发送Kakfa
    public static final String ENERGY_JUMP_TOPIC = "topic_stream_energy_jump";

    public static final String ACTIVITY_PACE_TOPIC = "topic_stream_activity_pace";

    public static final String TELECOM_NETWORK_TOPIC = "topic_stream_telecom_network";

    public static final String GEO_INFO_TOPIC = "topic_stream_geo_info";

    public static final String SEND_DEFAULT_TOPIC = "topic_stream_default";

}

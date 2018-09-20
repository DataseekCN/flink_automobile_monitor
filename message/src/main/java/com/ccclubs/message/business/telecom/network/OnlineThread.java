package com.ccclubs.message.business.telecom.network;

import org.apache.log4j.Logger;

/**
 * Created by taosm on 2018/6/6.
 */
public class OnlineThread implements Runnable {
    private static Logger logger = Logger.getLogger(OnlineThread.class);
    @Override
    public void run() {
        logger.info("OnlineThread启动");
        OnlineConsumerService onlineConsumerService = OnlineConsumerService.getInstance();
        onlineConsumerService.doConsumer();
    }
}

package com.ccclubs.message.business.realtime.status;

import org.apache.log4j.Logger;

/**
 * Created by taosm on 2018/6/6.
 */
public class StatusThread implements Runnable {
    private static Logger logger = Logger.getLogger(StatusThread.class);
    @Override
    public void run() {
        logger.info("StatusThread启动");
        StatusConsumerService statusConsumerService = StatusConsumerService.getInstance();
        statusConsumerService.doConsumer();
    }
}

package com.ccclubs.message.business.activity.pace;

import org.apache.log4j.Logger;

/**
 * Created by taosm on 2018/6/6.
 */
public class PaceThread implements Runnable {
    private static Logger logger = Logger.getLogger(PaceThread.class);
    @Override
    public void run() {
        logger.info("PaceThread启动");
        PaceConsumerService paceConsumerService = PaceConsumerService.getInstance();
        paceConsumerService.doConsumer();
    }
}

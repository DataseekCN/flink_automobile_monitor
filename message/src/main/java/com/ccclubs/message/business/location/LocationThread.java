package com.ccclubs.message.business.location;

import org.apache.log4j.Logger;

/**
 * Created by taosm on 2018/6/6.
 */
public class LocationThread implements Runnable {
    private static Logger logger = Logger.getLogger(LocationThread.class);
    @Override
    public void run() {
        logger.info("LocationThread启动");
        LocationConsumerService locationConsumerService = LocationConsumerService.getInstance();
        locationConsumerService.doConsumer();
    }
}

package com.ccclubs.message.app;

import com.ccclubs.message.business.activity.pace.PaceThread;
import com.ccclubs.message.business.location.LocationThread;
import com.ccclubs.message.business.realtime.status.StatusThread;
import com.ccclubs.message.business.telecom.network.OnlineThread;

/**
 * Created by taosm on 2018/6/6.
 */
public class ConsumerApp {
    public static void main(String[] args) {
        Thread paceThread = new Thread(new PaceThread());
        Thread locationThread = new Thread(new LocationThread());
        Thread onlineThread = new Thread(new OnlineThread());
        Thread statusThread = new Thread(new StatusThread());
        paceThread.start();
        locationThread.start();
        onlineThread.start();
        statusThread.start();
    }
}

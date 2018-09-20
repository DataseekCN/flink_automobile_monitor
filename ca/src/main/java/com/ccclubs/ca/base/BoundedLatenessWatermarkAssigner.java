package com.ccclubs.ca.base;

import com.ccclubs.ca.bean.CarState;
import com.ccclubs.ca.util.BizConstant;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;

import javax.annotation.Nullable;

/**
 * Created by lcy on 2018/6/25.
 */
public class BoundedLatenessWatermarkAssigner implements AssignerWithPeriodicWatermarks<CarState> {
    private long currentMaxTimeMills = -1L;
    private long maxOutOfOrderness = BizConstant.MAX_OUT_OF_ORDERNESS;

    @Nullable
    @Override
    public Watermark getCurrentWatermark() {
        return new Watermark(currentMaxTimeMills - maxOutOfOrderness);
    }

    @Override
    public long extractTimestamp(CarState message, long l) {
        Long timeMills = message.getCurrentTime();
        currentMaxTimeMills = Math.max(timeMills, currentMaxTimeMills);
        return timeMills;
    }
}

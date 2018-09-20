package com.ccclubs.storage.redis;

import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.storage.util.StorageConst;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Pipeline;

import java.util.*;

/**
 * Created by taosm on 2018/6/1.
 */
public class RedisTool {
    private static RedisTool redisTool = null;
    private static JedisPool jedisPool = null;
    private static Logger logger = Logger.getLogger(RedisTool.class);

    private RedisTool() {

    }

    public synchronized static RedisTool getInstance() {
        if (redisTool == null) {
            redisTool = new RedisTool();
            redisTool.doInit();
        }
        return redisTool;
    }

    private void doInit() {
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String REDIS_HOST = propertiesHelper.getValue(StorageConst.REDIS_HOST_KEY);
        String REDIS_PORT = propertiesHelper.getValue(StorageConst.REDIS_PORT_KEY);
        String REDIS_PASS = propertiesHelper.getValue(StorageConst.REDIS_PASS_KEY);
        String REDIS_TIMEOUT = propertiesHelper.getValue(StorageConst.REDIS_TIMEOUT_KEY);
        String REDIS_MAXIDLE = propertiesHelper.getValue(StorageConst.REDIS_MAXIDLE_KEY);
        String REDIS_MAXTOTAL = propertiesHelper.getValue(StorageConst.REDIS_MAXTOTAL_KEY);
        String REDIS_MAXWAITMILLIS = propertiesHelper.getValue(StorageConst.REDIS_MAXWAITMILLIS_KEY);
        String REDIS_TESTONBORROW = propertiesHelper.getValue(StorageConst.REDIS_TESTONBORROW_KEY);

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(Integer.parseInt(REDIS_MAXTOTAL));
        config.setMaxIdle(Integer.parseInt(REDIS_MAXIDLE));
        config.setMaxWaitMillis(Long.parseLong(REDIS_MAXWAITMILLIS));
        config.setTestOnBorrow(Boolean.valueOf(REDIS_TESTONBORROW));
        jedisPool = new JedisPool(config, REDIS_HOST, Integer.parseInt(REDIS_PORT), Integer.parseInt(REDIS_TIMEOUT), REDIS_PASS);
    }

    /**
     * 从jedis连接池中获取获取jedis对象
     *
     * @return
     */
    private Jedis getJedis() {
        Jedis jedis = null;
        if (jedisPool != null) {
            jedis = jedisPool.getResource();
        }
        return jedis;
    }

    /**
     * 回收jedis对象
     *
     * @return
     */
    private void returnJedis(Jedis jedis) {
        if (jedis != null && jedisPool != null) {
            jedisPool.returnResource(jedis);
        }
    }

    //设置某个key的过期时间
    public void setKeyExpiresWithSeconds(String key, int seconds) {
        Jedis jedis = getJedis();
        jedis.expire(key, seconds);
        returnJedis(jedis);
    }

    //插入数据
    public void doInsert(String key, String value) {
        Jedis jedis = getJedis();
        jedis.lpush(key, value);
        returnJedis(jedis);
    }

    public void doInsert(String key, List<String> values) {
        Jedis jedis = getJedis();
        Pipeline pipeline = jedis.pipelined();
        for (String value : values) {
            pipeline.lpush(key, value);
        }
        pipeline.sync();
        returnJedis(jedis);
    }

    public void doInsert(String key, Map<String, String> dataMap) {
        Jedis jedis = getJedis();
        try {
            Pipeline pipeline = jedis.pipelined();
            pipeline.hmset(key, dataMap);
            pipeline.sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }

    }

    public void doInsert(Map<String, String> map) {
        Jedis jedis = getJedis();
        Pipeline pipeline = jedis.pipelined();
        if (map != null) {
            Set<Map.Entry<String, String>> entrySet = map.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                String key = entry.getKey();
                String value = entry.getValue();
                pipeline.sadd(key, value);
            }
            pipeline.sync();
        }
    }

    public void doInsert(String key,Set<String> set) {
        Jedis jedis = getJedis();
        try {
            Pipeline pipeline = jedis.pipelined();
            if (set != null) {
                for (String geoHash : set) {
                    pipeline.sadd(key,geoHash);
                    pipeline.sync();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            returnJedis(jedis);
        }
    }

    public static void main(String[] args) {
        RedisTool redisTool = RedisTool.getInstance();
        Set<String> strs = new HashSet<>();
        strs.add("wws8w9");
        strs.add("hh");
        strs.add("tt");
        redisTool.doInsert("test",strs);
//        redisTool.doInsert("v1",strs);
//        redisTool.setKeyExpiresWithSeconds("v1",3);
//        Map<String, String> map = new HashMap<>();
//        map.put("name", "taosm");
//        map.put("healthy", "ok");
//        redisTool.doInsert("v1", map);
    }
}

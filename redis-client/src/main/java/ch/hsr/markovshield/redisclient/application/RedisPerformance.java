package ch.hsr.markovshield.redisclient.application;

import redis.clients.jedis.Jedis;

public class RedisPerformance {
    public static void main(final String[] args) throws Exception {
        Jedis jedis = new Jedis("localhost");
        jedis.psubscribe(new MarkovClickStreamValidationResultListener(), "*");
    }
}

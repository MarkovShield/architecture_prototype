package ch.hsr.markovshield.redisclient.application;

import redis.clients.jedis.JedisPubSub;
import java.time.Instant;

public class MarkovClickStreamValidationResultListener extends JedisPubSub {

    @Override
    public void onPMessage(String pattern, String channel,
                           String message) {
        System.out.println("channel: " + channel + " message: " + message + " timestamp: " + Instant
            .now()
            .toEpochMilli());
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
        System.out.println("started subscribing: " + pattern);
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("stopped subscribing: " + pattern);
    }
}

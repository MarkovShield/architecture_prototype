package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ClickStreamValidation;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;

public class ClickStreamValidationRedisMapper implements RedisMapper<ClickStreamValidation> {

    @Override
    public RedisCommandDescription getCommandDescription() {
        return new RedisCommandDescription(RedisCommand.PUBLISH, "");
    }

    @Override
    public String getKeyFromData(ClickStreamValidation data) {
        return data.getClickUUID();
    }

    @Override
    public String getValueFromData(ClickStreamValidation data) {
        return data.getRating().toString();
    }
}

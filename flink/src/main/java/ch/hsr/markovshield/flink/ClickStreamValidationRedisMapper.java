package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ClickStreamValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommand;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisCommandDescription;
import org.apache.flink.streaming.connectors.redis.common.mapper.RedisMapper;


public class ClickStreamValidationRedisMapper implements RedisMapper<ClickStreamValidation> {

    private final ObjectMapper mapper;

    public ClickStreamValidationRedisMapper() {
        mapper = new ObjectMapper();
    }

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
        try {
            return new String(mapper.writeValueAsBytes(data));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}

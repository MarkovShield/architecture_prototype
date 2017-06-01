package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.utils.JsonPOJOSerde;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.kafka.common.serialization.Serializer;
import java.util.function.Function;

public class MarkovShieldSerializationSchema<V> implements KeyedSerializationSchema<V> {

    private final Serializer<V> jsonSerializer;
    private final String topic;
    private final TypeInformation<V> forClass;
    private final Function<V, byte[]> keySerializer;

    public MarkovShieldSerializationSchema(String topic,
                                           Class<V> cls,
                                           Function<V, byte[]> keySerializer,
                                           boolean useSmile) {
        this.topic = topic;
        forClass = TypeExtractor.getForClass(cls);
        this.keySerializer = keySerializer;
        this.jsonSerializer = new JsonPOJOSerde<>(cls, useSmile).serializer();
    }

    @Override
    public byte[] serializeKey(V v) {
        return keySerializer.apply(v);
    }

    @Override
    public byte[] serializeValue(V v) {
        return jsonSerializer.serialize("", v);
    }

    @Override
    public String getTargetTopic(V v) {
        return topic;
    }
}

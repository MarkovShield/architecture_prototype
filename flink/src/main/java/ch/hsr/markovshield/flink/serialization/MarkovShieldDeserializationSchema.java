package ch.hsr.markovshield.flink.serialization;

import ch.hsr.markovshield.utils.JsonPOJOSerde;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.kafka.common.serialization.Deserializer;
import java.io.IOException;

public class MarkovShieldDeserializationSchema<V> implements KeyedDeserializationSchema<V> {

    private final Deserializer<V> jsonSerializer;
    private final TypeInformation<V> forClass;

    public MarkovShieldDeserializationSchema(Class<V> cls, boolean useSmile) {
        forClass = TypeExtractor.getForClass(cls);
        this.jsonSerializer = new JsonPOJOSerde<>(cls, useSmile).deserializer();
    }

    @Override
    public V deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
        return this.jsonSerializer.deserialize("", bytes1);
    }

    @Override
    public boolean isEndOfStream(V v) {
        return false;
    }

    @Override
    public TypeInformation<V> getProducedType() {
        return forClass;
    }
}

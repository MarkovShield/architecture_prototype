package ch.hsr.markovshield.flink;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDecoder;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
import java.util.Map;

public class MyAvroSerializer<T> implements SerializationSchema<T> {

    private KafkaAvroSerializer inner;

    @Override
    public byte[] serialize(T t) {
        if(inner == null)
        {
            inner = new KafkaAvroSerializer(new CachedSchemaRegistryClient("http://schema_registry:8081", 100));
        }
        return inner.serialize("xx", t);
    }
}

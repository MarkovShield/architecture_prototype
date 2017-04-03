//package ch.hsr.markovshield.flink;
//
//import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
//import io.confluent.kafka.serializers.KafkaAvroDecoder;
//import io.confluent.kafka.serializers.KafkaAvroDeserializer;
//import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
//import org.apache.commons.collections.map.HashedMap;
//import org.apache.flink.api.common.typeinfo.TypeInformation;
//import org.apache.flink.api.java.typeutils.TypeExtractor;
//import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
//
//import java.io.IOException;
//import java.util.Map;
//
//public class MyAvroDeserializerCopy<T> implements KeyedDeserializationSchema<T> {
//
//    private transient KafkaAvroDecoder decoder;
//
//    private final Class<T> avroType;
////    private KafkaAvroDecoder decoder;
//
//    public MyAvroDeserializerCopy(Class<T> avroType) {
//        this.avroType = avroType;
//    }
//
//    public T deserialize(byte[] message) {
//        System.out.println("deserialize: " + new String(message));
//
//        if(decoder == null){
//            decoder = new KafkaAvroDecoder(new CachedSchemaRegistryClient("http://schema_registry:8081", 100));
///*
//            SchemaRegistryClient schemaRegistry = new CachedSchemaRegistryClient("http://schema_registry:8081",100);
//            this.decoder = new KafkaAvroDecoder(schemaRegistry);*/
//        }
//        System.out.println("deserializer: " + decoder + " " + message);
//        return (T) this.decoder.fromBytes(message);
//    }
//
//    @Override
//    public T deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
//        System.out.println("deserialize: " + new String(bytes) + " " + new String(bytes1));
//        if(decoder == null){
//            Map<String, Object> settings = new HashedMap();
//            settings.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG,true);
//            settings.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://schema_registry:8081");
//            decoder = new KafkaAvroDecoder(new CachedSchemaRegistryClient("http://schema_registry:8081", 100));
///*
//            SchemaRegistryClient schemaRegistry = new CachedSchemaRegistryClient("http://schema_registry:8081",100);
//            this.decoder = new KafkaAvroDecoder(schemaRegistry);*/
//        }
//        System.out.println("deserializer: " + decoder + " s: "  + s +  " bytes" + bytes );
//        System.out.println("key: " + new String(bytes));
//        System.out.println((T) this.decoder.fromBytes(bytes1));
//        return (T) this.decoder.fromBytes(bytes1);
//    }
//
//    public boolean isEndOfStream(T nextElement) {
//        if(nextElement == null){
//            return true;
//
//        }
//        return false;
//    }
//
//    @Override
//    public TypeInformation<T> getProducedType() {
//        return TypeExtractor.getForClass(avroType);
//    }
//}

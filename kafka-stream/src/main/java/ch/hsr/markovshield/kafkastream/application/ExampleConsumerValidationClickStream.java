package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.ValidationClickStream;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;

public class ExampleConsumerValidationClickStream {
    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws InterruptedException {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG,"Example");
        final KafkaConsumer<String, ValidationClickStream> clickConsumer = new KafkaConsumer<>(properties,
            Serdes.String().deserializer(),
            new JsonPOJOSerde<>(ValidationClickStream.class, JsonPOJOSerde.MARKOV_SHIELD_SMILE).deserializer());
        clickConsumer.subscribe(Collections.singletonList(MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC));
        boolean running = true;
        try {
            while (running) {
                ConsumerRecords<String, ValidationClickStream> records = clickConsumer.poll(1000);
                for (ConsumerRecord<String, ValidationClickStream> record : records){
                    System.out.println(Instant.now().toEpochMilli() - record.value().timeStampOfLastClick().toInstant().toEpochMilli());
                }
            }
        } finally {
            clickConsumer.close();
        }

    }

}

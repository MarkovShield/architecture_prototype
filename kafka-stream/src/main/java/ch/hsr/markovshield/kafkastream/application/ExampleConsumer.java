package ch.hsr.markovshield.kafkastream.application;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;

public class ExampleConsumer {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws InterruptedException {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "Example");
        final KafkaConsumer<String, String> clickConsumer = new KafkaConsumer<String, String>(properties);
        clickConsumer.subscribe(Collections.singletonList("EXAMPLE_TOPIC2"));
        boolean running = true;
        try {
            while (running) {
                ConsumerRecords<String, String> records = clickConsumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    long l = Instant.now().toEpochMilli();
                    long difference = Long.valueOf(record.value()) - l;
                    System.out.println(record.offset() + ": " + record.value() + " currenttime: " +
                        l + "difference: " + difference);
                }
            }
        } finally {
            clickConsumer.close();
        }

    }

}

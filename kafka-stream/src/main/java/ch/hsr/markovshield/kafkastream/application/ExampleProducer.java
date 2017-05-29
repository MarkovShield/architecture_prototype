package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.constants.KafkaConnectionDefaults;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class ExampleProducer {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws InterruptedException {
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConnectionDefaults.DEFAULT_BOOTSTRAP_SERVERS);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        final KafkaProducer<String, String> clickProducer = new KafkaProducer<>(properties);
        for (int i = 0; i < 100000; i++) {
            clickProducer.send(new ProducerRecord<String, String>("EXAMPLE_TOPIC","a",
                String.valueOf(Instant.now().toEpochMilli())));
            sleep(1);
        }
    }

}

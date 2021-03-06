package ch.hsr.markovshield.kafkastream.development_tools.performance_tester;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
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

public class PerformanceMeasurementClick {

    public static void main(final String[] args) throws IOException, InterruptedException {
        String broker;
        if (args.length > 0) {
            broker = args[0];
        } else {
            broker = "localhost:9092";
        }
        measureInputs(broker);
    }

    private static void measureInputs(String broker) throws InterruptedException {
        final Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "Example");
        final KafkaConsumer<String, Click> clickConsumer = new KafkaConsumer<>(properties,
            Serdes.String().deserializer(),
            new JsonPOJOSerde<>(Click.class, false).deserializer());
        clickConsumer.subscribe(Collections.singletonList(MarkovTopics.MARKOV_CLICK_TOPIC));
        boolean running = true;
        try {
            while (running) {
                ConsumerRecords<String, Click> records = clickConsumer.poll(1000);
                for (ConsumerRecord<String, Click> record : records) {
                    String sessionUUID = record.value().getSessionUUID();
                    String clickUUID = record.value()
                        .getClickUUID();
                    long now = Instant.now().toEpochMilli();
                    long timeStamp = record.value()
                        .getTimeStamp()
                        .toInstant()
                        .toEpochMilli();
                    System.out.println(sessionUUID + " " + clickUUID + " " + now + ": " + (now - timeStamp) + " - " + record
                        .value()
                        .isValidationRequired() + " - " + timeStamp);
                }
            }
        } finally {
            clickConsumer.close();
        }

    }

}

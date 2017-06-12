package ch.hsr.markovshield.kafkastream.development_tools.performance_tester;

import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ValidatedClickStream;
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

public class PerformanceMeasurementValidatedClickStream {

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
        final KafkaConsumer<String, ValidatedClickStream> clickConsumer = new KafkaConsumer<>(properties,
            Serdes.String().deserializer(),
            new JsonPOJOSerde<>(ValidatedClickStream.class, JsonPOJOSerde.MARKOV_SHIELD_SMILE).deserializer());
        clickConsumer.subscribe(Collections.singletonList(MarkovTopics.MARKOV_VALIDATED_CLICK_STREAMS));
        boolean running = true;
        try {
            while (running) {
                ConsumerRecords<String, ValidatedClickStream> records = clickConsumer.poll(1000);

                for (ConsumerRecord<String, ValidatedClickStream> record : records) {
                    ValidatedClickStream clickStream = record.value();
                    long now = Instant.now().toEpochMilli();
                    long diff1 = now - clickStream
                        .timeStampOfLastClick()
                        .toInstant()
                        .toEpochMilli();

                    Click lastClick = clickStream
                        .getClicks()
                        .get(clickStream.getClicks().size() - 1);
                    String clickUUID = lastClick
                        .getClickUUID();
                    String sessionUUID = clickStream.getSessionUUID();
                    System.out.println(sessionUUID + " " + clickUUID + " " + now + " " + diff1);
                }
            }
        } finally {
            clickConsumer.close();
        }

    }

}

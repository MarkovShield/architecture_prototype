package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.models.UrlConfiguration;
import ch.hsr.markovshield.models.UrlId;
import ch.hsr.markovshield.models.UrlRating;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;

public class MarkovUrlConfigGenerator {
    private static final String MARKOV_CONFIG_TOPIC = "MarkovUrlConfig";


    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws IOException, InterruptedException {

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);

        final KafkaProducer<String, UrlConfiguration> configProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(UrlConfiguration.class).serializer());

        final List<UrlConfiguration> urlConfigurations = new LinkedList<>();
        Random random = new Random();
        urlConfigurations.add(new UrlConfiguration("index.html", UrlRating.fromInt(random.nextInt(5)), UrlId.fromInt(0)));
        urlConfigurations.add(new UrlConfiguration("logout.html",UrlRating.fromInt(random.nextInt(5)), UrlId.fromInt(1)));
        urlConfigurations.add(new UrlConfiguration("overview.html", UrlRating.fromInt(random.nextInt(5)), UrlId.fromInt(2)));
        urlConfigurations.add(new UrlConfiguration("news.html", UrlRating.fromInt(random.nextInt(5)), UrlId.fromInt(3)));
        urlConfigurations.add(new UrlConfiguration("start.html", UrlRating.fromInt(random.nextInt(5)), UrlId.fromInt(0)));
        urlConfigurations.add(new UrlConfiguration("xxx.html", UrlRating.fromInt(random.nextInt(5)), UrlId.fromInt(4)));

        for (UrlConfiguration config : urlConfigurations) {

            configProducer.send(new ProducerRecord<>(MARKOV_CONFIG_TOPIC, config.getUrl(), config));
            configProducer.flush();
        }

    }

}

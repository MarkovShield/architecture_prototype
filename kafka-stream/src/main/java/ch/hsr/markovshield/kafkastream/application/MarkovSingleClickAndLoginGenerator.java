package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.Session;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

public class MarkovSingleClickAndLoginGenerator {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws IOException, InterruptedException {
        final List<Session> logins = new LinkedList<>();

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int sessionId = random.nextInt(1, 100000 + 1);
        String user = "Kilian";
        logins.add(new Session(String.valueOf(sessionId), user));


        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);

        final KafkaProducer<String, Session> loginProducer;
        loginProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(Session.class, JsonPOJOSerde.MOD_MSHIELD_SMILE).serializer());
        final KafkaProducer<String, Click> clickProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(Click.class, JsonPOJOSerde.MOD_MSHIELD_SMILE).serializer());

        final String loginTopic = "MarkovLogins";
        final String clickTopic = "MarkovClicks";

        final List<Click> clicks = new LinkedList<>();
        clicks.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "login.html",
            UrlRating.RISK_LEVEL_MEDIUM,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicks.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "xxx.html",
            UrlRating.RISK_LEVEL_HIGH,
            Date.from(
                Instant.now()), false));


        for (Session login : logins) {
            loginProducer.send(new ProducerRecord<>(loginTopic, login.getSessionUUID().toString(), login));
            loginProducer.flush();
        }

        for (Click click : clicks) {

            clickProducer.send(new ProducerRecord<>(clickTopic, click.getSessionUUID().toString(), click));
            clickProducer.flush();
        }

    }

}

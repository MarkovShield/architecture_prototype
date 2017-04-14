package ch.hsr.markovshield.kafkastream;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;

public class MarkovClickAndLoginGenerator {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws IOException, InterruptedException {
        final List<Session> logins = new LinkedList<>();
        final List<Click> clicks = new LinkedList<>();

        final List<String> users = new LinkedList<String>();
        users.add("Kilian");
        users.add("Philip");
        users.add("Matthias");
        users.add("Ivan");
        users.add("Kilian");
        final List<String> urls = new ArrayList<>();
        urls.add("index.html");
        urls.add("logout.html");
        urls.add("overview.html");
        urls.add("news.html");
        final Map<String, Integer> urlRatings = new HashMap<>();
        urlRatings.put("index.html", UrlRating.RISK_LEVEL_LOW);
        urlRatings.put("logout.html", UrlRating.RISK_LEVEL_MEDIUM);
        urlRatings.put("overview.html", UrlRating.RISK_LEVEL_HIGH);
        urlRatings.put("news.html", UrlRating.RISK_LEVEL_LOW);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (String user : users) {
            int sessionId = random.nextInt(1, 100000 + 1);
            logins.add(new Session(String.valueOf(sessionId), user));

            IntStream.range(0, random.nextInt(10)).forEach(
                value -> {
                    String s = urls.get(random.nextInt(urls.size()));
                    clicks.add(new Click(String.valueOf(sessionId),
                        String.valueOf(random.nextInt()),
                        s,
                        urlRatings.get(s),
                        Date.from(
                            Instant.now())));
                }
            );
        }

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);

        final KafkaProducer<String, Session> loginProducer;
        loginProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(Session.class).serializer());
        final KafkaProducer<String, Click> clickProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(Click.class).serializer());

        final String loginTopic = "MarkovLogins";
        final String clickTopic = "MarkovClicks";

        final List<Click> clicksBeforeLogin = new LinkedList<>();
        clicksBeforeLogin.add(new Click(logins.get(0).getSessionId(), String.valueOf(random.nextInt()), "start.html", UrlRating.RISK_LEVEL_LOW, Date.from(
            Instant.now())));
        clicksBeforeLogin.add(new Click(logins.get(0).getSessionId(), String.valueOf(random.nextInt()), "login.html", UrlRating.RISK_LEVEL_MEDIUM, Date.from(
            Instant.now())));
        clicksBeforeLogin.add(new Click(logins.get(1).getSessionId(), String.valueOf(random.nextInt()), "start.html", UrlRating.RISK_LEVEL_LOW, Date.from(
            Instant.now())));
        clicksBeforeLogin.add(new Click(logins.get(2).getSessionId(), String.valueOf(random.nextInt()), "start.html", UrlRating.RISK_LEVEL_LOW, Date.from(
            Instant.now())));
        clicksBeforeLogin.add(new Click(logins.get(0).getSessionId(), String.valueOf(random.nextInt()), "xxx.html", UrlRating.RISK_LEVEL_HIGH, Date.from(
            Instant.now())));


        for (Click click : clicksBeforeLogin) {

            clickProducer.send(new ProducerRecord<>(clickTopic, click.getSessionUUID().toString(), click));
            clickProducer.flush();
        }

        sleep(1000);
        for (Session login : logins) {
            loginProducer.send(new ProducerRecord<>(loginTopic, login.getSessionId().toString(), login));
            loginProducer.flush();
        }
        sleep(1000);
        for (Click click : clicks) {

            clickProducer.send(new ProducerRecord<>(clickTopic, click.getSessionUUID().toString(), click));
            clickProducer.flush();
        }

    }

}

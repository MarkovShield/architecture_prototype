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
        String broker;
        if(args.length > 0){
            broker = args[0];
        }else{
            broker = "localhost:9092";
        }
        produceInputs(broker);
    }

    private static void produceInputs(String broker) throws IOException, InterruptedException {
        final List<Session> logins = new LinkedList<>();
        final List<Click> clicks = new LinkedList<>();

        final List<String> users = new LinkedList<String>();
        users.add("Kilian");
        users.add("Philip");
        users.add("Matthias");
        users.add("Ivan");
        users.add("Anja");
        users.add("Claudia");
        users.add("Patric");
        users.add("Jessica");
        users.add("Kilian");
        users.add("Philip");
        users.add("Matthias");
        users.add("Ivan");
        users.add("Anja");
        users.add("Matthias");
        users.add("Philip");
        final List<String> urls = new ArrayList<>();
        urls.add("index.html");
        urls.add("logout.html");
        urls.add("overview.html");
        urls.add("news.html");
        urls.add("hansli.html");
        urls.add("private.html");
        urls.add("xxx.html");
        urls.add("123.html");
        urls.add("31231.html");
        final Map<String, Integer> urlRatings = new HashMap<>();
        urlRatings.put("index.html", UrlRating.RISK_LEVEL_LOW);
        urlRatings.put("logout.html", UrlRating.RISK_LEVEL_MEDIUM);
        urlRatings.put("overview.html", UrlRating.RISK_LEVEL_HIGH);
        urlRatings.put("news.html", UrlRating.RISK_LEVEL_LOW);
        urlRatings.put("hansli.html", UrlRating.RISK_LEVEL_HIGH);
        urlRatings.put("private.html", UrlRating.RISK_LEVEL_HIGH);
        urlRatings.put("xxx.html", UrlRating.RISK_LEVEL_LOW);
        urlRatings.put("123.html", UrlRating.RISK_LEVEL_LOW);
        urlRatings.put("31231.html", UrlRating.RISK_LEVEL_LOW);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (String user : users) {
            int sessionId = random.nextInt(1, 100000 + 1);
            logins.add(new Session(String.valueOf(sessionId), user));

            IntStream.range(0, 100).forEach(
                value -> {
                    String s = urls.get(random.nextInt(urls.size()));
                    clicks.add(new Click(String.valueOf(sessionId),
                        String.valueOf(random.nextInt()),
                        s,
                        urlRatings.get(s),
                        Date.from(
                            Instant.now()), urlRatings.get(s) >= 2));
                }
            );
        }

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, broker);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);

        final KafkaProducer<String, Session> loginProducer;
        loginProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(Session.class, JsonPOJOSerde.MOD_MSHIELD_SMILE).serializer());
        final KafkaProducer<String, Click> clickProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(Click.class, JsonPOJOSerde.MOD_MSHIELD_SMILE).serializer());

        final String loginTopic = "MarkovLogins";
        final String clickTopic = "MarkovClicks";

        final List<Click> clicksBeforeLogin = new LinkedList<>();
        clicksBeforeLogin.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicksBeforeLogin.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "login.html",
            UrlRating.RISK_LEVEL_MEDIUM,
            Date.from(
                Instant.now()), false));
        clicksBeforeLogin.add(new Click(logins.get(1).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicksBeforeLogin.add(new Click(logins.get(2).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "start.html",
            UrlRating.RISK_LEVEL_LOW,
            Date.from(
                Instant.now()), false));
        clicksBeforeLogin.add(new Click(logins.get(0).getSessionUUID(),
            String.valueOf(random.nextInt()),
            "xxx.html",
            UrlRating.RISK_LEVEL_HIGH,
            Date.from(
                Instant.now()), true));


        for (Click click : clicksBeforeLogin) {
            Click newClick = new Click(click.getSessionUUID(),
                click.getClickUUID(),
                click.getUrl(),
                click.getUrlRiskLevel(),
                Date.from(
                    Instant.now()),
                click.isValidationRequired());
            clickProducer.send(new ProducerRecord<>(clickTopic, click.getSessionUUID().toString(), newClick));
            clickProducer.flush();
        }


        sleep(1000);
        for (Session login : logins) {
            loginProducer.send(new ProducerRecord<>(loginTopic, login.getSessionUUID().toString(), login));
            loginProducer.flush();
        }
        sleep(1000);

        clicks.add(new Click(logins.get(0).getSessionUUID(), "1000", "my-secret-url", 2, Date.from(Instant.now()),
            true));


        while (clicks.size() > 0) {
            int index;
            if (clicks.size() != 1) {
                index = random.nextInt(clicks.size() - 1);

            } else {
                index = 0;
            }
            Click click = clicks.get(index);
            clicks.remove(index);
            Click newClick = new Click(click.getSessionUUID(),
                click.getClickUUID(),
                click.getUrl(),
                click.getUrlRiskLevel(),
                Date.from(
                    Instant.now()),
                click.isValidationRequired());
            clickProducer.send(new ProducerRecord<>(clickTopic, click.getSessionUUID().toString(), newClick));
            clickProducer.flush();
            //sleep(10);
        }

    }

}

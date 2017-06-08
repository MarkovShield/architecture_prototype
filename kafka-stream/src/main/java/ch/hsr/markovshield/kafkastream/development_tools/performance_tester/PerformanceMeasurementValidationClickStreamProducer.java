package ch.hsr.markovshield.kafkastream.development_tools.performance_tester;

import ch.hsr.markovshield.ml_models.ClickStreamModel;
import ch.hsr.markovshield.ml_models.builder.RandomModelBuilder;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.UrlRating;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.ValidationClickStream;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ch.hsr.markovshield.constants.MarkovTopics.MARKOV_CLICK_STREAM_ANALYSIS_TOPIC;

public class PerformanceMeasurementValidationClickStreamProducer {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws InterruptedException {
        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        final KafkaProducer<String, ValidationClickStream> clickProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(ValidationClickStream.class, JsonPOJOSerde.MARKOV_SHIELD_SMILE).serializer());


        final List<String> users = new LinkedList<String>();
        users.add("Kilian");
        users.add("Kilian");
        users.add("Kilian");
        users.add("Kilian");
        users.add("Philip");
        users.add("Philip");
        users.add("Philip");
        users.add("Philip");
        users.add("Philip");
        users.add("Philip");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Matthias");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Ivan");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Anja");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Claudia");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Patric");
        users.add("Jessica");
        users.add("Jessica");
        users.add("Jessica");
        users.add("Jessica");
        users.add("Jessica");
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
        ClickStreamModel train = new RandomModelBuilder(20, 5).train(Collections.emptyList());
        for (String user : users) {
            LinkedList<Click> clicks = new LinkedList<>();
            String sessionId = String.valueOf(random.nextInt(1, 100000 + 1));
            IntStream.range(0, random.nextInt(10)).forEach(
                value -> {
                    String s = urls.get(random.nextInt(urls.size()));
                    clicks.add(new Click(sessionId,
                        String.valueOf(random.nextInt()),
                        s,
                        urlRatings.get(s),
                        Date.from(
                            Instant.now()), urlRatings.get(s) >= 2));
                }
            );
            ValidationClickStream validationClickStream = new ValidationClickStream(user,
                sessionId,
                clicks.stream().filter(click -> Objects.equals(click.getSessionUUID(), sessionId)).collect(
                    Collectors.toList()),
                new UserModel(user, Collections.singletonList(train)));
            clickProducer.send(new ProducerRecord<>(MARKOV_CLICK_STREAM_ANALYSIS_TOPIC,
                validationClickStream.getSessionUUID().toString(),
                validationClickStream));
            clickProducer.flush();
        }

    }

}

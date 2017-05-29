package ch.hsr.markovshield.kafkastream.application;

import ch.hsr.markovshield.avro.Click2;
import ch.hsr.markovshield.avro.Session2;
import ch.hsr.markovshield.models.UrlRating;
import io.confluent.examples.streams.utils.SpecificAvroSerializer;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
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

public class MarkovAvroClickAndLoginGenerator {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws IOException, InterruptedException {
        final List<Session2> logins = new LinkedList<>();
        final List<Click2> clicks = new LinkedList<>();

        final List<String> users = new LinkedList<String>();
        users.add("Kilian");
        users.add("Philip");
        users.add("Matthias");
        users.add("Ivan");
        users.add("Anja");
        users.add("Claudia");
        users.add("Patric");
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
        for (String user : users) {
            int sessionId = random.nextInt(1, 100000 + 1);
            logins.add(new Session2(String.valueOf(sessionId), user));

            IntStream.range(0, random.nextInt(100)).forEach(
                value -> {
                    String s = urls.get(random.nextInt(urls.size()));
                    clicks.add(new Click2(String.valueOf(sessionId),
                        String.valueOf(random.nextInt()),
                        s,
                        urlRatings.get(s),
                        Instant.now().toEpochMilli(), urlRatings.get(s) >= 2, 0L));
                }
            );
        }

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 0);
        properties.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
            "http://localhost:8081");


        SchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:8081", 1000);
        Map<String, String> schemaProps = new HashMap<>();
        schemaProps.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        final Serializer click2Serde = new KafkaAvroSerializer(client, schemaProps);
        final Serializer session2Serde = new KafkaAvroSerializer(client, schemaProps);
        String schemaStr = "{\"namespace\": \"ch.hsr.markovshield.avro\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"Click2\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"sessionUUID\", \"type\": \"string\"},\n" +
            "     {\"name\": \"clickUUID\", \"type\": \"string\"},\n" +
            "     {\"name\": \"url\", \"type\": \"string\"},\n" +
            "     {\"name\": \"urlRiskLevel\", \"type\": \"int\"},\n" +
            "     {\"name\": \"timeStamp\", \"type\": \"long\", \"logicalType\":\"timestamp-millis\"},\n" +
            "     {\"name\": \"validationRequired\", \"type\": \"boolean\"},\n" +
            "     {\"name\": \"kafkaFirstProcessedDate\", \"type\": \"long\", \"logicalType\":\"timestamp-millis\"}\n" +
            " ]\n" +
            "}";
        Schema clickSchema = new Schema.Parser().parse(schemaStr);
        SpecificData.Record x = new SpecificData.Record(clickSchema);
        final KafkaProducer<String, Session2> loginProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            session2Serde);
        final KafkaProducer<String, Click2> clickProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            click2Serde);

        final String loginTopic = "MarkovLogins2";
        final String clickTopic = "MarkovClicks2";

        for (Session2 login : logins) {
            loginProducer.send(new ProducerRecord<>(loginTopic, login.getSessionUUID().toString(), login));
            loginProducer.flush();
        }
        sleep(1000);

        for (Click2 click : clicks) {
            Click2 newClick = new Click2(click.getSessionUUID(),
                click.getClickUUID(),
                click.getUrl(),
                click.getUrlRiskLevel(),
                Instant.now().toEpochMilli(),
                click.getValidationRequired(),
                click.getKafkaFirstProcessedDate());
            clickProducer.send(new ProducerRecord<>(clickTopic, click.getSessionUUID().toString(), newClick));
            clickProducer.flush();
        }

    }

}

package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.Session;
import ch.hsr.markovshield.utils.SpecificAvroSerializer;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (String user : users){
            int sessionId = random.nextInt(1, 100000 + 1);
            logins.add(new Session(String.valueOf(sessionId), user));

            IntStream.range(0, random.nextInt(10)).forEach(
                value -> {
                    clicks.add(new Click(String.valueOf(sessionId), urls.get(random.nextInt(urls.size()))));
                }
            );
        }


        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SpecificAvroSerializer.class);

        properties.put("schema.registry.url", "http://localhost:8081");


        SchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:8081", 100);

        Serializer<Session> sessionSer = new SpecificAvroSerializer<>(client);
        Serializer<Click> clickSer = new SpecificAvroSerializer<>(client);

        final KafkaProducer<String, Session> loginProducer = new KafkaProducer<String, Session>(properties, Serdes.String().serializer(), sessionSer);
        final KafkaProducer<String, Click> clickProducer = new KafkaProducer<String, Click>(properties, Serdes.String().serializer(), clickSer);

        final String loginTopic = "MarkovLogins";
        final String clickTopic = "MarkovClicks";

        final List<Click> clicksBeforeLogin = new LinkedList<>();
        clicksBeforeLogin.add(new Click(logins.get(0).getSession(), "start.html"));
        clicksBeforeLogin.add(new Click(logins.get(0).getSession(), "login.html"));
        clicksBeforeLogin.add(new Click(logins.get(1).getSession(), "start.html"));
        clicksBeforeLogin.add(new Click(logins.get(2).getSession(), "start.html"));
        clicksBeforeLogin.add(new Click(logins.get(0).getSession(), "xxx.html"));

        for (Click click : clicksBeforeLogin) {

            clickProducer.send(new ProducerRecord<String, Click>(clickTopic, click.getSession().toString(), click));
            clickProducer.flush();
        }

        sleep(1000);
        for (Session login : logins) {
            loginProducer.send(new ProducerRecord<String, Session>(loginTopic, login.getSession().toString(), login));
            loginProducer.flush();
        }
        sleep(1000);
        for (Click click : clicks) {

            clickProducer.send(new ProducerRecord<String, Click>(clickTopic, click.getSession().toString(), click));
            clickProducer.flush();
        }

    }


}

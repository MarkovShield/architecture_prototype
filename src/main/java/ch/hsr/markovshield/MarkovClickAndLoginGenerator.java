package ch.hsr.markovshield; /**
 * Copyright 2016 Confluent Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import ch.hsr.markovshield.utils.SpecificAvroSerializer;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

/**
 * This is a sample driver for the {@link PageViewRegion}.
 * To run this driver please first refer to the instructions in {@link PageViewRegion}.
 * You can then run this class directly in your IDE or via the command line.
 * <p>
 * To run via the command line you might want to package as a fatjar first. Please refer to:
 * <a href='https://github.com/confluentinc/examples/tree/3.2.x/kafka-streams#packaging-and-running'>Packaging</a>
 * <p>
 * Once packaged you can then run:
 * <pre>
 * {@code
 * $ java -cp target/streams-examples-3.2.0-standalone.jar io.confluent.examples.streams.ch.hsr.markovshield.PageViewRegionDriver
 * }</pre>
 * You should terminate with {@code Ctrl-C}.
 */
public class MarkovClickAndLoginGenerator {

    public static void main(final String[] args) throws IOException {
        produceInputs();
    }

    private static void produceInputs() throws IOException {
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


        for (Session login : logins) {
            loginProducer.send(new ProducerRecord<String, Session>(loginTopic, login.getSession().toString(), login));
            loginProducer.flush();
        }

        for (Click click : clicks) {

            clickProducer.send(new ProducerRecord<String, Click>(clickTopic, click.getSession().toString(), click));
            clickProducer.flush();
        }

    }


}

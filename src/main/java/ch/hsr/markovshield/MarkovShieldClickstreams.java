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

import ch.hsr.markovshield.utils.GenericAvroSerde;
import ch.hsr.markovshield.utils.SpecificAvroSerde;
import ch.hsr.markovshield.utils.SpecificAvroSerializer;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.avro.Schema;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.map.SingletonMap;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.google.common.collect.Iterables.*;

/**
 * Demonstrates how to perform a join between a KStream and a KTable, i.e. an example of a stateful
 * computation, using the generic Avro binding for serdes in Kafka Streams.  Same as
 * PageViewRegionExample but uses lambda expressions and thus only works on Java 8+.
 * <p>
 * In this example, we join a stream of page views (aka clickstreams) that reads from a topic named
 * "PageViews" with a user profile table that reads from a topic named "UserProfiles" to compute the
 * number of page views per user region.
 * <p>
 * Note: The generic Avro binding is used for serialization/deserialization. This means the
 * appropriate Avro schema files must be provided for each of the "intermediate" Avro classes, i.e.
 * whenever new types of Avro objects (in the form of GenericRecord) are being passed between
 * processing steps.
 * <p>
 * <br>
 * HOW TO RUN THIS EXAMPLE
 * <p>
 * 1) Start Zookeeper, Kafka, and Confluent Schema Registry. Please refer to <a href='http://docs.confluent.io/current/quickstart.html#quickstart'>QuickStart</a>.
 * <p>
 * 2) Create the input/intermediate/output topics used by this example.
 * <pre>
 * {@code
 * $ bin/kafka-topics --create --topic PageViews \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic PageViewsByUser \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic UserProfiles \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * $ bin/kafka-topics --create --topic PageViewsByRegion \
 *                    --zookeeper localhost:2181 --partitions 1 --replication-factor 1
 * }</pre>
 * Note: The above commands are for the Confluent Platform. For Apache Kafka it should be {@code bin/kafka-topics.sh ...}.
 * <p>
 * 3) Start this example application either in your IDE or on the command line.
 * <p>
 * If via the command line please refer to <a href='https://github.com/confluentinc/examples/tree/3.2.x/kafka-streams#packaging-and-running'>Packaging</a>.
 * Once packaged you can then run:
 * <pre>
 * {@code
 * $ java -cp target/streams-examples-3.2.0-standalone.jar io.confluent.examples.streams.ch.hsr.markovshield.PageViewRegion
 * }</pre>
 * 4) Write some input data to the source topics (e.g. via {@link PageViewRegionDriver}).
 * The already running example application (step 3) will automatically process this input data and
 * write the results to the output topic.
 * <pre>
 * {@code
 * # Here: Write input data using the example driver. Once the driver has stopped generating data,
 * # you can terminate it via `Ctrl-C`.
 * $ java -cp target/streams-examples-3.2.0-standalone.jar io.confluent.examples.streams.ch.hsr.markovshield.PageViewRegionDriver
 * }</pre>
 * 5) Inspect the resulting data in the output topic, e.g. via {@code kafka-console-consumer}.
 * <pre>
 * {@code
 * $ bin/kafka-console-consumer --topic PageViewsByRegion --from-beginning \
 *                              --zookeeper localhost:2181 \
 *                              --property print.key=true \
 *                              --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
 * }</pre>
 * You should see output data similar to:
 * <pre>
 * {@code
 * [africa@1466515140000]  2
 * [asia@1466514900000]  3
 * ...
 * }</pre>
 * Here, the output format is "[REGION@WINDOW_START_TIME] COUNT".
 * <p>
 * 6) Once you're done with your experiments, you can stop this example via {@code Ctrl-C}. If needed,
 * also stop the Confluent Schema Registry ({@code Ctrl-C}), then stop the Kafka broker ({@code Ctrl-C}), and
 * only then stop the ZooKeeper instance ({@code Ctrl-C}).
 */
public class MarkovShieldClickstreams {

    public static final String USER_NOT_FOUND = "--------------------NOT FOUND---------------------------";

    public static void main(final String[] args) throws Exception {
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "MarkovShieldClickstreams");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);

        final KStreamBuilder builder = new KStreamBuilder();

        final KTable<String, Session> sessions = builder.table( "MarkovLogins", "MarkovLoginStore");


        sessions.foreach((key, value) -> {
            System.out.println("Session: " + key + " " + value.toString());
        });

        final KStream<String, Click> views = builder.stream("MarkovClicks");

        views.foreach((key, value) -> {
            System.out.println("Click: " + key + " " + value.toString());
        });



        KTable<String, ClickStream> clickstreams = views.leftJoin(sessions,
                (view, session) -> {
                    ClickStream clickStream = new ClickStream();
                    System.out.println(session);
                    if(session != null){
                        clickStream.setUser(session.getUser());
                    }else{
                        clickStream.setUser(USER_NOT_FOUND);
                    }
                    clickStream.setSession(view.getSession());
                    clickStream.setClicks(Collections.singletonList(view));
                    return clickStream;
                }
        ).groupByKey().reduce(
                (clickStream, v1) -> {
                    ClickStream aggregatedClickStream = new ClickStream();
                    aggregatedClickStream.setSession(clickStream.getSession());
                    if(clickStream.getUser().equals(USER_NOT_FOUND)){
                        aggregatedClickStream.setUser(v1.getUser());
                    }else{
                        aggregatedClickStream.setUser(clickStream.getUser());
                    }
                    aggregatedClickStream.setClicks(Lists.newLinkedList(concat(clickStream.getClicks(), v1.getClicks())));
                    return aggregatedClickStream;
                }, "MarkovClickStreamAggregation"
        );


        clickstreams.foreach((key, value) -> {
            System.out.println("ClickStream: " + key + " " + value.toString());
        });
        clickstreams.to("MarkovClickStreams");


        final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}



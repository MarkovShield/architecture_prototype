package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.Session;
import com.google.common.collect.Lists;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.hadoop.mapreduce.tools.CLI;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Collections;
import java.util.Properties;

import static com.google.common.collect.Iterables.concat;

public class MarkovShieldClickstreams {

    public static final String USER_NOT_FOUND = "--------------------NOT FOUND---------------------------";

    public static void main(final String[] args) throws Exception {
        final Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "MarkovShieldClickstreams");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:8081");
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass().getName());

        streamsConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        streamsConfiguration.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1);

        final KStreamBuilder builder = new KStreamBuilder();

        final KTable<String, Session> sessions = builder.table( Serdes.String(), new JsonPOJOSerde<>(Session.class),"MarkovLogins", "MarkovLoginStore");


        sessions.foreach((key, value) -> {
            System.out.println("Session: " + key + " " + value.toString());
        });

        final KStream<String, Click> views = builder.stream(Serdes.String(), new JsonPOJOSerde<>(Click.class),"MarkovClicks");

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
                }, Serdes.String(), new JsonPOJOSerde<>(Click.class)
        ).groupByKey( Serdes.String(), new JsonPOJOSerde<>(ClickStream.class)).reduce(
                (clickStream, v1) -> {
                    ClickStream aggregatedClickStream = new ClickStream();
                    aggregatedClickStream.setSession(clickStream.getSession());
                    aggregatedClickStream.setUser(clickStream.getUser());
                    if(clickStream.getUser().toString().equals(USER_NOT_FOUND) && !(clickStream.getUser().equals(v1.getUser()))){
                        aggregatedClickStream.setUser(v1.getUser());
                    }
                    aggregatedClickStream.setClicks(Lists.newLinkedList(concat(clickStream.getClicks(), v1.getClicks())));
                    return aggregatedClickStream;
                }, "MarkovClickStreamAggregation"
        );


        clickstreams.foreach((key, value) -> {
            System.out.println("ClickStream: " + key + " " + value.toString());
        });
        clickstreams.to(Serdes.String(), new JsonPOJOSerde<>(ClickStream.class),"MarkovClickStreams");


        final KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);

        streams.cleanUp();
        streams.start();

        // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

}



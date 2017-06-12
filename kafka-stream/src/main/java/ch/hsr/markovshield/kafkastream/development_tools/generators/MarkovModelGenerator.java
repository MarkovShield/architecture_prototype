package ch.hsr.markovshield.kafkastream.development_tools.generators;

import ch.hsr.markovshield.constants.KafkaConnectionDefaults;
import ch.hsr.markovshield.constants.MarkovTopics;
import ch.hsr.markovshield.ml_models.builder.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import ch.hsr.markovshield.ml_models.builder.RandomModelBuilder;
import ch.hsr.markovshield.models.SimpleUserModelFactory;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.models.UserModelFactory;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class MarkovModelGenerator {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws IOException, InterruptedException {

        final List<String> users = new LinkedList<>();
        users.add("Kilian");
        users.add("Philip");
        users.add("Matthias");
        users.add("Ivan");

        IQRFrequencyAnalysis iqrFrequencyAnalysis = new IQRFrequencyAnalysis();
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        UserModelFactory factory = new SimpleUserModelFactory(new RandomModelBuilder(20, 10));
        List<UserModel> userModels = users.stream()
            .map(user -> factory.trainUserModel(Collections.emptyList(), user))
            .collect(Collectors.toList());

        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConnectionDefaults.DEFAULT_BOOTSTRAP_SERVERS);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);


        final KafkaProducer<String, UserModel> modelProducer = new KafkaProducer<>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(UserModel.class, JsonPOJOSerde.MARKOV_SHIELD_SMILE).serializer());

        sleep(1000);

        userModels.stream().forEach(userModel -> {
            modelProducer.send(new ProducerRecord<>(MarkovTopics.MARKOV_USER_MODEL_TOPIC,
                userModel.getUserId().toString(),
                userModel));
            modelProducer.flush();
        });
    }

}

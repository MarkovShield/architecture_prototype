package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.ml.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.models.UserModel;
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

import static java.lang.Thread.sleep;

public class MarkovModelGenerator {

    public static void main(final String[] args) throws IOException, InterruptedException {
        produceInputs();
    }

    private static void produceInputs() throws IOException, InterruptedException {
        final List<UserModel> userModels = new LinkedList<>();


        final List<String> users = new LinkedList<String>();
        users.add("Kilian");
        users.add("Philip");
        users.add("Matthias");
        users.add("Ivan");

        IQRFrequencyAnalysis iqrFrequencyAnalysis = new IQRFrequencyAnalysis();
        for (String user : users) {
            UserModel userModel = new UserModel(user,
                MarkovChainWithMatrix.train(Collections.emptyList()),
                iqrFrequencyAnalysis.train(Collections.emptyList()));
            userModels.add(userModel);
        }


        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);


        final KafkaProducer<String, UserModel> modelProducer = new KafkaProducer<String, UserModel>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<>(UserModel.class).serializer());

        final String modelTopic = "MarkovUserModels";

        sleep(1000);
        for (UserModel userModel : userModels) {
            modelProducer.send(new ProducerRecord<>(modelTopic,
                userModel.getUserId().toString(),
                userModel));
            modelProducer.flush();
        }

    }

}

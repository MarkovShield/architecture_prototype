package ch.hsr.markovshield.kafkastream;

import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.ml.TransistionMatrix;
import ch.hsr.markovshield.models.FrequencyModel;
import ch.hsr.markovshield.models.TransitionModel;
import ch.hsr.markovshield.models.UserModel;
import ch.hsr.markovshield.utils.JsonPOJOSerde;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

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
        users.add("Kilian");

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (String user : users) {
            int transisitionModelRating = random.nextInt(1, 100);
            int frequencyModelRating = random.nextInt(1, 100);
            UserModel userModel = new UserModel(user,
               MarkovChainWithMatrix.train(Collections.emptyList()),
                new FrequencyModel(frequencyModelRating, Date.from(
                    Instant.now())));
            userModels.add(userModel);
        }


        final Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonPOJOSerde.class);

        properties.put("schema.registry.url", "http://localhost:8081");


        SchemaRegistryClient client = new CachedSchemaRegistryClient("http://localhost:8081", 100);

        final KafkaProducer<String, UserModel> modelProducer = new KafkaProducer<String, UserModel>(properties,
            Serdes.String().serializer(),
            new JsonPOJOSerde<UserModel>(UserModel.class).serializer());

        final String modelTopic = "MarkovUserModels";

        sleep(1000);
        for (UserModel userModel : userModels) {
            modelProducer.send(new ProducerRecord<String, UserModel>(modelTopic,
                userModel.getUserId().toString(),
                userModel));
            modelProducer.flush();
        }

    }

}

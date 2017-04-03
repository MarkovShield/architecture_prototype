package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRatings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.*;

import java.util.List;
import java.util.Properties;
import java.util.Random;


public class MarkovShieldAnalyser {
    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:9092");
        properties.setProperty("zookeeper.connect", "zookeeper:2181");
        properties.setProperty("group.id", "test");
        DataStreamSource<ObjectNode> stream = env
                .addSource(new FlinkKafkaConsumer010<>("MarkovClickStreams", new JSONKeyValueDeserializationSchema(false), properties));
        ObjectMapper mapper = new ObjectMapper();

        SingleOutputStreamOperator<ClickStream> sessionStream = stream.map(jsonNodes -> mapper.treeToValue(jsonNodes.get("value"), ClickStream.class));

        SingleOutputStreamOperator<ClickStreamValidation> validationStream = sessionStream.map(clickStream -> validateSession(clickStream));


        FlinkKafkaProducer010<ClickStreamValidation> producer = new FlinkKafkaProducer010<ClickStreamValidation>("broker:9092", "MarkovClickStreamValidations", new KeyedSerializationSchema<ClickStreamValidation>() {

            @Override
            public byte[] serializeKey(ClickStreamValidation validation) {
                return validation.getSession().getBytes();
            }

            @Override
            public byte[] serializeValue(ClickStreamValidation validation) {
                try {
                    return mapper.writeValueAsBytes(validation);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return new byte[1];
            }

            @Override
            public String getTargetTopic(ClickStreamValidation validation) {
                return "MarkovClickStreamValidations";
            }
        });
        validationStream.addSink(producer);

        stream.print();
        sessionStream.print();
        validationStream.print();


        env.execute("Read from kafka and deserialize");
    }

    private static ClickStreamValidation validateSession(ClickStream clickStream) {
        int score = calculateMarkovScore(clickStream.getClicks());
        MarkovRatings rating = calculateMarkovFraudLevel(score);
        return new ClickStreamValidation(clickStream.getSession(),score, rating );
    }

    private static MarkovRatings calculateMarkovFraudLevel(int rating) {
        if(rating < 20) return MarkovRatings.VALID;
        if(rating < 50) return MarkovRatings.SUSPICIOUS;
        return MarkovRatings.FRAUD;
    }

    private static int calculateMarkovScore(List<Click> clicks) {
        return new Random().nextInt(100);
    }

}


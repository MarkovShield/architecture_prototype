package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamValidation;
import ch.hsr.markovshield.models.MarkovRatings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.kafka.connect.source.SourceTask;

import java.io.IOException;
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
        ObjectMapper mapper = new ObjectMapper();

        DataStreamSource<ClickStream> stream = env
                .addSource(new FlinkKafkaConsumer010<ClickStream>("MarkovClickStreams", new KeyedDeserializationSchema<ClickStream>() {
                    @Override
                    public TypeInformation<ClickStream> getProducedType() {
                        return TypeExtractor.getForClass(ClickStream.class);
                    }

                    @Override
                    public ClickStream deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
                        return mapper.readValue(bytes1, ClickStream.class);
                    }

                    @Override
                    public boolean isEndOfStream(ClickStream o) {

                        return false;
                    }
                }, properties));



        SingleOutputStreamOperator<ClickStreamValidation> validationStream = stream.map(clickStream -> validateSession(clickStream));


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
        validationStream.print();


        env.execute("Read from kafka and deserialize");
    }

    private static ClickStreamValidation validateSession(ClickStream clickStream) {
        int score = 0;
        if(clickStream.getUserModel() != null){
            score = clickStream.getUserModel().getFrequencyModel().getFrequencyValue() + clickStream.getUserModel().getTransitionModel().getTransitionValue();
        }
        MarkovRatings rating = calculateMarkovFraudLevel(score);
        return new ClickStreamValidation(clickStream.getSession(), score, rating);
    }

    private static MarkovRatings calculateMarkovFraudLevel(int rating) {
        if (rating < 100) return MarkovRatings.VALID;
        if (rating < 150) return MarkovRatings.SUSPICIOUS;
        return MarkovRatings.FRAUD;
    }


}


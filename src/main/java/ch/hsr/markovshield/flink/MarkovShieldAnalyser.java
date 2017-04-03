package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Session;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class MarkovShieldAnalyser {
    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:9092");
        properties.setProperty("zookeeper.connect", "zookeeper:2181");
        properties.setProperty("group.id", "test");
        DataStreamSource<ObjectNode> stream = env
                .addSource(new FlinkKafkaConsumer010<>("MarkovLogins", new JSONKeyValueDeserializationSchema(false), properties));
        ObjectMapper mapper = new ObjectMapper();

        SingleOutputStreamOperator<Session> sessionStream = stream.map(jsonNodes -> mapper.treeToValue(jsonNodes.get("value"), Session.class));



        FlinkKafkaProducer010<Session> producer = new FlinkKafkaProducer010<Session>("broker:9092", "xxx", new KeyedSerializationSchema<Session>() {

            @Override
            public byte[] serializeKey(Session session) {
                return session.getSession().getBytes();
            }

            @Override
            public byte[] serializeValue(Session session) {
                try {
                    return mapper.writeValueAsBytes(session);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                return new byte[1];
            }

            @Override
            public String getTargetTopic(Session session) {
                return "xxx";
            }
        });
        sessionStream.addSink(producer);

        sessionStream.print();
        stream.print();



        env.execute("Read from kafka and deserialize");
    }

}


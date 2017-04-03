package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.Session;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

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

        SingleOutputStreamOperator<String> sessionStream = stream.map(session -> session.toString());
        FlinkKafkaProducer010<String> producer = new FlinkKafkaProducer010<String>("broker:9092", "xxx",new SimpleStringSchema());
        sessionStream.addSink(producer);
        sessionStream.print();
        stream.print();



        env.execute("Read from kafka and deserialize");
    }

}


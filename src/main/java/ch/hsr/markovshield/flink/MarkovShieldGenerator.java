package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.xx;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

import java.util.Properties;


public class MarkovShieldGenerator {
    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:9092");
        properties.setProperty("zookeeper.connect", "zookeeper:2181");
        properties.setProperty("group.id", "test");
        DataStream<xx> stream = env
                .addSource(new FlinkKafkaConsumer010<xx>("xx", new MyAvroDeserializer<>(xx.class), properties));

        SingleOutputStreamOperator<String> sessionStream = stream.map(session -> session.toString());
        FlinkKafkaProducer010<xx> producer = new FlinkKafkaProducer010<xx>("broker:9092", "xx", new MyAvroSerializer<xx>());
        DataStream<xx> xxStream = env.addSource(new SourceFunction<xx>() {
            private static final long serialVersionUID = 6369260445318862378L;
            public boolean running = true;
            @Override
            public void run(SourceContext<xx> sourceContext) throws Exception {
                long i = 0;
                while(this.running) {
                    sourceContext.collect(new xx("Element - " + i++));
                    Thread.sleep(100);
                }
            }

            @Override
            public void cancel() {
                running = false;
            }
        });
        xxStream.addSink(producer);
        xxStream.print();
        stream.print();



        env.execute("Read from kafka and deserialize");
    }

}


package ch.hsr.markovshield.flink;

import ch.hsr.markovshield.models.ClickCount;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.FrequencyModel;
import ch.hsr.markovshield.models.TransitionModel;
import ch.hsr.markovshield.models.UserModel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.AllWindowedStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer010;
import org.apache.flink.streaming.util.serialization.KeyedDeserializationSchema;
import org.apache.flink.streaming.util.serialization.KeyedSerializationSchema;
import org.apache.flink.util.Collector;
import java.io.IOException;
import java.util.Properties;


public class MarkovShieldModelUpdate {

    public static void main(final String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setStreamTimeCharacteristic(TimeCharacteristic.ProcessingTime);


        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "broker:9092");
        properties.setProperty("zookeeper.connect", "zookeeper:2181");
        properties.setProperty("group.id", "MarkovShieldModelUpdate");


        DataStreamSource<ClickStream> stream = env
            .addSource(new FlinkKafkaConsumer010<ClickStream>("MarkovClickStreams",
                new KeyedDeserializationSchema<ClickStream>() {
                    @Override
                    public TypeInformation<ClickStream> getProducedType() {
                        return TypeExtractor.getForClass(ClickStream.class);
                    }

                    @Override
                    public ClickStream deserialize(byte[] bytes, byte[] bytes1, String s, int i, long l) throws IOException {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.readValue(bytes1, ClickStream.class);
                    }

                    @Override
                    public boolean isEndOfStream(ClickStream o) {

                        return false;
                    }
                },
                properties));
        WindowedStream<ClickStream, String, TimeWindow> windowedStream = stream
            .keyBy(ClickStream::getUserName)
            .timeWindow(Time.minutes(100), Time.seconds(30));
        stream.keyBy(ClickStream::getUserName).timeWindow(Time.minutes(100), Time.seconds(30)).apply(MarkovShieldModelUpdate::xx).print();
        SingleOutputStreamOperator<UserModel> userModelStream = windowedStream.apply(MarkovShieldModelUpdate::recreateUserModel);

        FlinkKafkaProducer010<UserModel> producer = new FlinkKafkaProducer010<UserModel>(
            "broker:9092",
            "MarkovUserModels",
            new KeyedSerializationSchema<UserModel>() {

                @Override
                public byte[] serializeKey(UserModel model) {
                    return model.getUserId().getBytes();
                }

                @Override
                public byte[] serializeValue(UserModel model) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        return mapper.writeValueAsBytes(model);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return new byte[1];
                }

                @Override
                public String getTargetTopic(UserModel validation) {
                    return "MarkovUserModels";
                }
            });
        userModelStream.print();
        userModelStream.addSink(producer);



        env.execute("UpdateUserModels");
    }

    private static void xx(String x, TimeWindow xx, Iterable<ClickStream> iterable, Collector<ClickCount> collector) {
        ClickCount clickCount = new ClickCount(x, 0);
        for (ClickStream clickStream : iterable) {
            if(!x.equals(clickStream.getUserName())){
                clickCount.setMultipleUsers(true);
            }
            clickCount.addClickStream(clickStream);
            clickCount.setClicks(clickCount.getClicks() + 1);
        }
        collector.collect(clickCount);
    }

    private static void recreateUserModel(String key, TimeWindow timeWindow, Iterable<ClickStream> iterable, Collector<UserModel> collector) {
        UserModel model = null;
        int count = 0;
        for (ClickStream clickStream : iterable) {
            if (model == null) {
                model = new UserModel(clickStream.getUserName(), new TransitionModel(), new FrequencyModel());
            }else{
                if(!model.getUserId().equals(clickStream.getUserName())){
                    throw new RuntimeException("UserName not the same");
                }
            }
            count += 1;
        }
        System.out.println(model.getUserId() + " " + count);
        collector.collect(model);

    }

}


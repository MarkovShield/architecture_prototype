package ch.hsr.markovshield; /**
 * Copyright 2016 Confluent Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * This is a sample driver for the {@link PageViewRegion}.
 * To run this driver please first refer to the instructions in {@link PageViewRegion}.
 * You can then run this class directly in your IDE or via the command line.
 * <p>
 * To run via the command line you might want to package as a fatjar first. Please refer to:
 * <a href='https://github.com/confluentinc/examples/tree/3.2.x/kafka-streams#packaging-and-running'>Packaging</a>
 * <p>
 * Once packaged you can then run:
 * <pre>
 * {@code
 * $ java -cp target/streams-examples-3.2.0-standalone.jar io.confluent.examples.streams.ch.hsr.markovshield.PageViewRegionDriver
 * }</pre>
 * You should terminate with {@code Ctrl-C}.
 */
public class PageViewRegionDriver {

  public static void main(final String[] args) throws IOException {
    produceInputs();
    consumeOutput();
  }

  private static void produceInputs() throws IOException {
    final String[] users = {"erica", "bob", "joe", "damian", "tania", "phil", "sam", "lauren", "joseph"};
    final String[] regions = {"europe", "usa", "asia", "africa"};

    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, io.confluent.kafka.serializers.KafkaAvroSerializer.class);
    props.put("schema.registry.url", "http://localhost:8081");
    final KafkaProducer<String, GenericRecord> producer = new KafkaProducer<>(props);

    final GenericRecordBuilder pageViewBuilder =
      new GenericRecordBuilder(loadSchema("click.avsc"));
    final GenericRecordBuilder userProfileBuilder =
      new GenericRecordBuilder(loadSchema("userprofile.avsc"));

    final String pageViewsTopic = "PageViews";
    final String userProfilesTopic = "UserProfiles";

    final Random random = new Random();
    pageViewBuilder.set("industry", "eng");
    for (final String user : users) {
      userProfileBuilder.set("experience", "some");
      userProfileBuilder.set("region", regions[random.nextInt(regions.length)]);
      producer.send(new ProducerRecord<>(userProfilesTopic, user, userProfileBuilder.build()));
      // For each user generate some page views
      int rangeEnd = random.nextInt(10);
      IntStream.range(0, rangeEnd)
        .mapToObj(value -> {
          pageViewBuilder.set("user", user);
          pageViewBuilder.set("page", "index.html");
          return pageViewBuilder.build();
        }).forEach(
        record -> {
          try {
            System.in.read();
          } catch (IOException e) {
            e.printStackTrace();
          }
          producer.send(new ProducerRecord<>(pageViewsTopic, null, record));
          producer.flush();
        }
      );
      producer.flush();
      System.out.println(user + " " + rangeEnd );
    }
  }

  private static void consumeOutput() {
    final String resultTopic = "PageViewsByRegion";
    final Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG,
      "pageview-region-lambda-example-consumer");
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    final KafkaConsumer<String, Long> consumer = new KafkaConsumer<>(consumerProperties);

    consumer.subscribe(Collections.singleton(resultTopic));
    while (true) {
      final ConsumerRecords<String, Long> consumerRecords = consumer.poll(Long.MAX_VALUE);
      for (final ConsumerRecord<String, Long> consumerRecord : consumerRecords) {
        System.out.println(consumerRecord.key() + ":" + consumerRecord.value());
      }
    }
  }

  private static Schema loadSchema(final String name) throws IOException {
    try (InputStream input = PageViewRegion.class.getClassLoader()
      .getResourceAsStream("avro/io/confluent/examples/streams/" + name)) {
      return new Schema.Parser().parse(input);
    }
  }

}

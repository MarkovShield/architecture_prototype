# Readme kafka_streaming_playground
## Host file entries
 - `127.0.0.1 zookeeper`
 - `127.0.0.1 broker`

## Commands
### connect to docker host
```bash
docker exec -u 0 -it deployment_broker_1 /bin/bash
```
### create topics
```bash
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovLogins
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovClicks
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovUserModels
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovClickStreams
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovClickStreamAnalysis
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovValidatedClickStream

```
### consumer
```bash
kafka-console-consumer --zookeeper zookeeper:2181 --topic MarkovValidatedClickStream --from-beginning --property print.key=true
```
### producer
```bash
echo '61631#{"sessionUUID":"61631","clickUUID":"1000","url":"my-secret-url","urlRiskLevel":2,"timeStamp":1495602498740,"validationRequired":true}' | kafka-console-producer --broker-list localhost:9092 --topic MarkovClicks --property "parse.key=true" --property "key.separator=#";
```

### build jar
```bash
mvn -am --projects kafka-stream clean install
mvn -am --projects flink clean install
```
### delete all docker images ONLY for breaking changes
```bash
FOR /f "tokens=*" %i IN ('docker ps -a -q') DO docker rm %i
```
### run
#### Windows
```bash
java -cp kafka-stream\target\kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovModelGenerator
java -cp kafka-stream\target\kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovClickAndLoginGenerator
java -cp kafka-stream\target\kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovShieldClickstreams
```
```bash
bin\flink run -c ch.hsr.markovshield.flink.MarkovShieldAnalyser --jobmanager jobmanager:6123 C:\Users\maede\Documents\architecture_prototype\flink\target\flink-1.0-SNAPSHOT-jar-with-dependencies.jar
bin\flink run -c ch.hsr.markovshield.flink.MarkovShieldModelUpdate --jobmanager jobmanager:6123 C:\Users\maede\Documents\architecture_prototype\flink\target\flink-1.0-SNAPSHOT-jar-with-dependencies.jar

```
#### macOS/Linux
```bash
java -cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovModelGenerator
java -cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovClickAndLoginGenerator
java -cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovShieldClickstreams
```
```bash
flink run -c ch.hsr.markovshield.flink.MarkovShieldAnalyser --jobmanager jobmanager:6123 flink/target/flink-1.0-SNAPSHOT-jar-with-dependencies.jar
flink run -c ch.hsr.markovshield.flink.MarkovShieldModelUpdate --jobmanager jobmanager:6123 flink/target/flink-1.0-SNAPSHOT-jar-with-dependencies.jar

```
#### build
download http://mirror.synyx.de/apache/avro/avro-1.8.1/java/avro-tools-1.8.1.jar
```bash
java -cp avro-tools-1.8.1.jar org.apache.avro.tool.Main compile schema TypeReuseTest.avsc CompoundSubTypeExtended.avsc DirWithOtherAvscFiles OutputDir
```
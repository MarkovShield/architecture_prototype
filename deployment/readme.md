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
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic YourTopicName
```
### consumer
```bash
kafka-console-consumer --zookeeper zookeeper:2181 --topic YourTopicName --from-beginning --property print.key=true
```
### build jar
```bash
mvn clean compile assembly:single
```
### delete all docker images ONLY for breaking changes
```bash
FOR /f "tokens=*" %i IN ('docker ps -a -q') DO docker rm %i
```
### run
#### Windows
```bash
java -cp target\architecture_prototype-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.MarkovClickAndLoginGenerator
java -cp target\architecture_prototype-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.MarkovShieldClickstreams
```
```bash
bin\flink run -c ch.hsr.markovshield.flink.MarkovShieldAnalyser --jobmanager jobmanager:6123 C:\Users\maede\Documents\architecture_prototype\target\architecture_prototype-1.0-SNAPSHOT-jar-with-dependencies.jar
```
#### macOS/Linux
```bash
java -cp target/architecture_prototype-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.MarkovClickAndLoginGenerator
java -cp target/architecture_prototype-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.MarkovShieldClickstreams
```

#### build
download http://mirror.synyx.de/apache/avro/avro-1.8.1/java/avro-tools-1.8.1.jar
```bash
java -cp avro-tools-1.8.1.jar org.apache.avro.tool.Main compile schema TypeReuseTest.avsc CompoundSubTypeExtended.avsc DirWithOtherAvscFiles OutputDir
```
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
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic PageViews
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic PageViewsPerRegion
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic PageViewsByUser
```
### consumer
```bash
kafka-console-consumer --zookeeper zookeeper:2181 --topic PageViewsByRegion --from-beginning --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```
### build jar
```bash
mvn clean compile assembly:single
```
### run
#### Windows
```bash
java -cp target\kafka_streaming_playground-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.PageViewRegionDriver
java -cp target\kafka_streaming_playground-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.PageViewRegion
```

#### macOS/Linux
```bash
java -cp target/kafka_streaming_playground-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.PageViewRegionDriver
java -cp target/kafka_streaming_playground-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.PageViewRegion
```

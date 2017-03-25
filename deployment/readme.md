# Readme kafka_streaming_playground
### Hosteinträge
 - 127.0.0.1 zookeeper
 - 127.0.0.1 broker

### Befehle
##### connect to docker host
```
docker exec -u 0 -it deployment_broker_1 bash
```
##### create topics
```
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic PageViews
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic PageViewsPerRegion
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic PageViewsByUser
```
##### consumer
```
kafka-console-consumer --zookeeper zookeeper:2181 --topic PageViewsByRegion --from-beginning --property print.key=true --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
```
##### build jar
```
mvn clean compile assembly:single
```
##### run
```
java -cp target\kafka_streaming_playground-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.PageViewRegionDriver
java -cp target\kafka_streaming_playground-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.PageViewRegion
```

![MarkovShield](https://bitbucket.org/markovshield/mod_mshield/raw/develop/resources/markovshield_logo.png)

# MarkovShield Engine
This is the reporitoy of the MarkovShield Engine, the backend of MarkovShield. If you would like to try out MarkovShield, please have a look at the [https://github.com/MarkovShield/install](https://github.com/MarkovShield/install) repository. The following documentation is only needed, if you would like to run the MarkovShield Engine jobs the manual way.

## Content
* [MarkovShield Engine](#markdown-header-markovshield-engine)
	* [Content](#markdown-header-content)
	* [Prerequisites](#markdown-header-prerequisites)
		* [Host file entries](#markdown-header-host-file-entries)
		* [Dependency packages](#markdown-header-dependency-packages)
	* [Configuration](#markdown-header-configuration)
	* [Compilation](#markdown-header-compilation)
	* [Run the applications](#markdown-header-run-the-applications)
		* [Windows](#markdown-header-windows)
			* [Additonal commands](#markdown-header-additonal-commands)
		* [macOS/Linux](#markdown-header-macoslinux)
			* [Additonal commands](#markdown-header-additonal-commands)
	* [Usefull commands](#markdown-header-usefull-commands)
		* [Open shell inside the Kafka broker container](#markdown-header-open-shell-inside-the-kafka-broker-container)
		* [Create topics](#markdown-header-create-topics)
		* [Consume messages](#markdown-header-consume-messages)
		* [Produce message](#markdown-header-produce-message)
		* [Delete all docker images](#markdown-header-delete-all-docker-images)
	* [Old commands](#markdown-header-old-commands)
		* [Build AVRO](#markdown-header-build-avro)
	* [Publish images](#markdown-header-publish-images)

## Prerequisites

### Host file entries
If you run any of the down here provided commands ourside of the provided MarkovShield Engine job containers, ensure you have the following host entries set in your local hostfile:
```bash
127.0.0.1 zookeeper
127.0.0.1 broker
```

### Dependency packages
To run the commands down here you will need the following packages installes on your system:
*  `maven`
*  `flink`
*  `java-jdk`

**Note**: `flink` is only needed to submit the job JAR file to the MarkovShield Engine.

## Configuration
Please have a look at the [Settings](documentations/SETTINGS.md) documentation in order to see all possible configuration options.

## Compilation
To build the Kafka Stream and Flink JAR files:
```bash
mvn -am --projects kafka-stream clean install
mvn -am --projects flink clean install
```
**Hint**: If you run into any problems, try to run these commands with the parameter `-DskipTests`.

## Run the applications
Here are the commands listed which are needed to run the MarkovShield Engine.

### Windows
Start the Kafka Stream application:
```bash
java -cp kafka-stream\target\kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovShieldClickstreams
```

Afterwards start the Apache Flink jobs, which are needed to analyse a clickstream and update the user models:
```bash
bin\flink run -c ch.hsr.markovshield.flink.MarkovShieldAnalyser --jobmanager jobmanager:6123 C:\Users\<USER>\Documents\architecture_prototype\flink\target\flink-1.0-SNAPSHOT-jar-with-dependencies.jar
bin\flink run -c ch.hsr.markovshield.flink.MarkovShieldModelUpdater --jobmanager jobmanager:6123 C:\Users\<USER>\Documents\architecture_prototype\flink\target\flink-1.0-SNAPSHOT-jar-with-dependencies.jar
```
**Hint**: Perhaps you need to change the path of the `flink` application depending on your setup.

#### Additonal commands
```bash
java -cp kafka-stream\target\kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.development_tools.generators.MarkovModelGenerator
java -cp kafka-stream\target\kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.development_tools.generators.MarkovClickAndLoginGenerator
```

### macOS/Linux
Start the Kafka Stream application:
```bash
java -cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.application.MarkovShieldClickstreams
```

Afterwards start the Apache Flink jobs, which are needed to analyse a clickstream and update the user models:
```bash
flink run -c ch.hsr.markovshield.flink.MarkovShieldAnalyser --jobmanager jobmanager:6123 flink/target/flink-1.0-SNAPSHOT-jar-with-dependencies.jar
flink run -c ch.hsr.markovshield.flink.MarkovShieldModelUpdater --jobmanager jobmanager:6123 flink/target/flink-1.0-SNAPSHOT-jar-with-dependencies.jar
```
**Hint**: Perhaps you need to change the path of the `flink` application depending on your setup.

#### Additonal commands
```bash
java -cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.development_tools.generators.MarkovModelGenerator
java -cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar ch.hsr.markovshield.kafkastream.development_tools.generators.MarkovClickAndLoginGenerator
```

## Usefull commands

### Open shell inside the Kafka broker container
```bash
docker exec -u 0 -it deployment_broker_1 /bin/bash
```
### Create topics
To create Kafka topics, use the following commands:
```bash
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovLogins
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovClicks
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovUserModels
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovClickStreams
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovClickStreamAnalysis
kafka-topics --create --zookeeper zookeeper:2181 --replication-factor 1 --partitions 1 --topic MarkovValidatedClickStream
```

### Consume messages
To consume Kafka messages from a specific topic (e.g. `MarkovValidatedClickStream`) use:
```bash
kafka-console-consumer --zookeeper zookeeper:2181 --topic MarkovValidatedClickStream --from-beginning --property print.key=true
```

### Produce message
If you want to produce a sample click entry into a Kafka topic (e.g. `MarkovClicks`) use:
```bash
echo '61631#{"sessionUUID":"61631","clickUUID":"1000","url":"my-secret-url","urlRiskLevel":2,"timeStamp":1495602498740,"validationRequired":true}' | kafka-console-producer --broker-list localhost:9092 --topic MarkovClicks --property "parse.key=true" --property "key.separator=#";
```

### Delete all docker images
This command is only meant to be used for breaking changes:
```bash
FOR /f "tokens=*" %i IN ('docker ps -a -q') DO docker rm %i
```

## Old commands

### Build AVRO
Download [http://mirror.synyx.de/apache/avro/avro-1.8.1/java/avro-tools-1.8.1.jar](http://mirror.synyx.de/apache/avro/avro-1.8.1/java/avro-tools-1.8.1.jar)
```bash
java -cp avro-tools-1.8.1.jar org.apache.avro.tool.Main compile schema TypeReuseTest.avsc CompoundSubTypeExtended.avsc DirWithOtherAvscFiles OutputDir
```

## Publish images
To build the MarkovShield Docker images and publish them to a registry (e.g. the Docker Hub), change the `HUBPREFIX` inside the `Makefile` to your Docker Hub username/organisation name and hit `make publish`.

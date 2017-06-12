FROM openjdk:8u131-jre

MAINTAINER Philip Schmid

# Copy JAR file to right place
WORKDIR /opt
COPY deployment/MarkovShieldClickstreams/kafka-stream-2.0-jar-with-dependencies.jar /opt/

# Start the apache service
CMD sleep 10 && java -cp kafka-stream-2.0-jar-with-dependencies.jar \
  ch.hsr.markovshield.kafkastream.application.MarkovShieldClickstreams \
  --bootstrap $BOOTSTRAP \
  --resthostname $RESTHOSTNAME \
  --restport $RESTPORT \
  --zookeeper $ZOOKEEPER

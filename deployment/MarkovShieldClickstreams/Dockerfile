FROM openjdk:8u121-jre-alpine

MAINTAINER Philip Schmid

# Copy JAR file to right place
WORKDIR /opt
COPY deployment/MarkovShieldClickstreams/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/

# Start the apache service
CMD ["java", "-cp", "kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar", "ch.hsr.markovshield.kafkastream.MarkovShieldClickstreams"]

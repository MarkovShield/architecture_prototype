FROM flink:1.2

MAINTAINER Philip Schmid

# Copy files to right place
WORKDIR /opt
COPY deployment/MarkovShieldAnalyser/flink-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/

# Start the apache service
CMD flink run \
  -c ch.hsr.markovshield.flink.MarkovShieldAnalyser \
  --jobmanager $JOBMANAGER \
  flink-1.0-SNAPSHOT-jar-with-dependencies.jar

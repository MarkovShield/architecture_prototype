FROM pschmid/flink-base:1.2.0

MAINTAINER Philip Schmid

# Copy files to right place
WORKDIR /opt
COPY deployment/MarkovShieldAnalyser/flink-2.0-jar-with-dependencies.jar /opt/

# Start the apache service
CMD sleep 10 && flink run \
  -c ch.hsr.markovshield.flink.MarkovShieldAnalyser \
  --jobmanager $JOBMANAGER \
  flink-2.0-jar-with-dependencies.jar \
  --bootstrap $BOOTSTRAP \
  --redishost $REDISHOST \
  --redisport $REDISPORT
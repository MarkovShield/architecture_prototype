FROM pschmid/flink-base:1.2.0

MAINTAINER Philip Schmid

# Copy files to right place
WORKDIR /opt
COPY deployment/MarkovShieldModelUpdate/flink-2.0-jar-with-dependencies.jar /opt/

# Start the apache service
CMD sleep 60 && flink run \
  -c ch.hsr.markovshield.flink.MarkovShieldModelUpdater \
  --jobmanager $JOBMANAGER \
  flink-2.0-jar-with-dependencies.jar \
  --bootstrap $BOOTSTRAP \
  --updateinterval $UPDATEINTERVAL \
  --lookbackperiod $LOOKBACKPERIOD \
  --sessiontimeout $SESSIONTIMEOUT \
  

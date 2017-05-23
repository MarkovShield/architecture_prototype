FROM flink:1.2

MAINTAINER Philip Schmid

# Copy files to right place
WORKDIR /opt
COPY deployment/MarkovShieldAnalyser/entry_point.sh /opt/
COPY deployment/MarkovShieldAnalyser/flink-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/

# Set execute permissions on bash files
RUN chmod +x /opt/entry_point.sh

# Start the apache service
CMD ./entry_point.sh $JOBMANAGER

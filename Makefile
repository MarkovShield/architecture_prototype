#############################################
#
# MarkovShield Engine Makefile V1.0
#
#############################################

build:
		mvn clean package
		cp flink/target/flink-1.0-SNAPSHOT-jar-with-dependencies.jar deployment/MarkovShieldAnalyser
		cp flink/target/flink-1.0-SNAPSHOT-jar-with-dependencies.jar deployment/MarkovShieldModelUpdate
		cp kafka-stream/target/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar deployment/MarkovShieldClickstreams
		docker build -f deployment/MarkovShieldAnalyser/Dockerfile -t mshield-flink-analyser .
		docker build -f deployment/MarkovShieldModelUpdate/Dockerfile -t mshield-flink-modelupdater .
		docker build -f deployment/MarkovShieldClickstreams/Dockerfile -t mshield-kafka-clickstreams .

clean:
		mvn clean
		rm -rf deployment/MarkovShieldAnalyser/flink-1.0-SNAPSHOT-jar-with-dependencies.jar
		rm -rf deployment/MarkovShieldModelUpdate/flink-1.0-SNAPSHOT-jar-with-dependencies.jar
		rm -rf deployment/MarkovShieldClickstreams/kafka-stream-1.0-SNAPSHOT-jar-with-dependencies.jar

.PHONY: build clean

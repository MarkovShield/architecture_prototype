#############################################
#
# MarkovShield Engine Makefile V2.0
#
#############################################

BUILDTAG = 2.0
HUBPREFIX = markovshield

#############################################
#
# IMPORTANT: Do not change anything below
# here unless you excactly know what
# you are doing!
#
#############################################

build:
	mvn clean package

copy-jars:
	cp flink/target/flink-2.0-jar-with-dependencies.jar deployment/MarkovShieldAnalyser
	cp flink/target/flink-2.0-jar-with-dependencies.jar deployment/MarkovShieldModelUpdate
	cp kafka-stream/target/kafka-stream-2.0-jar-with-dependencies.jar deployment/MarkovShieldClickstreams

local-images: build copy-jars
	docker build -f deployment/MarkovShieldAnalyser/Dockerfile -t mshield-flink-analyser .
	docker build -f deployment/MarkovShieldModelUpdate/Dockerfile -t mshield-flink-modelupdater .
	docker build -f deployment/MarkovShieldClickstreams/Dockerfile -t mshield-kafka-clickstreams .

publish: build copy-jars
	docker build -f deployment/MarkovShieldAnalyser/Dockerfile -t $(HUBPREFIX)/mshield-flink-analyser .
	docker build -f deployment/MarkovShieldModelUpdate/Dockerfile -t $(HUBPREFIX)/mshield-flink-modelupdater .
	docker build -f deployment/MarkovShieldClickstreams/Dockerfile -t $(HUBPREFIX)/mshield-kafka-clickstreams .
	docker tag markovshield/mshield-flink-analyser markovshield/mshield-flink-analyser:$(BUILDTAG)
	docker tag markovshield/mshield-flink-modelupdater markovshield/mshield-flink-modelupdater:$(BUILDTAG)
	docker tag markovshield/mshield-kafka-clickstreams markovshield/mshield-kafka-clickstreams:$(BUILDTAG)
	docker push markovshield/mshield-flink-analyser:$(BUILDTAG)
	docker push markovshield/mshield-flink-modelupdater:$(BUILDTAG)
	docker push markovshield/mshield-kafka-clickstreams:$(BUILDTAG)

clean:
	mvn clean
	rm -rf deployment/MarkovShieldAnalyser/flink-2.0-jar-with-dependencies.jar
	rm -rf deployment/MarkovShieldModelUpdate/flink-2.0-jar-with-dependencies.jar
	rm -rf deployment/MarkovShieldClickstreams/kafka-stream-2.0-jar-with-dependencies.jar


.PHONY: build copy-jars local-images publish clean

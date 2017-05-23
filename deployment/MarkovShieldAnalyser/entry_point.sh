#!/bin/bash
flink run \
  -c ch.hsr.markovshield.flink.MarkovShieldAnalyser \
  --jobmanager $1 \
  flink-1.0-SNAPSHOT-jar-with-dependencies.jar

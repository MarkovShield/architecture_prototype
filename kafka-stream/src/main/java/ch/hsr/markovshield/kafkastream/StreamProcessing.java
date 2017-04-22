package ch.hsr.markovshield.kafkastream;

import org.apache.kafka.streams.kstream.KStreamBuilder;

public interface StreamProcessing {

    KStreamBuilder getStreamBuilder();
}

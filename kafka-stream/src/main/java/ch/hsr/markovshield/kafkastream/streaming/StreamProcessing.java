package ch.hsr.markovshield.kafkastream.streaming;

import org.apache.kafka.streams.kstream.KStreamBuilder;

public interface StreamProcessing {

    KStreamBuilder getStreamBuilder();
}

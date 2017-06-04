package ch.hsr.markovshield.kafkastream.application;

import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.Options;
import java.util.Map;

public class CustomRocksDBConfig implements RocksDBConfigSetter {

    @Override
    public void setConfig(String storeName, Options options, Map<String, Object> configs) {

        int bufferSizeInBytes = 32*1024*1024;
        options.setWriteBufferSize(bufferSizeInBytes);
        options.setMaxWriteBufferNumber(3);
        options.setMinWriteBufferNumberToMerge(1);
    }
}

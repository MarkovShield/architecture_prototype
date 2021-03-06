package ch.hsr.markovshield.utils; /**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;

public class JsonPOJOSerde<T> implements Serde<T> {

    public static final boolean MOD_MSHIELD_SMILE = false;
    public static final boolean MARKOV_SHIELD_SMILE = true;
    private final ObjectMapper mapper;
    private final Class<T> cls;


    public JsonPOJOSerde(Class<T> cls, boolean useSmile) {

        this.cls = cls;
        if (useSmile) {
            SmileFactory f = new SmileFactory();
            mapper = new ObjectMapper(f);
        } else {
            mapper = new ObjectMapper();
        }
        mapper.registerModule(new AfterburnerModule());
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<T> serializer() {
        return new Serializer<T>() {

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public byte[] serialize(String topic, T data) {
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (Exception e) {
                    throw new SerializationException("Error serializing JSON message", e);
                }
            }

            @Override
            public void close() {

            }
        };

    }

    @Override
    public Deserializer<T> deserializer() {
        return new Deserializer<T>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public T deserialize(String topic, byte[] data) {
                if (data == null) {
                    return null;
                }
                T result;
                try {
                    result = mapper.readValue(data, cls);
                } catch (Exception e) {
                    throw new SerializationException(e);
                }

                return result;
            }

            @Override
            public void close() {

            }
        };
    }
}
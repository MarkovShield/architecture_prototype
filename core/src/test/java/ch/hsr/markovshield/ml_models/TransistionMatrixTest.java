package ch.hsr.markovshield.ml_models;


import ch.hsr.markovshield.ml_models.data_helper.TransitionMatrix;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TransistionMatrixTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void serializationTest() throws JsonProcessingException {
        TransitionMatrix matrix = new TransitionMatrix(2);
        String json = mapper.writeValueAsString(matrix);
        assertThat(json, containsString("columns"));
        assertThat(json, containsString("rows"));
        assertThat(json, containsString("data"));
    }

    @Test
    public void serializationAnDeserializationTest() throws IOException {
        TransitionMatrix matrix = new TransitionMatrix(2);
        String json = mapper.writeValueAsString(matrix);
        TransitionMatrix deserializiedMatrix = mapper.readValue(json, TransitionMatrix.class);
        assertThat(deserializiedMatrix, equalTo(matrix));
    }
}

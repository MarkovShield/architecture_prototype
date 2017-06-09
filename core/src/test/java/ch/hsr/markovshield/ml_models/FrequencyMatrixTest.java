package ch.hsr.markovshield.ml_models;


import ch.hsr.markovshield.ml_models.data_helper.FrequencyMatrix;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertThat;

public class FrequencyMatrixTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void serializationTest() throws JsonProcessingException {
        FrequencyMatrix matrix = new FrequencyMatrix(2);
        String json = mapper.writeValueAsString(matrix);
        assertThat(json, containsString("columns"));
        assertThat(json, containsString("rows"));
        assertThat(json, containsString("data"));
    }

    @Test
    public void serializationAnDeserializationTest() throws IOException {
        FrequencyMatrix matrix = new FrequencyMatrix(2);
        String json = mapper.writeValueAsString(matrix);
        FrequencyMatrix deserializiedMatrix = mapper.readValue(json, FrequencyMatrix.class);
        assertThat(deserializiedMatrix, equalTo(matrix));
    }

    @Test
    public void testSave() throws Exception {
        FrequencyMatrix matrix = new FrequencyMatrix(2);
        matrix.set(1, 1, 2);
        assertThat(matrix.get(1, 1), closeTo(2, 0.001));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void invalidSave() throws Exception {
        FrequencyMatrix matrix = new FrequencyMatrix(2);
        matrix.set(1, 2, 2);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void invalidGet() throws Exception {
        FrequencyMatrix matrix = new FrequencyMatrix(2);
        matrix.get(1, 2);
    }
}

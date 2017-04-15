package ch.hsr.markovshield.ml;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DoubleMatrixTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void serializationTest() throws JsonProcessingException {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        String json = mapper.writeValueAsString(matrix);
        assertThat(json, containsString("columns"));
        assertThat(json, containsString("rows"));
        assertThat(json, containsString("data"));
    }

    @Test
    public void serializationAnDeserializationTest() throws IOException {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        String json = mapper.writeValueAsString(matrix);
        DoubleMatrix deserializedMatrix = mapper.readValue(json, DoubleMatrix.class);
        assertThat(deserializedMatrix, equalTo(matrix));
    }
}

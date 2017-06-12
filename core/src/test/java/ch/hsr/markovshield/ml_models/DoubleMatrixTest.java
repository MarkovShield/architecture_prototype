package ch.hsr.markovshield.ml_models;


import ch.hsr.markovshield.ml_models.data_helper.DoubleMatrix;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.number.IsCloseTo.closeTo;
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

    @Test
    public void testSave() throws Exception {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        matrix.set(1, 1, 2);
        assertThat(matrix.get(1, 1), closeTo(2, 0.001));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidColumnSave() throws Exception {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        matrix.set(2, 1, 2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidRowSave() throws Exception {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        matrix.set(1, 2, 2);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidColumnGet() throws Exception {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        matrix.get(2, 1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidRowGet() throws Exception {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        matrix.get(1, 2);
    }

    @Test
    public void testSaveToColumnZero() throws Exception {
        DoubleMatrix matrix = new DoubleMatrix(2, 2);
        matrix.set(0, 0, 2);
        assertThat(matrix.get(0, 0), closeTo(2, 0.001));
    }
}

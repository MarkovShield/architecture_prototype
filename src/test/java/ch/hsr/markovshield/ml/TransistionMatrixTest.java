package ch.hsr.markovshield.ml;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TransistionMatrixTest {

    @Test
    public void serializationTest() throws JsonProcessingException {
        TransitionMatrix matrix = new TransitionMatrix(2);
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(matrix);
        assertThat(s, containsString("columns"));
        assertThat(s, containsString("rows"));
        assertThat(s, containsString("data"));
    }
    @Test
    public void serializationAnDeserializationTest() throws IOException {
        TransitionMatrix matrix = new TransitionMatrix(2);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(matrix);
        TransitionMatrix matrix1 = mapper.readValue(json, TransitionMatrix.class);
        assertThat(matrix1, equalTo(matrix));
    }
}

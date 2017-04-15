package ch.hsr.markovshield.models;


import ch.hsr.markovshield.ml.DoubleMatrix;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import ch.hsr.markovshield.ml.TransitionMatrix;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TransitionModelTest {

    @Test
    public void testSerialization() throws JsonProcessingException {
        TransitionModel matrix = MarkovChainWithMatrix.train(Collections.emptyList());
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(matrix);
        assertThat(s, containsString("timeCreated"));
        assertThat(s, containsString("urlStore"));
        assertThat(s, containsString("transitionMatrix"));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {
        TransitionModel matrix = MarkovChainWithMatrix.train(Collections.emptyList());
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(matrix);

        TransitionModel transitionModel = mapper.readValue(s, TransitionModel.class);
        assertThat(transitionModel, equalTo(matrix));

    }
}

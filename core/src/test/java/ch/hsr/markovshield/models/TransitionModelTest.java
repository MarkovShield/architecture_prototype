package ch.hsr.markovshield.models;


import ch.hsr.markovshield.ml_models.TransitionModel;
import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class TransitionModelTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel model = markovChainAnalysis.train(Collections.emptyList());
        String json = mapper.writeValueAsString(model);
        assertThat(json, containsString("timeCreated"));
        assertThat(json, containsString("urlStore"));
        assertThat(json, containsString("transitionMatrix"));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel model = markovChainAnalysis.train(Collections.emptyList());
        String json = mapper.writeValueAsString(model);
        TransitionModel deserializedModel = mapper.readValue(json, TransitionModel.class);
        assertThat(deserializedModel, equalTo(model));
    }
}

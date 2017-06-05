package ch.hsr.markovshield.models;


import ch.hsr.markovshield.ml_models.MatrixFrequencyModel;
import ch.hsr.markovshield.ml_models.TransitionModel;
import ch.hsr.markovshield.ml_models.builder.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class UserModelTest {

    private ObjectMapper mapper;
    private UserModel model;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
        List<ClickStreamModel> models = new ArrayList<>();
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel transitionModel = markovChainAnalysis.train(Collections.emptyList());
        MatrixFrequencyModel frequencyModel = (new IQRFrequencyAnalysis()).train(Collections.emptyList());
        models.add(transitionModel);
        models.add(frequencyModel);
        model = new UserModel("Ivan", models);
    }

    @Test
    public void testSerialization() throws JsonProcessingException {

        String json = mapper.writeValueAsString(model);
        assertThat(json, containsString("timeCreated"));
        assertThat(json, containsString("clickStreamModels"));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {

        String json = mapper.writeValueAsString(model);

        UserModel deserializedModel = mapper.readValue(json, UserModel.class);
        assertThat(deserializedModel, equalTo(model));

    }
}

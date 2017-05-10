package ch.hsr.markovshield.models;


import ch.hsr.markovshield.ml.IQRFrequencyAnalysis;
import ch.hsr.markovshield.ml.MarkovChainWithMatrix;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class UserModelTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        TransitionModel transitionModel = MarkovChainWithMatrix.train(Collections.emptyList());
        MatrixFrequencyModel frequencyModel = (new IQRFrequencyAnalysis()).train(Collections.emptyList());

        UserModel model = new UserModel("Ivan", transitionModel, frequencyModel);

        String json = mapper.writeValueAsString(model);
        assertThat(json, containsString("timeCreated"));
        assertThat(json, containsString("clickStreamModels"));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {
        TransitionModel transitionModel = MarkovChainWithMatrix.train(Collections.emptyList());
        MatrixFrequencyModel frequencyModel = (new IQRFrequencyAnalysis()).train(Collections.emptyList());

        UserModel model = new UserModel("Ivan", transitionModel, frequencyModel);

        String json = mapper.writeValueAsString(model);

        UserModel deserializedModel = mapper.readValue(json, UserModel.class);
        assertThat(deserializedModel, equalTo(model));

    }
}

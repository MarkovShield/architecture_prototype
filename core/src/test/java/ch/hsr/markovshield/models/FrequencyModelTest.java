package ch.hsr.markovshield.models;


import ch.hsr.markovshield.ml_models.ClickStreamModel;
import ch.hsr.markovshield.ml_models.FrequencyModel;
import ch.hsr.markovshield.ml_models.ModelBuilder;
import ch.hsr.markovshield.ml_models.builder.PDFFrequencyAnalysis;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class FrequencyModelTest {

    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = new ObjectMapper();
    }

    @Test
    public void testSerialization() throws JsonProcessingException {
        ModelBuilder pdfFrequencyAnalysis = new PDFFrequencyAnalysis();
        ClickStreamModel model = pdfFrequencyAnalysis.train(Collections.emptyList());
        String json = mapper.writeValueAsString(model);
        assertThat(json, containsString("timeCreated"));
        assertThat(json, containsString("urlStore"));
        assertThat(json, containsString("frequencyMatrix"));
    }

    @Test
    public void testSerializationAndDeserialization() throws IOException {
        ModelBuilder pdfFrequencyAnalysis = new PDFFrequencyAnalysis();
        ClickStreamModel model = pdfFrequencyAnalysis.train(Collections.emptyList());
        String json = mapper.writeValueAsString(model);
        FrequencyModel deserializedModel = mapper.readValue(json, FrequencyModel.class);
        assertThat(deserializedModel, equalTo(model));
    }
}

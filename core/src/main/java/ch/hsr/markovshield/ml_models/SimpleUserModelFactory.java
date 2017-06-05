package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ClickStreamModel;
import ch.hsr.markovshield.models.UserModelFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleUserModelFactory implements UserModelFactory {

    private List<ModelBuilder> allModelBuilders;

    public SimpleUserModelFactory(ModelBuilder... modelBuilders) {
        allModelBuilders = new ArrayList<>();
        for (ModelBuilder modelBuilder : modelBuilders) {
            allModelBuilders.add(modelBuilder);
        }
    }

    @Override
    public List<ClickStreamModel> trainAllModels(Iterable<ClickStream> clickStreams) {
        List<ClickStreamModel> clickStreamModels = allModelBuilders.stream()
            .map(modelBuilder -> modelBuilder.train(clickStreams))
            .collect(Collectors.toList());
        return clickStreamModels;
    }
}

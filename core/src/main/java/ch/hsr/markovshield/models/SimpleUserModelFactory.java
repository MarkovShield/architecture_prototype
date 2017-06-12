package ch.hsr.markovshield.models;

import ch.hsr.markovshield.ml_models.ClickStreamModel;
import ch.hsr.markovshield.ml_models.ModelBuilder;
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
    public UserModel trainUserModel(Iterable<ClickStream> clickStreams, String userId) {
        List<ClickStreamModel> clickStreamModels = allModelBuilders.stream()
            .map(modelBuilder -> modelBuilder.train(clickStreams))
            .collect(Collectors.toList());
        return new UserModel(userId, clickStreamModels);
    }
}

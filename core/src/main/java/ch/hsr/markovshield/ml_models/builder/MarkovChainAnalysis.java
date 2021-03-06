package ch.hsr.markovshield.ml_models.builder;

import ch.hsr.markovshield.ml_models.ModelBuilder;
import ch.hsr.markovshield.ml_models.TransitionModel;
import ch.hsr.markovshield.ml_models.data_helper.TransitionMatrix;
import ch.hsr.markovshield.ml_models.data_helper.UrlStore;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import java.util.HashMap;
import java.util.Map;

public class MarkovChainAnalysis implements ModelBuilder {


    public static final String END_OF_CLICK_STREAM = "endOfClickStream";

    public TransitionModel train(Iterable<ClickStream> stream) {

        HashMap<String, HashMap<String, Integer>> clickCountMatrix = new HashMap<>();

        stream.forEach(clickStream -> {
            Click[] clicks = clickStream.getClicks().toArray(new Click[]{});
            for (int i = 0; i <= clicks.length - 1; i++) {
                if (i == clicks.length - 1) {
                    updateClickCount(clickCountMatrix, clicks[i].getUrl(), END_OF_CLICK_STREAM);
                } else {
                    updateClickCount(clickCountMatrix, clicks[i].getUrl(), clicks[i + 1].getUrl());
                }
            }
        });
        Map<String, Integer> urlMap = getMappings(clickCountMatrix);
        TransitionMatrix clickProbabilityMatrix = calculateProbilities(clickCountMatrix, urlMap);
        return new TransitionModel(clickProbabilityMatrix, new UrlStore(urlMap));
    }

    private static Map<String, Integer> getMappings(HashMap<String, HashMap<String, Integer>> clickCountMatrix) {
        HashMap<String, Integer> urlMapping = new HashMap<>();
        int urlCount = 0;
        for (String url : clickCountMatrix.keySet()) {
            urlMapping.put(url, urlCount++);
        }
        urlMapping.put("endOfClickStream", urlCount);
        return urlMapping;
    }

    private static TransitionMatrix calculateProbilities(HashMap<String, HashMap<String, Integer>> clickCountMatrix,
                                                         Map<String, Integer> urlMap) {
        TransitionMatrix clickProbabilityMatrix = new TransitionMatrix(urlMap.size());
        clickCountMatrix.forEach((s, stringIntegerHashMap) -> {
            Double sum = (double) stringIntegerHashMap.values().stream().mapToInt(Integer::intValue).sum();
            stringIntegerHashMap.forEach((s1, integer) -> addToProbabilityMatrix(clickProbabilityMatrix,
                s,
                s1,
                Double.valueOf(integer) / sum,
                urlMap));
        });
        return clickProbabilityMatrix;
    }

    private static void addToProbabilityMatrix(TransitionMatrix clickProbabilityMatrix,
                                               String source,
                                               String target,
                                               double probability,
                                               Map<String, Integer> urlMap) {
        int sourceIndex = urlMap.get(source);
        int targetIndex = urlMap.get(target);
        clickProbabilityMatrix.set(sourceIndex, targetIndex, probability);
    }

    private static void updateClickCount(HashMap<String, HashMap<String, Integer>> clickCountMatrix,
                                         String sourceUrl,
                                         String targetUrl) {
        if (!clickCountMatrix.containsKey(sourceUrl)) {
            HashMap<String, Integer> targetMap = new HashMap<>();
            targetMap.put(targetUrl, 1);
            clickCountMatrix.put(sourceUrl, targetMap);
        } else {
            HashMap<String, Integer> sourceMap = clickCountMatrix.get(sourceUrl);
            if (sourceMap.containsKey(targetUrl)) {
                sourceMap.put(targetUrl, sourceMap.get(targetUrl) + 1);
            } else {
                sourceMap.put(targetUrl, 1);
            }
        }
    }

}

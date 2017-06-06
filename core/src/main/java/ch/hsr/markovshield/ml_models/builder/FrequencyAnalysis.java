package ch.hsr.markovshield.ml_models.builder;

import ch.hsr.markovshield.ml_models.MatrixFrequencyModel;
import ch.hsr.markovshield.ml_models.ModelBuilder;
import ch.hsr.markovshield.ml_models.data_helper.FrequencyMatrix;
import ch.hsr.markovshield.ml_models.data_helper.UrlStore;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import java.util.HashMap;
import java.util.Map;

public abstract class FrequencyAnalysis implements ModelBuilder {

    static void addToFrequencyMatrix(FrequencyMatrix clickProbabilityMatrix,
                                     String url,
                                     double lowerBound,
                                     double upperBound,
                                     Map<String, Integer> urlMap) {
        int sourceIndex = urlMap.get(url);
        clickProbabilityMatrix.set(sourceIndex, 0, lowerBound);
        clickProbabilityMatrix.set(sourceIndex, 1, upperBound);
    }

    public MatrixFrequencyModel train(Iterable<ClickStream> stream) {
        ClickAndUrlContainer clickAndUrlContainer = calculateClicks(stream);
        FrequencyMatrix clickProbabilityMatrix = calculateFrequencies(clickAndUrlContainer.getClicks(),
            clickAndUrlContainer.getUrlMap());
        return new MatrixFrequencyModel(clickProbabilityMatrix, new UrlStore(clickAndUrlContainer.getUrlMap()));
    }

    protected abstract FrequencyMatrix calculateFrequencies(double[][] clicks, Map<String, Integer> urlMap);

    private ClickAndUrlContainer calculateClicks(Iterable<ClickStream> stream) {
        HashMap<String, HashMap<String, Double>> clickCountMatrix = new HashMap<>();
        Map<String, Integer> urlMap = new HashMap<>();

        Map<String, Integer> sessionMap = new HashMap<>();
        stream.forEach(clickStream -> {
            for (Click click :
                clickStream.getClicks()) {
                if (!urlMap.containsKey(click.getUrl())) {
                    urlMap.put(click.getUrl(), urlMap.size());
                }
                updateClickCount(clickCountMatrix, click);
            }
            sessionMap.put(clickStream.getSessionUUID(), sessionMap.size());
        });
        double[][] clicks = new double[urlMap.size()][sessionMap.size()];
        for (Map.Entry<String, Integer> x : urlMap.entrySet()
            ) {
            HashMap<String, Double> stringDoubleHashMap = clickCountMatrix.get(x.getKey());
            for (Map.Entry<String, Integer> y : sessionMap.entrySet()) {
                clicks[x.getValue()][y.getValue()] = (stringDoubleHashMap.containsKey(y.getKey())) ? stringDoubleHashMap
                    .get(y.getKey()) : 0;
            }

        }
        return new ClickAndUrlContainer(urlMap, clicks);
    }

    private static void updateClickCount(HashMap<String, HashMap<String, Double>> clickCountMatrix, Click click) {
        String sessionUUID = click.getSessionUUID();
        String url = click.getUrl();
        if (!clickCountMatrix.containsKey(url)) {
            HashMap<String, Double> frequencyMap = new HashMap<>();
            frequencyMap.put(sessionUUID, 1.0);
            clickCountMatrix.put(url, frequencyMap);
        } else {
            HashMap<String, Double> frequencyMap = clickCountMatrix.get(url);
            if (frequencyMap.containsKey(sessionUUID)) {
                frequencyMap.put(sessionUUID, frequencyMap.get(sessionUUID) + 1);
            } else {
                frequencyMap.put(sessionUUID, 1.0);
            }
        }
    }

    private class ClickAndUrlContainer {

        private final Map<String, Integer> urlMap;
        private final double[][] clicks;

        ClickAndUrlContainer(Map<String, Integer> urlMap, double[][] clicks) {
            this.urlMap = urlMap;
            this.clicks = clicks;
        }

        Map<String, Integer> getUrlMap() {
            return urlMap;
        }

        public double[][] getClicks() {
            return clicks;
        }
    }

}

package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MatrixFrequencyModel;
import ch.hsr.markovshield.models.UrlStore;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import java.util.HashMap;
import java.util.Map;

public class NormalDistributionFrequencyAnalysis implements FrequencyAnalysis {


    public MatrixFrequencyModel train(Iterable<ClickStream> stream) {
        HashMap<String, HashMap<String, Double>> clickCountMatrix = new HashMap<>();
        Map<String, Integer> urlMap = new HashMap<>();
        Map<String, Integer> sessionMap = new HashMap<>();
        stream.forEach(clickStream -> {
            frequency(clickCountMatrix, urlMap, clickStream);
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
        FrequencyMatrix clickProbabilityMatrix = calculateFrequencies(clicks, urlMap);
        return new MatrixFrequencyModel(clickProbabilityMatrix, new UrlStore(urlMap));
    }

    private void frequency(HashMap<String, HashMap<String, Double>> clickCountMatrix, Map<String, Integer> urlMap, ClickStream clickStream) {
        for (Click click :
            clickStream.getClicks()) {
            if (!urlMap.containsKey(click.getUrl())) {
                urlMap.put(click.getUrl(), urlMap.size());
            }
            updateClickCount(clickCountMatrix, click);
        }
    }

    private static FrequencyMatrix calculateFrequencies(double[][] data, Map<String, Integer> urlMap) {
        FrequencyMatrix clickFrequencyMatrix = new FrequencyMatrix(urlMap.size());
        for (Map.Entry<String, Integer> entry : urlMap.entrySet()
            ) {
            SummaryStatistics da = new SummaryStatistics();
            double[] datum = data[entry.getValue()];
            for (double v : datum) {
                da.addValue(v);
            }
            double standardDeviation = da.getStandardDeviation();
            double mean = da.getMean();
            double lowerBound = mean - 2 * standardDeviation;
            double upperBound = mean + 2 * standardDeviation;
            addToFrequencyMatrix(clickFrequencyMatrix,
                entry.getKey(),
                lowerBound,
                upperBound,
                urlMap);

        }
        return clickFrequencyMatrix;
    }

    private static void addToFrequencyMatrix(FrequencyMatrix clickProbabilityMatrix, String url, double lowerBound, double upperBound, Map<String, Integer> urlMap) {
        int sourceIndex = getIndexByUrl(urlMap, url);
        clickProbabilityMatrix.set(sourceIndex, 0, lowerBound);
        clickProbabilityMatrix.set(sourceIndex, 1, upperBound);
    }

    private static int getIndexByUrl(Map<String, Integer> urlMap, String url) {
        Integer integer = urlMap.get(url);
        return integer;
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

}

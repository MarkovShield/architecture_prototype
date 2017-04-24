package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.FrequencyModel;
import ch.hsr.markovshield.models.UrlStore;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FrequencyAnalysis {


    public static FrequencyModel train(Iterable<ClickStream> stream) {

        HashMap<String, HashMap<String, Double>> clickCountMatrix = new HashMap<>();

        stream.forEach(clickStream -> {
            for (Click click :
                clickStream.getClicks()) {
                updateClickCount(clickCountMatrix, click);
            }
        });
        Map<String, Integer> urlMap = getMappings(clickCountMatrix);
        FrequencyMatrix clickProbabilityMatrix = calculateFrequencies(clickCountMatrix, urlMap);
        return new FrequencyModel(clickProbabilityMatrix, new UrlStore(urlMap));
    }

    private static Map<String, Integer> getMappings(HashMap<String, HashMap<String, Double>> clickCountMatrix) {
        HashMap<String, Integer> urlMapping = new HashMap<>();
        int urlCount = 0;
        for (String url : clickCountMatrix.keySet()) {
            urlMapping.put(url, urlCount++);
        }
        urlMapping.put("endOfClickStream", urlCount);
        return urlMapping;
    }

    private static FrequencyMatrix calculateFrequencies(HashMap<String, HashMap<String, Double>> clickCountMatrix, Map<String, Integer> urlMap) {

        FrequencyMatrix clickFrequencyMatrix = new FrequencyMatrix(urlMap.size());
        clickCountMatrix.forEach((url, stringIntegerHashMap) -> {
            int size = stringIntegerHashMap.size();
            Collection<Double> values = stringIntegerHashMap.values();
            double[] data = new double[size];

            Iterator<Double> iterator = values.iterator();
            for(int i = 0; i < size; i++){
                data[i] = iterator.next();
            }
            // obtain data here
            DescriptiveStatistics da = new DescriptiveStatistics(data);
            double firstQuartile = da.getPercentile(25);
            double thirdQuartile = da.getPercentile(75);
            double iqr = thirdQuartile - firstQuartile;
            double lowerBound = firstQuartile - 1.5 * iqr;
            double upperBound = thirdQuartile + 1.5 * iqr;



            stringIntegerHashMap.forEach((s1, integer) -> addToFrequencyMatrix(clickFrequencyMatrix,
                url,
                lowerBound,
                upperBound,
                urlMap));
        });
        return clickFrequencyMatrix;
    }

    private static void addToFrequencyMatrix(FrequencyMatrix clickProbabilityMatrix, String url, double lowerBound, double upperBound, Map<String, Integer> urlMap) {
        int sourceIndex = getIndexByUrl(urlMap, url);
        clickProbabilityMatrix.set(sourceIndex,0, lowerBound);
        clickProbabilityMatrix.set(sourceIndex,1, upperBound);
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

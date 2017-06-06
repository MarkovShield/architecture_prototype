package ch.hsr.markovshield.ml_models.builder;

import ch.hsr.markovshield.ml_models.data_helper.FrequencyMatrix;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import java.util.Map;

public class IQRFrequencyAnalysis extends FrequencyAnalysis {

    protected FrequencyMatrix calculateFrequencies(double[][] data, Map<String, Integer> urlMap) {
        FrequencyMatrix clickFrequencyMatrix = new FrequencyMatrix(urlMap.size());
        for (Map.Entry<String, Integer> entry : urlMap.entrySet()
            ) {
            DescriptiveStatistics da = new DescriptiveStatistics(data[entry.getValue()]);
            double firstQuartile = da.getPercentile(25);
            double thirdQuartile = da.getPercentile(75);
            double iqr = thirdQuartile - firstQuartile;
            double lowerBound = firstQuartile - 1.5 * iqr;
            double upperBound = thirdQuartile + 1.5 * iqr;
            addToFrequencyMatrix(clickFrequencyMatrix,
                entry.getKey(),
                lowerBound,
                upperBound,
                urlMap);

        }
        return clickFrequencyMatrix;
    }

}

package ch.hsr.markovshield.ml_models.builder;

import ch.hsr.markovshield.ml_models.data_helper.FrequencyMatrix;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import java.util.Map;

public class PDFFrequencyAnalysis extends FrequencyAnalysis {

    protected FrequencyMatrix calculateFrequencies(double[][] data, Map<String, Integer> urlMap) {
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
}

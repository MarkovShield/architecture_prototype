package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MarkovRating;
import java.util.HashMap;
import java.util.stream.Stream;

public class MarkovChainWithHashMaps implements MarkovChain {

    HashMap<String, HashMap<String, Integer>> clickCountMatrix;
    HashMap<String, HashMap<String, Double>> clickProbabilityMatrix;

    private MarkovChainWithHashMaps() {
        clickCountMatrix = new HashMap<>();
        clickProbabilityMatrix = new HashMap<>();
    }

    public static MarkovChainWithHashMaps create() {
        return new MarkovChainWithHashMaps();
    }

    public void train(Stream<ClickStream> stream) {
        stream.forEach(clickStream -> {
            Click[] clicks = clickStream.getClicks().toArray(new Click[]{});
            for (int i = 0; i <= clicks.length-1; i++) {
                if(i == clicks.length -1 ){
                    updateClickCount(clicks[i].getUrl(),"endOfClickStream");
                }else{
                    updateClickCount(clicks[i].getUrl(),clicks[i+1].getUrl() );
                }
            }
        });
        calculateProbilities();
    }

    private void calculateProbilities() {
        clickProbabilityMatrix.clear();
        clickCountMatrix.forEach((s, stringIntegerHashMap) -> {
            HashMap<String, Double> targetMap = new HashMap<>();
            clickProbabilityMatrix.put(s, targetMap);
            Double sum = Double.valueOf(stringIntegerHashMap.values().stream().mapToInt(Integer::intValue).sum());
            stringIntegerHashMap.forEach((s1, integer) -> {
                targetMap.put(s1, Double.valueOf(integer) / sum);
            });
        });
    }

    private void updateClickCount(String sourceUrl, String targetUrl) {
        if(!clickCountMatrix.containsKey(sourceUrl)){
            HashMap<String, Integer> targetMap = new HashMap<>();
            targetMap.put(targetUrl,1);
            clickCountMatrix.put(sourceUrl,targetMap);
        }else{
            HashMap<String, Integer> sourceMap = clickCountMatrix.get(sourceUrl);
            if(sourceMap.containsKey(targetUrl)){
                sourceMap.put(targetUrl, sourceMap.get(targetUrl) + 1);
            }else{
                sourceMap.put(targetUrl, 1);
            }
        }
    }


    public double getProbabilityForClick(Click currentClick, Click newClick) {
        if(clickProbabilityMatrix.containsKey(currentClick.getUrl())){
            HashMap<String, Double> stringDoubleHashMap = clickProbabilityMatrix.get(currentClick.getUrl());
            if(stringDoubleHashMap.containsKey(newClick.getUrl())){
                return stringDoubleHashMap.get(newClick.getUrl());
            }
        }
        return 0;
    }


}

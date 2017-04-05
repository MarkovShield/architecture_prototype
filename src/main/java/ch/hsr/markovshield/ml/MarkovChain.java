package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import javafx.util.Pair;
import org.apache.commons.collections.map.HashedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MarkovChain {

    HashMap<String, HashMap<String, Integer>> clickCountMatrix;
    HashMap<String, HashMap<String, Double>> clickProbabilityMatrix;

    private MarkovChain(int i) {
        clickCountMatrix = new HashMap<>();
        clickProbabilityMatrix = new HashMap<>();
    }

    public static MarkovChain create(int i) {
        return new MarkovChain(i);
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
        return clickProbabilityMatrix.get(currentClick.getUrl()).get(newClick.getUrl());
    }


}

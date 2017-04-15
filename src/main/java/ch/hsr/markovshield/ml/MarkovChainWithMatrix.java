package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MarkovChainWithMatrix implements MarkovChain {
    HashMap<String, Integer> urlMapping;
    HashMap<String, HashMap<String, Integer>> clickCountMatrix;
    TransistionMatrix clickProbabilityMatrix;

    public MarkovChainWithMatrix() {
        this.urlMapping = new HashMap<>();
        this.clickCountMatrix = new HashMap<>();
    }

    public void train(Stream<ClickStream> stream) {
        stream.forEach(clickStream -> {
            Click[] clicks = clickStream.getClicks().toArray(new Click[]{});
            for (int i = 0; i <= clicks.length - 1; i++) {
                if (i == clicks.length - 1) {
                    updateClickCount(clicks[i].getUrl(), "endOfClickStream");
                } else {
                    updateClickCount(clicks[i].getUrl(), clicks[i + 1].getUrl());
                }
            }
        });
        addMappings();
        calculateProbilities();
    }

    private void addMappings() {
        int urlCount = 0;
        for (String url : clickCountMatrix.keySet()) {
            urlMapping.put(url, urlCount++);
        }
        urlMapping.put("endOfClickStream", urlCount);
    }

    private void calculateProbilities() {
        clickCountMatrix.forEach((s, stringIntegerHashMap) -> {
            Double sum = Double.valueOf(stringIntegerHashMap.values().stream().mapToInt(Integer::intValue).sum());
            stringIntegerHashMap.forEach((s1, integer) -> {
                addToProbabilityMatrix(s,s1,  Double.valueOf(integer) / sum);
            });
        });
    }

    private void addToProbabilityMatrix(String source, String target, double probability) {
        if(this.clickProbabilityMatrix == null){
            this.clickProbabilityMatrix = new TransistionMatrix(urlMapping.size());
        }
        int sourceIndex = getIndexByUrl(source);
        int targetIndex = getIndexByUrl(target);
        this.clickProbabilityMatrix.set(sourceIndex, targetIndex, probability);
    }

    private int getIndexByUrl(String url) {
        Integer integer = this.urlMapping.get(url);
        return integer;
    }

    private void updateClickCount(String sourceUrl, String targetUrl) {
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

    public double getProbabilityForClick(Click currentClick, Click newClick) {
        return clickProbabilityMatrix.get(getIndexByUrl(currentClick.getUrl()),getIndexByUrl(newClick.getUrl()));
    }

}

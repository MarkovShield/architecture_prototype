package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MarkovChain {
    int[][] clickCountArray;
    Double[][] clickProbabilityArray;
    HashMap<String, Integer> urlMapping;

    private MarkovChain(int i) {
        clickCountArray = new int[i+1][i+1];
        clickProbabilityArray = new Double[i+1][i+1];
        urlMapping = new HashMap<>();
        urlMapping.put("index.html", 0);
        urlMapping.put("news.html", 1);
        urlMapping.put("logout.html", 2);
        urlMapping.put("endOfClickStream", 3);

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
        for(int i = 0; i < clickCountArray.length; i++){
            Double sum = Double.valueOf(IntStream.of(clickCountArray[i]).sum());
            for(int y = 0; y < clickCountArray[i].length; y ++){
                clickProbabilityArray[i][y] = Double.valueOf( clickCountArray[i][y])/sum;
            }

        }
    }

    private void updateClickCount(String sourceUrl, String targetUrl) {
        clickCountArray[getUrlId(sourceUrl)][getUrlId(targetUrl)] += 1;

    }

    private Integer getUrlId(String targetUrl) {
        return urlMapping.get(targetUrl);
    }


    public double getProbabilityForClick(Click currentClick, Click newClick) {
        return clickProbabilityArray[getUrlId(currentClick.getUrl())][getUrlId(newClick.getUrl())];
    }


}

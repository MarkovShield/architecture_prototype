package ch.hsr.markovshield.models;

public class UrlFrequencies {

    private final String url;
    private final int urlRiskLevel;
    private int frequencyCounter;

    public UrlFrequencies(String url, int frequencyCounter, int urlRiskLevel) {
        this.url = url;
        this.frequencyCounter = frequencyCounter;
        this.urlRiskLevel = urlRiskLevel;
    }

    public void increaseFrequencyCounter() {
        frequencyCounter += 1;
    }

    public int getFrequencyCounter() {
        return frequencyCounter;
    }

    public int getUrlRiskLevel() {
        return urlRiskLevel;
    }

    public String getUrl() {
        return url;
    }
}
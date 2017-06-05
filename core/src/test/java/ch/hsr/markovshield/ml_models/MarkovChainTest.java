package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import junit.framework.TestCase;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkovChainTest extends TestCase {


    private List<ClickStream> trainingSet;

    public void setUp() throws Exception {
        super.setUp();
        trainingSet = new ArrayList<>();
        List<Click> clicks = new ArrayList<>();
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "2", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "5", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "6", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        trainingSet.add(new ClickStream("Kilian", "97572", clicks));
        List<Click> clicks2 = new ArrayList<>();
        clicks2.add(new Click("97573", "7", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks2.add(new Click("97573", "8", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        trainingSet.add(new ClickStream("Kilian", "97573", clicks2));
        List<Click> clicks3 = new ArrayList<>();
        clicks3.add(new Click("97574", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks3.add(new Click("97574", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks3.add(new Click("97574", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        trainingSet.add(new ClickStream("Kilian", "97574", clicks3));
    }

    @Test
    public void testMarkovChainWithMatrix() {

        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel train = markovChainAnalysis.train(trainingSet);
        double indexNewsProbability = train.getProbabilityForClick(
            "index.html",
            "news.html");
        double newsIndexProbability = train.getProbabilityForClick(
            "news.html",
            "index.html");
        double newsNewsProbability = train.getProbabilityForClick(
            "news.html",
            "news.html");
        double newsLogoutProbability = train.getProbabilityForClick(
            "news.html",
            "logout.html");
        assertEquals(0.5d, indexNewsProbability, 1e-3);
        assertEquals(1d / 4d, newsIndexProbability, 1e-3);
        assertEquals(2d / 4d, newsNewsProbability, 1e-3);
        assertEquals(1d / 4d, newsLogoutProbability, 1e-3);
    }

    @Test
    public void testMarkovChainWithClick() {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel train = markovChainAnalysis.train(trainingSet);
        double newsIndexProbability = train.getProbabilityForClick(
            "news.html",
            "index.html");
        double newsNewsProbability = train.getProbabilityForClick(
            "news.html",
            "news.html");
        double newsLogoutProbability = train.getProbabilityForClick(
            "news.html",
            "logout.html");
        double newsIndexProbabilityWithClick = train.getProbabilityForClick(new Click("1", "1",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L)), false),
            new Click("1", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));

        double newsNewsProbabilityWithClick = train.getProbabilityForClick(new Click("1", "1",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L)), false),
            new Click("1", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        double newsLogoutProbabilityWithClick = train.getProbabilityForClick(new Click("1", "1",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L)), false),
            new Click("1", "1", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        assertEquals(newsIndexProbabilityWithClick, newsIndexProbability, 1e-3);
        assertEquals(newsNewsProbabilityWithClick, newsNewsProbability, 1e-3);
        assertEquals(newsLogoutProbabilityWithClick, newsLogoutProbability, 1e-3);
    }

    @Test
    public void testMarkovChainWithMatrixEmptySet() {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        Iterable<ClickStream> x = Collections.emptyList();
        TransitionModel train = markovChainAnalysis.train(x);

        double indexNewsProbability = train.getProbabilityForClick(new Click("1", "1",
                "index.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L)), false),
            new Click("1", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        double newsIndexProbability = train.getProbabilityForClick(new Click("1", "2",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L)), false),
            new Click("1", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));

        assertEquals(0d, indexNewsProbability, 1e-3);
        assertEquals(0d, newsIndexProbability, 1e-3);
    }
}
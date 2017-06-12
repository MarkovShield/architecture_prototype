package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.ml_models.builder.MarkovChainAnalysis;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import org.junit.Before;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertEquals;

public class MarkovChainTest {


    private List<ClickStream> trainingSet;

    @Before
    public void setUp() throws Exception {
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

    @Test
    public void clickStreamScoreWithSingleClick() throws Exception {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel train = markovChainAnalysis.train(trainingSet);
        List<Click> clicks = new LinkedList<>();
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); // 1 * 2
        ClickStream newClickStream = new ClickStream("user", "session", clicks);
        double score = train.clickStreamScore(newClickStream);
        assertThat(score, closeTo(2, 0.001));
    }

    @Test
    public void clickStreamScoreWithoutClick() throws Exception {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel train = markovChainAnalysis.train(trainingSet);
        List<Click> clicks = new LinkedList<>();
        ClickStream newClickStream = new ClickStream("user", "session", clicks);
        double score = train.clickStreamScore(newClickStream);
        assertThat(score, closeTo(0, 0.001));
    }

    @Test
    public void clickStreamScore() throws Exception {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel train = markovChainAnalysis.train(trainingSet);
        List<Click> clicks = new LinkedList<>();
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.5 * 2
        clicks.add(new Click("97572", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.5 * 2
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.75 * 2
        clicks.add(new Click("97572", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.5 * 2
        clicks.add(new Click("97572", "1", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.75 * 2

        ClickStream newClickStream = new ClickStream("user", "session", clicks);
        double score = train.clickStreamScore(newClickStream);
        assertThat(score, closeTo(6, 0.001));
    }

    @Test
    public void clickStreamScoreWithDifferentRiskLevels() throws Exception {
        MarkovChainAnalysis markovChainAnalysis = new MarkovChainAnalysis();
        TransitionModel train = markovChainAnalysis.train(trainingSet);
        List<Click> clicks = new LinkedList<>();
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        clicks.add(new Click("97572", "1", "news.html", 10, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.5 * 11
        clicks.add(new Click("97572", "1", "news.html", 5, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.5 * 6
        clicks.add(new Click("97572", "1", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),
            false)); //0.75 * 2
        ClickStream newClickStream = new ClickStream("user", "session", clicks);
        double score = train.clickStreamScore(newClickStream);
        assertThat(score, closeTo(10, 0.001));
    }
}
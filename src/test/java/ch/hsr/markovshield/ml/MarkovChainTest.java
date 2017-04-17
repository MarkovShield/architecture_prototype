package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.TransitionModel;
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
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks.add(new Click("97572", "2", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks.add(new Click("97572", "5", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks.add(new Click("97572", "6", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        trainingSet.add(new ClickStream("Kilian", "97572", clicks));
        List<Click> clicks2 = new ArrayList<>();
        clicks2.add(new Click("97573", "7", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks2.add(new Click("97573", "8", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        trainingSet.add(new ClickStream("Kilian", "97573", clicks2));
        List<Click> clicks3 = new ArrayList<>();
        clicks3.add(new Click("97574", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks3.add(new Click("97574", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L))));
        clicks3.add(new Click("97574", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        trainingSet.add(new ClickStream("Kilian", "97574", clicks3));
    }

    @Test
    public void testMarkovChainWithMatrix() {
        TransitionModel train = MarkovChainWithMatrix.train(trainingSet);
        double indexNewsProbability = train.getProbabilityForClick(new Click("1", "1",
                "index.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L))),
            new Click("1", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        double newsIndexProbability = train.getProbabilityForClick(new Click("1", "2",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L))),
            new Click("1", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        double newsNewsProbability = train.getProbabilityForClick(new Click("1", "1",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L))),
            new Click("1", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        double newsLogoutProbability = train.getProbabilityForClick(new Click("1", "1",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L))),
            new Click("1", "1", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        assertEquals(0.5d, indexNewsProbability, 1e-3);
        assertEquals(1d / 4d, newsIndexProbability, 1e-3);
        assertEquals(2d / 4d, newsNewsProbability, 1e-3);
        assertEquals(1d / 4d, newsLogoutProbability, 1e-3);
    }

    @Test
    public void testMarkovChainWithMatrixEmptySet() {
        Iterable<ClickStream> x = Collections.emptyList();
        TransitionModel train = MarkovChainWithMatrix.train(x);

        double indexNewsProbability = train.getProbabilityForClick(new Click("1", "1",
                "index.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L))),
            new Click("1", "1", "news.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));
        double newsIndexProbability = train.getProbabilityForClick(new Click("1", "2",
                "news.html", 1,
                Date.from(Instant.ofEpochMilli(1491390672752L))),
            new Click("1", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L))));

        assertEquals(0d, indexNewsProbability, 1e-3);
        assertEquals(0d, newsIndexProbability, 1e-3);
    }
}
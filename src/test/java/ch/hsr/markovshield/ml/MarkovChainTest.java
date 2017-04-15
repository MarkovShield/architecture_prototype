package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.TransitionModel;
import junit.framework.TestCase;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MarkovChainTest extends TestCase {


    private List<ClickStream> trainingSet;

    public void setUp() throws Exception {
        super.setUp();
        trainingSet = new ArrayList<>();
        List<Click> clicks = new ArrayList<>();
        clicks.add(new Click("97572", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index2.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index3.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index4.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index5.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index6.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index7.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index8.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index9.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index10.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index11.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index12.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index13.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index14.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index15.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index16.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index17.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index18.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index19.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index20.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index21.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index22.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index23.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index24.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index25.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index26.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index27.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index28.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        trainingSet.add(new ClickStream("Kilian", "97572", clicks, null));
        List<Click> clicks2 = new ArrayList<>();
        clicks2.add(new Click("97573", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks2.add(new Click("97573", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        trainingSet.add(new ClickStream("Kilian", "97573",clicks2, null));
        List<Click> clicks3 = new ArrayList<>();
        clicks3.add(new Click("97574", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks3.add(new Click("97574", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks3.add(new Click("97574", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        trainingSet.add(new ClickStream("Kilian", "97574",clicks3, null));
    }

    @Test
    public void testMarkovChainWithMatrix(){
        TransitionModel train = MarkovChainWithMatrix.train(trainingSet);
        double indexNewsProbability = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double newsIndexProbability = train.getProbabilityForClick(new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double newsNewsProbability = train.getProbabilityForClick(new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double newsLogoutProbability = train.getProbabilityForClick(new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));

        assertEquals(0.5d, indexNewsProbability, 1e-3);
        assertEquals(1d/4d, newsIndexProbability, 1e-3);
        assertEquals(2d/4d, newsNewsProbability, 1e-3);
        assertEquals(1d/4d, newsLogoutProbability, 1e-3);
    }

}
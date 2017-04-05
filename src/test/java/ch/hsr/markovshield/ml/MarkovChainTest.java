package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import junit.framework.TestCase;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MarkovChainTest extends TestCase {

    @Test
    public void testMarkovChain(){
        List<ClickStream> trainingSet = new ArrayList<>();
        List<Click> clicks = new ArrayList<>();
        clicks.add(new Click("97572", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks.add(new Click("97572", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        trainingSet.add(new ClickStream("Kilian", "97572",clicks, null));
        List<Click> clicks2 = new ArrayList<>();
        clicks2.add(new Click("97573", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks2.add(new Click("97573", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        trainingSet.add(new ClickStream("Kilian", "97573",clicks2, null));
        List<Click> clicks3 = new ArrayList<>();
        clicks3.add(new Click("97574", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks3.add(new Click("97574", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        clicks3.add(new Click("97574", "logout.html", Date.from(Instant.ofEpochMilli(1491390672752L) )));
        trainingSet.add(new ClickStream("Kilian", "97574",clicks3, null));
        MarkovChain markovChain = MarkovChain.create(3);
        markovChain.train(trainingSet.stream());
        double p = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double x = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double c = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xa = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));

        assertEquals(0.5d, p, 1e-3);
        assertEquals(true, true);
    }

}
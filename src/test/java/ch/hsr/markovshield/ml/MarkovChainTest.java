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
    public void testMarkovChainWithHashMaps(){
        MarkovChainWithHashMaps markovChain = MarkovChainWithHashMaps.create();
        markovChain.train(trainingSet.stream());
        double p = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double x = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double c = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xa = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));

        assertEquals(0.5d, p, 1e-3);
        assertEquals(true, true);
    }
    @Test
    public void testMarkovChainWithMatrix(){
        TransitionModel train = MarkovChainWithMatrix.train(trainingSet);
        double p = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double x = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double c = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xa = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));

        assertEquals(0.5d, p, 1e-3);
        assertEquals(true, true);
    }

    @Test
    public void testSpeed(){
        long l = System.nanoTime();
        TransitionModel train = MarkovChainWithMatrix.train(trainingSet);
        long l2 = System.nanoTime();
        double p = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double x = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double c = train.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xa = train.getProbabilityForClick(new Click("1", "index2.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xb = train.getProbabilityForClick(new Click("1", "index3.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xc = train.getProbabilityForClick(new Click("1", "index4.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xd = train.getProbabilityForClick(new Click("1", "index5.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xe = train.getProbabilityForClick(new Click("1", "index6.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xf = train.getProbabilityForClick(new Click("1", "index10.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xg = train.getProbabilityForClick(new Click("1", "index12.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xh = train.getProbabilityForClick(new Click("1", "index13.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xj = train.getProbabilityForClick(new Click("1", "index14.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xk = train.getProbabilityForClick(new Click("1", "index17.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xl = train.getProbabilityForClick(new Click("1", "index20.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xm = train.getProbabilityForClick(new Click("1", "index21.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xn = train.getProbabilityForClick(new Click("1", "index22.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xo = train.getProbabilityForClick(new Click("1", "index23.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xp = train.getProbabilityForClick(new Click("1", "index24.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        long l3 = System.nanoTime();
        System.out.println("Matrixtraining: " +  (l2 - l));
        System.out.println("Matrixquery: " + (l3 - l2));
    }

    @Test
    public void testSpeedWithHashMaps(){
        long l = System.nanoTime();
        MarkovChainWithHashMaps markovChain = MarkovChainWithHashMaps.create();
        markovChain.train(trainingSet.stream());
        long l2 = System.nanoTime();
        double p = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double x = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double c = markovChain.getProbabilityForClick(new Click("1", "index.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xa = markovChain.getProbabilityForClick(new Click("1", "index2.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xb = markovChain.getProbabilityForClick(new Click("1", "index3.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xc = markovChain.getProbabilityForClick(new Click("1", "index4.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xd = markovChain.getProbabilityForClick(new Click("1", "index5.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xe = markovChain.getProbabilityForClick(new Click("1", "index6.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xf = markovChain.getProbabilityForClick(new Click("1", "index10.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xg = markovChain.getProbabilityForClick(new Click("1", "index12.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xh = markovChain.getProbabilityForClick(new Click("1", "index13.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xj = markovChain.getProbabilityForClick(new Click("1", "index14.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xk = markovChain.getProbabilityForClick(new Click("1", "index17.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xl = markovChain.getProbabilityForClick(new Click("1", "index20.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xm = markovChain.getProbabilityForClick(new Click("1", "index21.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xn = markovChain.getProbabilityForClick(new Click("1", "index22.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xo = markovChain.getProbabilityForClick(new Click("1", "index23.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        double xp = markovChain.getProbabilityForClick(new Click("1", "index24.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )), new Click("1", "news.html", Date.from(Instant.ofEpochMilli(1491390672752L)  )));
        long l3 = System.nanoTime();
        System.out.println("HashMaptraining: " +  (l2 - l));
        System.out.println("HashMapquery: " + (l3 - l2));
    }
}
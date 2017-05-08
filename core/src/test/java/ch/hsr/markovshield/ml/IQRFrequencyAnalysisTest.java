package ch.hsr.markovshield.ml;

import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.MatrixFrequencyModel;
import junit.framework.TestCase;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class IQRFrequencyAnalysisTest extends TestCase {

    private List<ClickStream> trainingSet;

    public void setUp() throws Exception {
        super.setUp();
        trainingSet = new ArrayList<>();
        List<Click> clicks = new ArrayList<>();
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "2", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "5", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97572", "6", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        trainingSet.add(new ClickStream("Kilian", "97572", clicks));
        List<Click> clicks2 = new ArrayList<>();
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks2.add(new Click("97573", "7", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks2.add(new Click("97573", "8", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        trainingSet.add(new ClickStream("Kilian", "97573", clicks2));
        List<Click> clicks3 = new ArrayList<>();
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));

        clicks3.add(new Click("97574", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks3.add(new Click("97574", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks3.add(new Click("97574", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        trainingSet.add(new ClickStream("Kilian", "97574", clicks3));
        List<Click> clicks4 = new ArrayList<>();
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));

        clicks3.add(new Click("97575", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks3.add(new Click("97575", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        clicks3.add(new Click("97575", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        trainingSet.add(new ClickStream("Kilian", "97575", clicks4));
        List<Click> clicks5 = new ArrayList<>();
        clicks3.add(new Click("97576", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        //clicks3.add(new Click("97576", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)),false));
        trainingSet.add(new ClickStream("Kilian", "97576", clicks5));
    }

    @Test
    public void testMarkovChainWithMatrix() {
        IQRFrequencyAnalysis x = new IQRFrequencyAnalysis();
        MatrixFrequencyModel train = x.train(trainingSet);
        double newsLowerBound = train.getFrequencyLowerBound(
            "news.html");
        double newsUpperBound = train.getFrequencyLowerBound(
            "news.html");
//        assertEquals(1d / 4d, newsLowerBound, 1e-3);
//        assertEquals(2d / 4d, newsUpperBound, 1e-3);
    }

}
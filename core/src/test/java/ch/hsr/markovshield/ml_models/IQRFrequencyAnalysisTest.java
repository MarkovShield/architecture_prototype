package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.ml_models.builder.IQRFrequencyAnalysis;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import org.junit.Before;
import org.junit.Test;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class IQRFrequencyAnalysisTest {

    private List<ClickStream> trainingSet;

    @Before
    public void setUp() throws Exception {
        trainingSet = new ArrayList<>();
        List<Click> clicks = new ArrayList<>();
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "2", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "5", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "6", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "7", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "8", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "9", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "11", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "12", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "13", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "14", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "15", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "16", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97572", clicks));
        List<Click> clicks2 = new ArrayList<>();
        clicks2.add(new Click("97573", "17", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "18", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "19", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "20", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "21", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "22", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "23", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "24", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "25", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97573", clicks2));
        List<Click> clicks3 = new ArrayList<>();
        clicks3.add(new Click("97574", "26", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "27", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "28", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "29", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "30", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "31", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));

        clicks3.add(new Click("97574", "32", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "33", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "34", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97574", clicks3));
        List<Click> clicks4 = new ArrayList<>();
        clicks4.add(new Click("97575", "35", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "36", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "37", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "38", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "39", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "40", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));

        clicks4.add(new Click("97575", "41", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "42", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575", "43", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks4.add(new Click("97575",
            "44",
            "thisIsARandomUrl.html",
            1,
            Date.from(Instant.ofEpochMilli(1491390672752L)),
            false));
        trainingSet.add(new ClickStream("Kilian", "97575", clicks4));

    }

    @Test
    public void testFrequency() {
        IQRFrequencyAnalysis x = new IQRFrequencyAnalysis();
        FrequencyModel trainedModel = x.train(trainingSet);
        assertThat(trainedModel.getFrequencyLowerBound("thisIsARandomUrl.html"), closeTo(-1.125, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("thisIsARandomUrl.html"), closeTo(1.875, 0.001));
        assertThat(trainedModel.getFrequencyLowerBound("index.html"), closeTo(-0.125, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("index.html"), closeTo(2.875, 0.001));
        assertThat(trainedModel.getFrequencyLowerBound("news.html"), closeTo(0.25, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("news.html"), closeTo(18.25, 0.001));
        assertThat(trainedModel.getFrequencyLowerBound("logout.html"), closeTo(1, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("logout.html"), closeTo(1, 0.001));

    }

    @Test
    public void testFrequencyWithSingleClickStream() {
        IQRFrequencyAnalysis x = new IQRFrequencyAnalysis();
        FrequencyModel trainedModel = x.train(Collections.singletonList(trainingSet.get(0)));
        assertThat(trainedModel.getFrequencyLowerBound("news.html"), closeTo(13, 0.001));
        assertThat(trainedModel.getFrequencyLowerBound("index.html"), closeTo(2, 0.001));
        assertThat(trainedModel.getFrequencyLowerBound("logout.html"), closeTo(1, 0.001));

    }

    @Test
    public void testFrequencyWithoutClickStream() {
        IQRFrequencyAnalysis x = new IQRFrequencyAnalysis();
        FrequencyModel trainedModel = x.train(Collections.emptyList());
        assertThat(trainedModel.getFrequencyLowerBound("someUrl"), closeTo(0, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("someUrl"), closeTo(0, 0.001));
    }

    @Test
    public void testFrequnecyWithSingleClick() {
        IQRFrequencyAnalysis x = new IQRFrequencyAnalysis();
        ClickStream o = new ClickStream("testuser",
            "97576",
            Collections.singletonList(new Click("97576",
                "9",
                "index.html",
                1,
                Date.from(Instant.ofEpochMilli(1491390672752L)),
                false)));
        List<ClickStream> stream = Collections.singletonList(o);
        FrequencyModel trainedModel = x.train(stream);

        assertThat(trainedModel.getFrequencyLowerBound("index.html"), closeTo(1, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("index.html"), closeTo(1, 0.001));
        assertThat(trainedModel.getFrequencyLowerBound("someOtherUrl"), closeTo(0, 0.001));
        assertThat(trainedModel.getFrequencyUpperBound("someOtherUrl"), closeTo(0, 0.001));

    }
}
package ch.hsr.markovshield.ml_models;

import ch.hsr.markovshield.ml_models.builder.IQRFrequencyAnalysis;
import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import ch.hsr.markovshield.models.ValidatedClickStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import org.junit.Test;
import java.io.IOException;
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
        clicks.add(new Click("97572", "1", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "2", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "4", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "5", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97572", "6", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97572", clicks));
        List<Click> clicks2 = new ArrayList<>();
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97573", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "7", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks2.add(new Click("97573", "8", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97573", clicks2));
        List<Click> clicks3 = new ArrayList<>();
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97574", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));

        clicks3.add(new Click("97574", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97574", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97574", clicks3));
        List<Click> clicks4 = new ArrayList<>();
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks.add(new Click("97575", "3", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));

        clicks3.add(new Click("97575", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97575", "10", "news.html", 0, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        clicks3.add(new Click("97575", "11", "logout.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
        trainingSet.add(new ClickStream("Kilian", "97575", clicks4));
        List<Click> clicks5 = new ArrayList<>();
        clicks3.add(new Click("97576", "9", "index.html", 1, Date.from(Instant.ofEpochMilli(1491390672752L)), false));
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

    @Test
    public void testWithFakedData() throws IOException {
        String clickStream1String = "{\"userName\":\"Ivan\",\"sessionUUID\":\"87455\",\"clicks\":[{\"sessionUUID\":\"87455\",\"clickUUID\":\"1269943442\",\"url\":\"index.html\",\"urlRiskLevel\":0,\"timeStamp\":1494241362356,\"validationRequired\":false},{\"sessionUUID\":\"87455\",\"clickUUID\":\"-1156578755\",\"url\":\"overview.html\",\"urlRiskLevel\":2,\"timeStamp\":1494241362356,\"validationRequired\":true}],\"clickStreamValidation\":{\"userName\":\"Ivan\",\"sessionUUID\":\"87455\",\"clickUUID\":\"-1156578755\",\"validationScore\":0.0,\"rating\":\"OK\",\"timeCreated\":1493790228734}}";
        String clickStream2String = "{\"userName\":\"Ivan\",\"sessionUUID\":\"10843\",\"clicks\":[{\"sessionUUID\":\"10843\",\"clickUUID\":\"1213490874\",\"url\":\"logout.html\",\"urlRiskLevel\":1,\"timeStamp\":1494241333901,\"validationRequired\":false},{\"sessionUUID\":\"10843\",\"clickUUID\":\"-1344310799\",\"url\":\"news.html\",\"urlRiskLevel\":0,\"timeStamp\":1494241333901,\"validationRequired\":false},{\"sessionUUID\":\"10843\",\"clickUUID\":\"1729819448\",\"url\":\"index.html\",\"urlRiskLevel\":0,\"timeStamp\":1494241333901,\"validationRequired\":false},{\"sessionUUID\":\"10843\",\"clickUUID\":\"1294278680\",\"url\":\"logout.html\",\"urlRiskLevel\":1,\"timeStamp\":1494241333901,\"validationRequired\":false}],\"clickStreamValidation\":{\"userName\":\"Ivan\",\"sessionUUID\":\"10843\",\"clickUUID\":\"1294278680\",\"validationScore\":0.0,\"rating\":\"UNEVALUDATED\",\"timeCreated\":1493790200706}}";
        String clickStream3String = "{\"userName\":\"Ivan\",\"sessionUUID\":\"46887\",\"clicks\":[{\"sessionUUID\":\"46887\",\"clickUUID\":\"895581624\",\"url\":\"overview.html\",\"urlRiskLevel\":2,\"timeStamp\":1494241291064,\"validationRequired\":true},{\"sessionUUID\":\"46887\",\"clickUUID\":\"2042031669\",\"url\":\"index.html\",\"urlRiskLevel\":0,\"timeStamp\":1494241291064,\"validationRequired\":false},{\"sessionUUID\":\"46887\",\"clickUUID\":\"1449483682\",\"url\":\"overview.html\",\"urlRiskLevel\":2,\"timeStamp\":1494241291064,\"validationRequired\":true},{\"sessionUUID\":\"46887\",\"clickUUID\":\"-780876205\",\"url\":\"overview.html\",\"urlRiskLevel\":2,\"timeStamp\":1494241291064,\"validationRequired\":true},{\"sessionUUID\":\"46887\",\"clickUUID\":\"1729992073\",\"url\":\"logout.html\",\"urlRiskLevel\":1,\"timeStamp\":1494241291064,\"validationRequired\":false},{\"sessionUUID\":\"46887\",\"clickUUID\":\"-423064369\",\"url\":\"news.html\",\"urlRiskLevel\":0,\"timeStamp\":1494241291064,\"validationRequired\":false}],\"clickStreamValidation\":{\"userName\":\"Ivan\",\"sessionUUID\":\"46887\",\"clickUUID\":\"-780876205\",\"validationScore\":0.0,\"rating\":\"OK\",\"timeCreated\":1493790157921}}";

        ObjectMapper mapper = new ObjectMapper();

        ValidatedClickStream clickStream1 = mapper.readValue(clickStream1String, ValidatedClickStream.class);
        ValidatedClickStream clickStream2 = mapper.readValue(clickStream2String, ValidatedClickStream.class);
        ValidatedClickStream clickStream3 = mapper.readValue(clickStream3String, ValidatedClickStream.class);
        List<ClickStream> clickStreamList = new ArrayList<>();
        clickStreamList.add(clickStream1);
        clickStreamList.add(clickStream2);
        clickStreamList.add(clickStream3);
        IQRFrequencyAnalysis x = new IQRFrequencyAnalysis();
        MatrixFrequencyModel train = x.train(clickStreamList);
        System.out.println(train);
    }
}
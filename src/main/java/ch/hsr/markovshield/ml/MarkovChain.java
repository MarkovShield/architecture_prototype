package ch.hsr.markovshield.ml;


import ch.hsr.markovshield.models.Click;
import ch.hsr.markovshield.models.ClickStream;
import java.util.stream.Stream;

public interface MarkovChain {

    void train(Stream<ClickStream> stream);
    double getProbabilityForClick(Click currentClick, Click newClick);
}

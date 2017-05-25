package ch.hsr.markovshield.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransitionMatrix extends DoubleMatrix {

    @JsonCreator
    private TransitionMatrix(@JsonProperty ("rows") int rows, @JsonProperty ("columns") int columns, @JsonProperty ("data") double[] data) {
        super(rows, columns, data);
    }

    public TransitionMatrix(int initialCapacity) {
        super(initialCapacity, initialCapacity);
    }

}

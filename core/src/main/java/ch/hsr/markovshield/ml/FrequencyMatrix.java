package ch.hsr.markovshield.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by maede on 24.04.2017.
 */
public class FrequencyMatrix extends DoubleMatrix {

    @JsonCreator
    private FrequencyMatrix(@JsonProperty ("rows") int rows, @JsonProperty ("columns") int columns, @JsonProperty ("data") double[] data) {
        super(rows, columns, data);
    }

    public FrequencyMatrix(int initialCapacity) {
        super(initialCapacity, 2);
    }

}

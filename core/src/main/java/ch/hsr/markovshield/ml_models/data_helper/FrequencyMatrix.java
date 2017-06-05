package ch.hsr.markovshield.ml_models.data_helper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FrequencyMatrix extends DoubleMatrix {

    @JsonCreator
    private FrequencyMatrix(@JsonProperty ("rows") int rows,
                            @JsonProperty ("columns") int columns,
                            @JsonProperty ("data") double[] data) {
        super(rows, columns, data);
    }

    public FrequencyMatrix(int initialCapacity) {
        super(initialCapacity, 2);
    }

}

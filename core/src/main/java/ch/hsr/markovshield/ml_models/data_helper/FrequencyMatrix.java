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

    @Override
    public double get(int col, int row) {
        if (row > 1) {
            throw new UnsupportedOperationException(
                "It's not possible to insert more than two rows of data into FrequencyMatrix");
        }
        return super.get(col, row);

    }

    @Override
    public void set(int col, int row, double value) {
        if (row > 1) {
            throw new UnsupportedOperationException(
                "It's not possible to insert more than two rows of data into FrequencyMatrix");
        }
        super.set(col, row, value);
    }
}

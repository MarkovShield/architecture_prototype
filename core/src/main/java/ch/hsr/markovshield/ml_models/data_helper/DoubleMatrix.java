package ch.hsr.markovshield.ml_models.data_helper;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@JsonAutoDetect (fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DoubleMatrix implements Serializable {

    private double[] data;
    private int rows;
    private int columns;

    @JsonCreator
    protected DoubleMatrix(@JsonProperty ("rows") int rows,
                           @JsonProperty ("columns") int columns,
                           @JsonProperty ("data") double[] data) {
        this.rows = rows;
        this.columns = columns;
        this.data = data;
    }

    public DoubleMatrix(int initialCols, int initialRows) {
        this.rows = initialRows;
        this.columns = initialCols;
        data = new double[initialCols * initialRows];
    }

    public double get(int col, int row) {
        boundCheck(col, row);
        return data[getIndex(col, row, columns)];
    }

    private static int getIndex(int col, int row, int width) {
        return row * width + col;
    }

    private void boundCheck(int col, int row) {
        if (row >= rows || col >= columns) {
            throw new IllegalArgumentException("Invalid access to doublematrix");
        }
    }

    public void set(int col, int row, double value) {
        boundCheck(col, row);
        data[getIndex(col, row, columns)] = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DoubleMatrix matrix = (DoubleMatrix) o;
        return rows == matrix.rows &&
            columns == matrix.columns &&
            Arrays.equals(data, matrix.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, rows, columns);
    }

    @Override
    public String toString() {
        return "DoubleMatrix{" +
            "data=" + Arrays.toString(data) +
            ", rows=" + rows +
            ", columns=" + columns +
            '}';
    }
}

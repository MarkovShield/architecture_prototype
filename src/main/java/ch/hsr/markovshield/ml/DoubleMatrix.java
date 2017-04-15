package ch.hsr.markovshield.ml;

public class DoubleMatrix {

    private static final int DEFAULT_COLS = 10;
    private static final int DEFAULT_ROWS = 10;
    private double[] data;
    private int rows;
    private int cols;

    public DoubleMatrix(int initialCols, int initialRows) {
        this.rows = initialRows;
        this.cols = initialCols;
        data = new double[initialCols * initialRows];
    }

    public DoubleMatrix() {
        this(DEFAULT_COLS, DEFAULT_ROWS);
    }

    private static int getIndex(int col, int row, int width) {
        return row * width + col;
    }

    public double get(int col, int row) {
        return data[getIndex(col, row, cols)];
    }

    public void set(int col, int row, double value) {
        data[getIndex(col, row, cols)] = value;
    }

    public void resize(int cols, int rows) {
        double [] newData = new double[cols * rows];
        int colsToCopy = Math.min(cols, this.cols);
        int rowsToCopy = Math.min(rows, this.rows);
        for (int i = 0; i < rowsToCopy; ++i) {
            int oldRowStart = getIndex(0, i, this.cols);
            int newRowStart = getIndex(0, i, cols);
            System.arraycopy(data, oldRowStart, newData, newRowStart,
                colsToCopy
            );
        }
        data = newData;
    }

    double[] getData() {
        return data;
    }

    int getRows() {
        return rows;
    }

    int getCols() {
        return cols;
    }
}

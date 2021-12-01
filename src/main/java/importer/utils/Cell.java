package importer.utils;

public class Cell {
    public final int row;
    public final int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;

        if (other instanceof Cell that) {
            return row == that.row && col == that.col;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * Integer.valueOf(row).hashCode() + Integer.valueOf(col).hashCode();
    }
}

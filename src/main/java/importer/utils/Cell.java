package importer.utils;

public record Cell(int row, int col) {

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

package net.notfab.lindsey.core.framework;

import java.util.*;

public class AsciiTable {

    private final List<List<String>> rows = new ArrayList<>();
    private int columns = 0;
    private final List<Integer> columnLength = new ArrayList<>();

    public void addRow(String... columns) {
        this.rows.add(Arrays.asList(columns));
        if (columns.length > this.columns) {
            this.columns = columns.length;
        }
        for (int i = 0; i < columns.length; i++) {
            boolean isOutOfBounds = this.columnLength.size() <= i;
            int length = columns[i].length();
            if (isOutOfBounds) {
                this.columnLength.add(length);
            } else if (length > columnLength.get(i)) {
                this.columnLength.set(i, length);
            }
        }
    }

    public Queue<String> create() {
        Queue<String> table = new ArrayDeque<>();
        for (List<String> row : this.rows) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < columns; i++) {
                int length = this.columnLength.get(i);
                String column;
                if (row.size() <= i) {
                    column = " ".repeat(length);
                } else {
                    column = this.rightPad(row.get(i), length);
                }
                if (i > 0) {
                    builder.append("| ");
                }
                builder.append(column);
                if (i < (columns - 1)) {
                    builder.append(" ");
                }
            }
            table.add(builder.toString());
        }
        return table;
    }

    private String rightPad(String string, int length) {
        return string + " ".repeat(length - string.length());
    }

    public enum AsciiFormat {
        FREE, DESKTOP, MOBILE
    }

}

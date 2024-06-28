//
package com.veriktig.tools.jdeps;

import java.io.PrintWriter;

public class FixedWidthWriter {
    private PrintWriter writer;
    private int LINEWIDTH;

    FixedWidthWriter(PrintWriter writer, int LINEWIDTH) {
        this.writer = writer;
        this.LINEWIDTH = LINEWIDTH;
    }

    void format(StringBuilder sb) {
        int lines = ((sb.length() % LINEWIDTH) != 0) ? sb.length() / LINEWIDTH + 1 : sb.length();
        for (int ii = 0; ii < lines; ii++) {
            int line_width = (ii == 0) ? LINEWIDTH : LINEWIDTH - 1;
            int start = ii * line_width;
            int end = (ii*line_width + line_width > sb.length()) ? sb.length() : ii*line_width + line_width;
            if (ii > 0)
                writer.format(" %s%n", sb.substring(start, end));
            else
                writer.format("%s%n", sb.substring(start, end));
        }
    }
}

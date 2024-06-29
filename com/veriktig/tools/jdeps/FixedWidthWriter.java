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
        int length = sb.length();
        int line_number = 0;
        int start = 0;
        while (length > 0) {
            int base_line_width = (line_number == 0) ? LINEWIDTH : LINEWIDTH - 1;
            int line_width = (length > base_line_width) ? base_line_width : length;
            int end = start + line_width;
            if (line_number == 0)
                writer.format("%s%n", sb.substring(start, end));
            else
                writer.format(" %s%n", sb.substring(start, end));
            start += line_width;
            length -= line_width;
            line_number++;
        }
    }
}

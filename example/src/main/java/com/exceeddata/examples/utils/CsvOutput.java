/* 
 * Copyright (C) 2016-2024 Smart Software for Car Technologies Inc. and EXCEEDDATA
 *     https://www.smartsct.com
 *     https://www.exceeddata.com
 *
 *                            MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Except as contained in this notice, the name of a copyright holder
 * shall not be used in advertising or otherwise to promote the sale, use 
 * or other dealings in this Software without prior written authorization 
 * of the copyright holder.
 */

package com.exceeddata.examples.utils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.exceeddata.sdk.vdata.data.VDataRow;

/**
 * Utility class for generating CSV format.
 *
 */
public final class CsvOutput {
    private static final Pattern specials = Pattern.compile("[\\,\\\"\\r\\n]+");
    
    private CsvOutput() {}
    
    /**
     * Generate CSV header. The list of columns is assumed to have include the time column.
     * 
     * @param cols the list of header column names
     * @param sb the string buffer
     * @return generated header string
     */
    public static String headerToString(final List<String> cols, final StringBuilder sb) {
        sb.setLength(0);
        for (int i = 0, s = cols.size(); i < s; ++i) {
            if (i != 0) {
                sb.append(',');
            }
            sb.append(cols.get(i));
        }
        sb.append('\n');
        return sb.toString();
    }
    
    /**
     * Generate a CSV row from VDataRow.
     * 
     * @param row the row of data
     * @param sb the string buffer
     * @param fmt the decimal format
     * @return generated row string
     */
    public static String rowToString(final VDataRow row, final StringBuilder sb, final DecimalFormat fmt) {
        sb.setLength(0);
        sb.append(toMicrosTimeString(row.getTime()));
        return valuesToString(row.getValues(), 0, sb, fmt);
    }
    
    /**
     * Generate a CSV row from objects.
     * 
     * @param objs the row of objects
     * @param sb the string buffer
     * @param fmt the decimal format
     * @return generated row string
     */
    public static String objectsToString(final Object[] objs, final StringBuilder sb, final DecimalFormat fmt) {
        sb.setLength(0);
        sb.append(toMicrosTimeString((Instant) objs[0]));
        return valuesToString(objs, 1, sb, fmt);
    }
    
    private static String valuesToString(final Object[] objs, final int startPos, final StringBuilder sb, final DecimalFormat fmt) {
        for (int r = startPos, t = objs.length; r < t; ++r) {
            sb.append(',');
            final Object o = objs[r];
            if (o != null) {
                if (o instanceof Number) {
                    sb.append(fmt.format(o));
                } else if (o instanceof Map) {
                    final Map<?, ?> vals = (Map<?, ?>) o;
                    sb.append("\"{");
                    for (final Map.Entry<?, ?> entry : vals.entrySet()) {
                        if (entry.getValue() != null) {
                            sb.append("\"\"").append(entry.getKey().toString()).append("\"\":\"\"");
                            sb.append(entry.getValue() instanceof String
                                    ? entry.getValue().toString() : fmt.format(entry.getValue()));
                            sb.append("\"\",");
                        }
                    }
                    if (vals.size() > 0) {
                        sb.setLength(sb.length() - 1);
                    }
                    sb.append("}\"");
                } else if (o instanceof Number[]) {
                    final Number[] vals = (Number[]) o;
                    sb.append("\"[");
                    for (int i = 0, s = vals.length; i < s; ++i) {
                        if (vals[i] != null) {
                            sb.append(fmt.format(vals[i]));
                        }
                        sb.append(',');
                    }
                    if (vals.length > 0) {
                        sb.setLength(sb.length() - 1);
                    }
                    sb.append("]\"");
                } else if (o instanceof String[]) {
                    final String[] vals = (String[]) o;
                    final StringBuilder nb = new StringBuilder();
                    sb.append("\"[");
                    for (int i = 0, s = vals.length; i < s; ++i) {
                        if (vals[i] != null) {
                            nb.setLength(0);
                            buildString(nb, vals[i]);
                            buildString(sb, nb.toString());
                        }
                        sb.append(',');
                    }
                    if (vals.length > 0) {
                        sb.setLength(sb.length() - 1);
                    }
                    sb.append("]\"");
                } else {
                    buildString(sb, o.toString());
                }
            }
        }
        sb.append('\n');
        return sb.toString();
    }
    
    private static void buildString (final StringBuilder sb, final String s) {
        final int len = s != null ? s.length() : 0;
        
        if (len > 0) {
            if (!specials.matcher(s).find()) {
                sb.append(s);
                return;
            }
            
            final StringBuilder nb = new StringBuilder();
            boolean needsQuotes = false;
            char c;
            for (int i = 0; i < len; ++i) {
                c = s.charAt(i);
                if (c == ',' || c == '\n' || c == '\r') {
                    nb.append(c);
                    needsQuotes = true;
                } else if (c == '"') {
                    nb.append(c).append(c);
                    needsQuotes = true;
                } else if (c == '\\') {
                    nb.append(c).append(c);
                    needsQuotes = true;
                } else {
                    nb.append(c);
                }
            }
            if (needsQuotes) {
                sb.append('"').append(nb).append('"');
            } else {
                sb.append(nb);
            }
        }
    }
    
    private static String toMicrosTimeString(final Instant time) {
        final long millis = time.toEpochMilli();
        final int nanos = time.getNano();
        int submillis = nanos / 1000;
        submillis = submillis - (submillis / 1000) * 1000;
        
        final String str = String.valueOf(millis);
        if (submillis == 0) {
            return str;
        } else if (submillis < 10) {
            return str + ".00" + submillis;
        } else if (submillis < 100) {
            return str + ".0" + submillis;
        } else {
            return str + "." + submillis;
        }
    }
}

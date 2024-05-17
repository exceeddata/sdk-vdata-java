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

package com.exceeddata.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.exceeddata.examples.utils.ApplicationUtils;
import com.exceeddata.examples.utils.CsvOutput;
import com.exceeddata.sdk.vdata.app.LogUtils;
import com.exceeddata.sdk.vdata.binary.BinarySeekableReader;
import com.exceeddata.sdk.vdata.data.VDataFrame;
import com.exceeddata.sdk.vdata.data.VDataReader;
import com.exceeddata.sdk.vdata.data.VDataReaderFactory;
import com.exceeddata.sdk.vdata.data.VDataRow;
import com.exceeddata.sdk.vdata.util.VDataUtils;

/**
 * 
 * Project: An full application on using the SDK to decode vData data.
 * Author:  Nick Xie
 * Usage:   java -cp vdata-sdk-java-examples.jar com.exceeddata.examples.VswDecode -i input_path -o output_path [...optional parameters]
 *
 */
public class VswDecode {
    
    private static void printUsage() {
        System.out.println("java -cp vdata.jar com.exceeddata.sdk.vdata.app.Trappist inputPath outputPath [signalNames base64Encoded densifyNumRows]");
        System.out.println("     [-i|input <paths>]. Required. The input vsw file path(s). Multiple files are comma separated.");
        System.out.println("     [-o|output <path>]. Required. The output file path.");
        System.out.println("     [-s|signals <names>]. Optional. Comma-separated list of signal names to extract.");
        System.out.println("     [-b|base64 <true|false>]. Optional. Whether the input file is base64 encoded. Default is false.");
        System.out.println("     [-d|densify rows <#>]. Optional. The number of rows to look ahead to fill in for initial null rows.  Default is 0 (no fill in)");
        System.out.println("     [-e|densify interval <ms>]. Optional. Give a new output interval (vary frequency) for dense data. Default is 0 (no interval).");
        System.out.println("     [-m|qmode <last|first|all>]. Optional. The retrieve mode when there are multiple values for a signal at the same time. Default is 'last' (use last value)");
        System.out.println("     [-p|expand <none|flat|full>]. Optional. 'none' is output as columns as stored. 'flat' will extract structs into individual columns. 'full' is extract with qualified name. Default is 'full')");
        System.out.println("     [-x|query method<iterator|objects|object1s>]. Optional. The query method.  Default is objects.)");
        System.out.println("     [-h|help]. optional)");
        System.out.println("");
    }
    
    /**
     * The main entry method.
     * 
     * @param args the arguments
     * @throws Exception if exception occurs
     */
    public static void main(String[] args) throws Exception {
        
        LogUtils.setLogLevel();
        
        final Map<String, String> configs = new HashMap<>();
        if (ApplicationUtils.parseArguments(args, configs) < 0) {
            printUsage();
            return;
        }
        
        final String inputPath = ApplicationUtils.get(configs, "-i", "");
        final String outputPath = ApplicationUtils.get(configs, "-o", "");
        final String signalNames = ApplicationUtils.get(configs, "-s", null);
        final String signalQueueMode = ApplicationUtils.getEnum(configs, "-m", new String[] { "last", "first", "all"}, null);
        final String columnExpandMode = ApplicationUtils.getEnum(configs, "-p", new String[] { "none", "flat", "full"}, null);
        final String queryMethod = ApplicationUtils.getEnum(configs, "-x", new String[] { "iterator", "objects", "object1s"}, "objects");
        final boolean base64Encoded = ApplicationUtils.parseBoolean(ApplicationUtils.get(configs, "-b", null), false);
        final int densifyRowsAhead = ApplicationUtils.parseInt(ApplicationUtils.get(configs, "-d", null), 0);
        final int densifyOutputItv = ApplicationUtils.parseInt(ApplicationUtils.get(configs, "-e", null), 0);
        
        if (inputPath.length() == 0) {
            System.out.println("Error: input path parameter empty");
            return;
        }
        if (outputPath.length() == 0) {
            if (!"none".equals(queryMethod)) {
                System.out.println("Error: output path parameter empty");
                return;
            }
        }

        final List<String> inputFilePaths = new ArrayList<>();
        final String[] ps = inputPath.split(",");
        for (final String p : ps) {
            final String np = p.trim();
            if (np.isEmpty()) {
                continue;
            }

            final File fp = new File(np);
            if (!fp.exists()) {
                System.err.println("Error: input path not exist: " + fp);
                continue;
            }
            
            if (fp.isDirectory()) {
                for (File f : fp.listFiles()) {
                    if (ApplicationUtils.validateFile(f, base64Encoded)) {
                        inputFilePaths.add(f.getAbsolutePath().toString());
                    }
                }
            } else if (ApplicationUtils.validateFile(fp, base64Encoded)) {
                inputFilePaths.add(fp.getAbsolutePath().toString());
            }
        }
        
        if (inputFilePaths.size() == 0) {
            System.err.println("Error: no input files not found: " + inputPath);
            return;
        }
        
        long start = System.currentTimeMillis();
        
        decode(queryMethod, inputFilePaths, outputPath, signalNames, base64Encoded, densifyRowsAhead, densifyOutputItv, signalQueueMode, columnExpandMode);
        
        long end = System.currentTimeMillis();
        System.out.println("took " + (end - start) + " ms");
    }
    
    private static void decode(
            final String queryMethod,
            final List<String> paths,
            final String outputPath,
            final String signalNames,
            final boolean base64Encoded,
            final int densifyRowsAhead,
            final int densifyOutputItv,
            final String signalQueueMode,
            final String columnExpandMode) throws IOException {
        final StringBuilder sb = new StringBuilder(4096);
        final DecimalFormat fmt = VDataUtils.getDecimalFormat();
        fmt.setMaximumFractionDigits(10);
         
        ArrayList<String> targetSignals = new ArrayList<>();
        if (signalNames != null && signalNames.trim().length() > 0) {
            for (final String name : signalNames.split(",")) {
                if (name.trim().length() > 0) {
                    targetSignals.add(name.trim());
                }
            }
        }
        
        List<BinarySeekableReader> seekables = null;
        OutputStream output = null;
        VDataReader reader = null;
        VDataFrame df = null;
        List<String> cols = null;
        int rowtotal = 0;
        
        try {
            seekables = ApplicationUtils.getSeekables(paths, base64Encoded);
            reader = new VDataReaderFactory()
                            .setDataReaders(seekables)
                            .setSignals(targetSignals)
                            .setColumnExpandMode(columnExpandMode)
                            .setSignalQueueMode(signalQueueMode)
                            .open();

            df = reader.df();
            cols = df.cols(true);
            
            output = new FileOutputStream(outputPath);
            output.write(CsvOutput.headerToString(cols, sb).getBytes(StandardCharsets.UTF_8)); //header
            
            if ("iterator".equalsIgnoreCase(queryMethod)) {
                final Iterator<VDataRow> iter = df.iterator(densifyRowsAhead, densifyOutputItv, 0);
                while (iter.hasNext()) {
                    output.write(CsvOutput.rowToString(iter.next(), sb, fmt).getBytes(StandardCharsets.UTF_8));
                    rowtotal++;
                }
            } else {
                final Object[][] objs = "object1s".equalsIgnoreCase(queryMethod)
                        ? df.object1s()
                        : df.objects(densifyRowsAhead, densifyOutputItv);
                rowtotal = objs.length;
                
                for (final Object[] row : objs) {
                    output.write(CsvOutput.objectsToString(row, sb, fmt).getBytes(StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch(IOException e) {}
            }
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (seekables != null) {
                for (final BinarySeekableReader seekable : seekables) {
                    seekable.close();
                }
            }
        }
        System.out.println("Rows: " + rowtotal);
        System.out.println("------------------------------------------------");
    }
}

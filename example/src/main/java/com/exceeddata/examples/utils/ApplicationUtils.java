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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.exceeddata.sdk.vdata.binary.BinarySeekableReader;
import com.exceeddata.sdk.vdata.binary.LittleEndianSeekableBytesReader;

/**
 * Application utility class.
 *
 */
public final class ApplicationUtils {
    private ApplicationUtils() {}

    /**
     * Parse a boolean value from string.
     * 
     * @param str the string
     * @param defaultValue the default value
     * @return boolean
     */
    public static boolean parseBoolean(final String str, final boolean defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        
        final String s = str.trim();
        if (s.length() == 0) {
            return defaultValue;
        }
        
        return ("0".equals(s) || "false".equalsIgnoreCase(s)) ? false : true;
    }
    
    /**
     * Parse an int value from string.
     * 
     * @param str the string
     * @param defaultValue the default value
     * @return int
     */
    public static int parseInt(final String str, final int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        
        final String s = str.trim();
        if (s.length() == 0) {
            return defaultValue;
        }
        
        try {
            return (int) Math.round(Double.parseDouble(s));
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parse a long value from string.
     * 
     * @param str the string
     * @param defaultValue the default value
     * @return long
     */
    public static long parseLong(final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        
        final String s = str.trim();
        if (s.length() == 0) {
            return defaultValue;
        }
        
        try {
            return Math.round(Double.parseDouble(s));
        } catch(NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * Parse an array of command line arguments to a configuration map.
     * 
     * @param args command line arguments
     * @param configs the configuration map
     * @return zero if successful, otherwise unsuccessful
     * @throws IOException if occurs
     */
    public static int parseArguments (final String[] args,  final Map<String, String> configs) throws IOException {
        if (args == null || args.length<1) {
            throw new IOException ("Error: parameters are missing");
        }

        for (int i = 0; i < args.length; ++i) {
            String key = args[i].trim();
            if (key.length() == 0) {
                return 0;
            } else if (key.charAt(0)!='-' || key.length()<2) {
                throw new IOException ("Error: parameters are invalid");
            } else if (key.equalsIgnoreCase("-h")) {
                return -1;
            }
            
            if (++i < args.length) {
                String val = args[i].trim();
                if (key.length() == 0) {
                    throw new IOException ("Error: parameters are invalid: " + key); 
                }
                configs.put(key, val);
            } else {
                throw new IOException ("Error: parameters are invalid: " + key);
            }
        }
        
        return 0;
    }

    /**
     * Get a string value from the parameter map.
     * 
     * @param params the map
     * @param key the key to lookup
     * @param defaultValue the default value if not found
     * @return string
     */
    public static String get(final Map<String, String> params, final String key, final String defaultValue) {
        if (params.containsKey(key)) {
            final String v = params.remove(key).trim();
            if (v.length() > 0) {
                return v;
            }
        }
        return defaultValue;
    }

    /**
     * Get a string value from the parameter map.
     * 
     * @param params the map
     * @param key the key to lookup
     * @param enums the available enum list
     * @param defaultValue the default value if not found or not in enum list
     * @return string
     */
    public static String getEnum(final Map<String, String> params, final String key, final String[] enums, final String defaultValue) {
        if (params.containsKey(key)) {
            final String v = params.remove(key).trim().toLowerCase();
            if (v.length() > 0) {
                for (final String e : enums) {
                    if (v.equals(e)) {
                        return v;
                    }
                }
            }
        }
        return defaultValue;
    }
    
    public static boolean validateFile(final File file, final boolean base64Encoded) {
        if (file.isHidden() || file.isDirectory()) {
            return false;
        }
        final String name = file.getName().toLowerCase();
        if (name.startsWith(".") || name.startsWith("_")) {
            return false;
        }
        if (name.endsWith(".vsw") || name.endsWith(".stf") || name.endsWith(".mmf")) {
            return true;
        } else if (base64Encoded && (name.endsWith(".kfk") || name.endsWith(".txt") || name.endsWith(".json"))) {
            return true;
        }
        return false;
    }
    
    public static List<BinarySeekableReader> getSeekables(final List<String> paths, final boolean base64Encoded) throws IOException {
        final List<BinarySeekableReader> seekables = new ArrayList<>();
        for (final String p : paths) {
            final String pn = p.trim();
            if (pn.isEmpty()) {
                continue;
            }
            byte[] data = Files.readAllBytes(Paths.get(pn));
            if (base64Encoded) {
                data = Base64.getMimeDecoder().decode(data);
            }
            seekables.add(new LittleEndianSeekableBytesReader(data));
        }
        return seekables;
    }
}

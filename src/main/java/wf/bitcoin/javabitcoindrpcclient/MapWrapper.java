/*
 * Bitcoin-JSON-RPC-Client License
 * 
 * Copyright (c) 2013, Mikhail Yevchenko.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 
 * Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package wf.bitcoin.javabitcoindrpcclient;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com
 */
class MapWrapper {

    public final Map m;

    public MapWrapper(Map m) {
        this.m = m;
    }

    public boolean mapBool(String key) {
        return mapBool(m, key);
    }

    public float mapFloat(String key) {
        return mapFloat(m, key);
    }

    public double mapDouble(String key) {
        return mapDouble(m, key);
    }

    public int mapInt(String key) {
        return mapInt(m, key);
    }

    public long mapLong(String key) {
        return mapLong(m, key);
    }

    public String mapStr(String key) {
        return mapStr(m, key);
    }

    public Date mapCTime(String key) {
        return mapCTime(m, key);
    }

    public BigDecimal mapBigDecimal(String key) {
      return mapBigDecimal(m, key);
    }

    public static boolean mapBool(Map m, String key) {
        return ((Boolean)m.get(key));
    }

    public static BigDecimal mapBigDecimal(Map m, String key) {
      return new BigDecimal((String) m.get(key));
    }

    public static float mapFloat(Map m, String key) {
        return ((Number)m.get(key)).floatValue();
    }

    public static double mapDouble(Map m, String key) {
        return ((Number)m.get(key)).doubleValue();
    }

    public static int mapInt(Map m, String key) {
        return ((Number)m.get(key)).intValue();
    }

    public static long mapLong(Map m, String key) {
        return ((Number)m.get(key)).longValue();
    }

    public static String mapStr(Map m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }

    public static Date mapCTime(Map m, String key) {
        Object v = m.get(key);
        return v == null ? null : new Date(mapLong(m, key) * 1000);
    }

    @Override
    public String toString() {
        return String.valueOf(m);
    }

}

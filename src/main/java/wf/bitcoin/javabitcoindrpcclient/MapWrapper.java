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

import wf.bitcoin.krotjson.HexCoder;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com
 */
class MapWrapper implements MapWrapperType {

  public final Map<String, ?> m;

  MapWrapper(Map<String, ?> m) {
    this.m = m;
  }

  @Override
  public Boolean mapBool(String key) {
    return mapBool(m, key);
  }

  @Override
  public Integer mapInt(String key) {
    return mapInt(m, key);
  }

  @Override
  public Long mapLong(String key) {
    return mapLong(m, key);
  }

  @Override
  public String mapStr(String key) {
    return mapStr(m, key);
  }

  @Override
  public Date mapDate(String key) {
    return mapDate(m, key);
  }

  @Override
  public BigDecimal mapBigDecimal(String key) {
    return mapBigDecimal(m, key);
  }

  @Override
  public byte[] mapHex(String key) {
    return mapHex(m, key);
  }

  private static Boolean mapBool(Map<String, ?> m, String key) {
    Object val = m.get(key);
    if (!(val instanceof Boolean)) return null;
    return (Boolean) val;
  }

  private static BigDecimal mapBigDecimal(Map<String, ?> m, String key) {
    Object val = m.get(key);
    if (val instanceof BigDecimal) return (BigDecimal) val;
    String strVal = mapStr(m, key);
    if (strVal == null) return null;
    return new BigDecimal(strVal);
  }

  private static Integer mapInt(Map<String, ?> m, String key) {
    Object val = m.get(key);
    if (!(val instanceof Number)) return null;
    return ((Number) val).intValue();
  }

  private static Long mapLong(Map<String, ?> m, String key) {
    Object val = m.get(key);
    if (!(val instanceof Number)) return null;
    return ((Number) val).longValue();
  }

  private static String mapStr(Map<String, ?> m, String key) {
    Object val = m.get(key);
    if (val == null) return null;
    return val.toString();
  }

  private static Date mapDate(Map<String, ?> m, String key) {
    Long longVal = mapLong(m, key);
    if (longVal == null) return null;
    return new Date(longVal * 1000);
  }

  private static byte[] mapHex(Map<String, ?> m, String key) {
    String strVal = mapStr(m, key);
    if (strVal == null) return null;
    return HexCoder.decode(strVal);
  }

  @Override
  public String toString() {
    return String.valueOf(m);
  }

}
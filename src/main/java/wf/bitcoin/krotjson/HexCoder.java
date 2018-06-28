// Copyright 2003-2010 Christian d'Heureuse, Inventec Informatik AG, Zurich, Switzerland
// www.source-code.biz, www.inventec.ch/chdh
//
// This module is multi-licensed and may be used under the terms
// of any of the following licenses:
//
//  EPL, Eclipse Public License, V1.0 or later, http://www.eclipse.org/legal
//  LGPL, GNU Lesser General Public License, V2.1 or later, http://www.gnu.org/licenses/lgpl.html
//  GPL, GNU General Public License, V2 or later, http://www.gnu.org/licenses/gpl.html
//  AL, Apache License, V2.0 or later, http://www.apache.org/licenses
//  BSD, BSD License, http://www.opensource.org/licenses/bsd-license.php
//  MIT, MIT License, http://www.opensource.org/licenses/MIT
//
// Please contact the author if you need another license.
// This module is provided "as is", without warranties of any kind.

package wf.bitcoin.krotjson;

/**
* A Hex encoder/decoder.
*/
public final class HexCoder {

  private final static char[] hexArray = "0123456789abcdef".toCharArray();

  // Hide ctor
  private HexCoder() {
  }

  public static String encode(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int i = 0; i < bytes.length; i++) {
      int v = bytes[i] & 0xFF;
      hexChars[i * 2] = hexArray[v >>> 4];
      hexChars[i * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static byte[] decode(String encoded) {
    char[] hexChars = encoded.toLowerCase().toCharArray();
    byte[] bytes = new byte[hexChars.length / 2];
    for (int i = 0; i < bytes.length; i++) {
      char v1 = hexChars[2 * i];
      if ('0' <= v1 && v1 <= '9') {
        bytes[i] = (byte) (16 * (v1 - '0'));
      } else {
        bytes[i] = (byte) (16 * (10 + v1 - 'a'));
      }
      char v2 = hexChars[2 * i + 1];
      if ('0' <= v2 && v2 <= '9') {
        bytes[i] += (byte) (v2 - '0');
      } else {
        bytes[i] += (byte) (10 + v2 - 'a');
      }
    }
    return bytes;
  }
}

package wf.bitcoin.javabitcoindrpcclient;

import org.junit.Assert;
import org.junit.Test;

import wf.bitcoin.krotjson.HexCoder;

public class HexCoderTest {

  @Test
  public void testHexCoder () {
    
    String str = "DOCPROOF";
    
    byte[] bytes = str.getBytes();
    String hex = HexCoder.encode(bytes);
    Assert.assertEquals("444f4350524f4f46", hex);
    
    byte[] res = HexCoder.decode(hex);
    Assert.assertArrayEquals(bytes, res);
    Assert.assertEquals(str, new String(res));
    
    res = HexCoder.decode("444F4350524F4F46");
    Assert.assertArrayEquals(bytes, res);
    Assert.assertEquals(str, new String(res));
  }
}
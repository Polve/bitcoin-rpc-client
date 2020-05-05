package wf.bitcoin.javabitcoindrpcclient;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.ScanObject;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.UnspentTxOutput;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.UtxoSet;
import wf.bitcoin.krotjson.JSON;

/**
 * Created by fpeters on 11-01-17.
 */

public class BitcoinJSONRPCClientTest {

    class MyClientTest extends BitcoinJSONRPCClient {

        String expectedMethod;
        Object[] expectedObject;
        String result;

        MyClientTest(boolean testNet, String expectedMethod, Object[] expectedObject, String result) {
            super(testNet);
            this.expectedMethod = expectedMethod;
            this.expectedObject = expectedObject;
            this.result = result;
        }

        @Override
        public Object query(String method, Object... o) throws GenericRpcException {
            if(method!=expectedMethod) {
                throw new GenericRpcException("wrong method");
            }
            if(o.equals(expectedObject)){
                throw new GenericRpcException("wrong object");
            }
            return JSON.parse(result);
        }
    }

    MyClientTest client;

    @Test
    public void signRawTransactionTest() throws Exception {
        client = new MyClientTest(false, "signrawtransaction", null,
                                    "{\n" +
                                            "  \"hex\": \"0100000001b8b2244faca910c1ffff24ecd2b559b4699338398bf77e4cb1fdeb19ad419ea0010000006b483045022100b68b7fe9cfabb32949af6747b6769dffcf2aa4170e4df2f0e9d0a4571989e94e02204cf506c210cdb6b6b4413bf251a0b57ebcf1b1b2d303ba6183239b557ef0a310012102ab46e1d7b997d8094e97bc06a21a054c2ef485fac512e2dc91eb9831af55af4effffffff012e2600000000000017a9140b2d7ed4e5076383ba8e98b9b3bce426b7a2ea1e8700000000\",\n" +
                                            "  \"complete\": true\n" +
                                            "}\n");
        LinkedList<BitcoindRpcClient.ExtendedTxInput> inputList = new LinkedList<BitcoindRpcClient.ExtendedTxInput>();
        LinkedList<String> privateKeys = new LinkedList<String>();
        privateKeys.add("cSjzx3VAM1r9iLXLvL6N61oS3zKns9Z9DcocrbkEzesPTDHWm5r4");
        String hex = client.signRawTransaction("0100000001B8B2244FACA910C1FFFF24ECD2B559B4699338398BF77E4CB1FDEB19AD419EA0010000001976A9144CB4C3B90994FEF58FABB6D8368302E917C6EFB188ACFFFFFFFF012E2600000000000017A9140B2D7ED4E5076383BA8E98B9B3BCE426B7A2EA1E8700000000",
                                                inputList, privateKeys, "ALL");
        assertEquals("0100000001b8b2244faca910c1ffff24ecd2b559b4699338398bf77e4cb1fdeb19ad419ea0010000006b483045022100b68b7fe9cfabb32949af6747b6769dffcf2aa4170e4df2f0e9d0a4571989e94e02204cf506c210cdb6b6b4413bf251a0b57ebcf1b1b2d303ba6183239b557ef0a310012102ab46e1d7b997d8094e97bc06a21a054c2ef485fac512e2dc91eb9831af55af4effffffff012e2600000000000017a9140b2d7ed4e5076383ba8e98b9b3bce426b7a2ea1e8700000000",
                    hex);
    }

    @Test
    public void signRawTransactionTestException() throws Exception {
        client = new MyClientTest(false, "signrawtransaction", null,
                "{\n" +
                        "  \"hex\": \"0100000001b8b2244faca910c1ffff24ecd2b559b4699338398bf77e4cb1fdeb19ad419ea00100000000ffffffff012e2600000000000017a9140b2d7ed4e5076383ba8e98b9b3bce426b7a2ea1e8700000000\",\n" +
                        "  \"complete\": false,\n" +
                        "  \"errors\": [\n" +
                        "    {\n" +
                        "      \"txid\": \"a09e41ad19ebfdb14c7ef78b39389369b459b5d2ec24ffffc110a9ac4f24b2b8\",\n" +
                        "      \"vout\": 1,\n" +
                        "      \"scriptSig\": \"\",\n" +
                        "      \"sequence\": 4294967295,\n" +
                        "      \"error\": \"Operation not valid with the current stack size\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}");
        LinkedList<BitcoindRpcClient.ExtendedTxInput> inputList = new LinkedList<BitcoindRpcClient.ExtendedTxInput>();
        LinkedList<String> privateKeys = new LinkedList<String>();
        try {
            client.signRawTransaction("0100000001B8B2244FACA910C1FFFF24ECD2B559B4699338398BF77E4CB1FDEB19AD419EA0010000001976A9144CB4C3B90994FEF58FABB6D8368302E917C6EFB188ACFFFFFFFF012E2600000000000017A9140B2D7ED4E5076383BA8E98B9B3BCE426B7A2EA1E8700000000",
                    inputList, privateKeys, "ALL");
        }
        catch(Exception e) {
            assertThat(e.getMessage(), is("Incomplete"));
        }
    }

    @Test
    public void signRawTransactionTest2() throws Exception {
        client = new MyClientTest(false, "signrawtransaction", null,
                "{\n" +
                        "  \"hex\": \"0100000001b8b2244faca910c1ffff24ecd2b559b4699338398bf77e4cb1fdeb19ad419ea0010000006b483045022100b68b7fe9cfabb32949af6747b6769dffcf2aa4170e4df2f0e9d0a4571989e94e02204cf506c210cdb6b6b4413bf251a0b57ebcf1b1b2d303ba6183239b557ef0a310012102ab46e1d7b997d8094e97bc06a21a054c2ef485fac512e2dc91eb9831af55af4effffffff012e2600000000000017a9140b2d7ed4e5076383ba8e98b9b3bce426b7a2ea1e8700000000\",\n" +
                        "  \"complete\": true\n" +
                        "}\n");
        String hex = client.signRawTransaction("0100000001B8B2244FACA910C1FFFF24ECD2B559B4699338398BF77E4CB1FDEB19AD419EA0010000001976A9144CB4C3B90994FEF58FABB6D8368302E917C6EFB188ACFFFFFFFF012E2600000000000017A9140B2D7ED4E5076383BA8E98B9B3BCE426B7A2EA1E8700000000");
        assertEquals("0100000001b8b2244faca910c1ffff24ecd2b559b4699338398bf77e4cb1fdeb19ad419ea0010000006b483045022100b68b7fe9cfabb32949af6747b6769dffcf2aa4170e4df2f0e9d0a4571989e94e02204cf506c210cdb6b6b4413bf251a0b57ebcf1b1b2d303ba6183239b557ef0a310012102ab46e1d7b997d8094e97bc06a21a054c2ef485fac512e2dc91eb9831af55af4effffffff012e2600000000000017a9140b2d7ed4e5076383ba8e98b9b3bce426b7a2ea1e8700000000",
                    hex);
    }

    @Test
    public void scanTxOutSetTest() {
      ScanObject scanObject1 = new ScanObject("addr(mtoffFXQWh6YNP86TRsRETNn9nDaMmsKsL)", null);
      ScanObject scanObject2 = new ScanObject("addr(mi11rWuB14Eb2L5tpdqfD77DGMhschQdgx)", null);
      List<ScanObject> list = Arrays.asList(scanObject1, scanObject2);

      String json = "{\n" + 
          "  \"success\": true,\n" + 
          "  \"searched_items\": 22462153,\n" + 
          "  \"unspents\": [\n" + 
          "    {\n" + 
          "      \"txid\": \"6415d590f46344a6f72c0e1544eb183a5ac3d8ff9a2ab48435f3255794af3915\",\n" + 
          "      \"vout\": 0,\n" + 
          "      \"scriptPubKey\": \"76a9141b3edeb7188b1cef9996e81ae22b68dfb3f7806688ac\",\n" + 
          "      \"amount\": 0.00900000,\n" + 
          "      \"height\": 1442023\n" + 
          "    },\n" + 
          "    {\n" + 
          "      \"txid\": \"2d3bb59ba7bf690b43f604d7289e76534a9a32e92dd4f1945413a59832fe0723\",\n" + 
          "      \"vout\": 0,\n" + 
          "      \"scriptPubKey\": \"76a91491c2d21b865e338794bc92326de5dd0c15663d8788ac\",\n" + 
          "      \"amount\": 0.00300000,\n" + 
          "      \"height\": 1441179\n" + 
          "    },\n" + 
          "    {\n" + 
          "      \"txid\": \"b6573ad024dd97172238712a8d417e39ff9fbeb15e35bbae447b86966503289b\",\n" + 
          "      \"vout\": 1,\n" + 
          "      \"scriptPubKey\": \"76a91491c2d21b865e338794bc92326de5dd0c15663d8788ac\",\n" + 
          "      \"amount\": 0.00200000,\n" + 
          "      \"height\": 1440923\n" + 
          "    }\n" + 
          "  ],\n" + 
          "  \"total_amount\": 0.01400000\n" + 
          "}\n" + 
          "";

      client = new MyClientTest(false, "scantxoutset", new Object[] { "start", list }, json);
      UtxoSet utxoSet = client.scanTxOutSet(list);
      assertEquals(22462153, utxoSet.searchedItems().intValue());
      assertEquals(new BigDecimal("0.01400000"), utxoSet.totalAmount());
      assertEquals(3, utxoSet.unspents().size());
      UnspentTxOutput utxo = utxoSet.unspents().get(0);
      assertEquals("6415d590f46344a6f72c0e1544eb183a5ac3d8ff9a2ab48435f3255794af3915", utxo.txid());
      assertEquals(0, utxo.vout().intValue());
    }
}
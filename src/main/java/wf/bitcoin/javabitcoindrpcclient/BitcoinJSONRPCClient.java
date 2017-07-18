/*
 * BitcoindRpcClient-JSON-RPC-Client License
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
 /*
 * Repackaged with simple additions for easier maven usage by Alessandro Polverini
 */
package wf.bitcoin.javabitcoindrpcclient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import wf.bitcoin.krotjson.Base64Coder;
import wf.bitcoin.krotjson.JSON;
import static wf.bitcoin.javabitcoindrpcclient.MapWrapper.*;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ at azazar.com Small modifications by
 * Alessandro Polverini polverini at gmail.com
 */
public class BitcoinJSONRPCClient implements BitcoindRpcClient {

  private static final Logger logger = Logger.getLogger(BitcoinJSONRPCClient.class.getCanonicalName());

  public final URL rpcURL;

  private URL noAuthURL;
  private String authStr;

  public BitcoinJSONRPCClient(String rpcUrl) throws MalformedURLException {
    this(new URL(rpcUrl));
  }

  public BitcoinJSONRPCClient(URL rpc) {
    this.rpcURL = rpc;
    try {
      noAuthURL = new URI(rpc.getProtocol(), null, rpc.getHost(), rpc.getPort(), rpc.getPath(), rpc.getQuery(), null).toURL();
    } catch (MalformedURLException | URISyntaxException ex) {
      throw new IllegalArgumentException(rpc.toString(), ex);
    }
    authStr = rpc.getUserInfo() == null ? null : String.valueOf(Base64Coder.encode(rpc.getUserInfo().getBytes(Charset.forName("ISO8859-1"))));
  }

  public static final URL DEFAULT_JSONRPC_URL;
  public static final URL DEFAULT_JSONRPC_TESTNET_URL;

  static {
    String user = "user";
    String password = "pass";
    String host = "localhost";
    String port = null;

    try {
      File f;
      File home = new File(System.getProperty("user.home"));

      if ((f = new File(home, ".bitcoin" + File.separatorChar + "bitcoin.conf")).exists()) {
      } else if ((f = new File(home, "AppData" + File.separatorChar + "Roaming" + File.separatorChar + "Bitcoin" + File.separatorChar + "bitcoin.conf")).exists()) {
      } else {
        f = null;
      }

      if (f != null) {
        logger.fine("Bitcoin configuration file found");

        Properties p = new Properties();
        try (FileInputStream i = new FileInputStream(f)) {
          p.load(i);
        }

        user = p.getProperty("rpcuser", user);
        password = p.getProperty("rpcpassword", password);
        host = p.getProperty("rpcconnect", host);
        port = p.getProperty("rpcport", port);
      }
    } catch (Exception ex) {
      logger.log(Level.SEVERE, null, ex);
    }

    try {
      DEFAULT_JSONRPC_URL = new URL("http://" + user + ':' + password + "@" + host + ":" + (port == null ? "8332" : port) + "/");
      DEFAULT_JSONRPC_TESTNET_URL = new URL("http://" + user + ':' + password + "@" + host + ":" + (port == null ? "18332" : port) + "/");
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public BitcoinJSONRPCClient(boolean testNet) {
    this(testNet ? DEFAULT_JSONRPC_TESTNET_URL : DEFAULT_JSONRPC_URL);
  }

  public BitcoinJSONRPCClient() {
    this(DEFAULT_JSONRPC_TESTNET_URL);
  }

  private HostnameVerifier hostnameVerifier = null;
  private SSLSocketFactory sslSocketFactory = null;

  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
  }

  public SSLSocketFactory getSslSocketFactory() {
    return sslSocketFactory;
  }

  public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    this.sslSocketFactory = sslSocketFactory;
  }

  public static final Charset QUERY_CHARSET = Charset.forName("ISO8859-1");

  public byte[] prepareRequest(final String method, final Object... params) {
    return JSON.stringify(new LinkedHashMap() {
      {
        put("method", method);
        put("params", params);
        put("id", "1");
      }
    }).getBytes(QUERY_CHARSET);
  }

  private static byte[] loadStream(InputStream in, boolean close) throws IOException {
    ByteArrayOutputStream o = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    for (;;) {
      int nr = in.read(buffer);

      if (nr == -1)
        break;
      if (nr == 0)
        throw new IOException("Read timed out");

      o.write(buffer, 0, nr);
    }
    return o.toByteArray();
  }

  public Object loadResponse(InputStream in, Object expectedID, boolean close) throws IOException, BitcoinRpcException {
    try {
      String r = new String(loadStream(in, close), QUERY_CHARSET);
      logger.log(Level.FINE, "Bitcoin JSON-RPC response:\n{0}", r);
      try {
        Map response = (Map) JSON.parse(r);

        if (!expectedID.equals(response.get("id")))
          throw new BitcoinRPCException("Wrong response ID (expected: " + String.valueOf(expectedID) + ", response: " + response.get("id") + ")");

        if (response.get("error") != null)
          throw new BitcoinRpcException(JSON.stringify(response.get("error")));

        return response.get("result");
      } catch (ClassCastException ex) {
        throw new BitcoinRPCException("Invalid server response format (data: \"" + r + "\")");
      }
    } finally {
      if (close)
        in.close();
    }
  }

  public Object query(String method, Object... o) throws BitcoinRpcException {
    HttpURLConnection conn;
    try {
      conn = (HttpURLConnection) noAuthURL.openConnection();

      conn.setDoOutput(true);
      conn.setDoInput(true);

      if (conn instanceof HttpsURLConnection) {
        if (hostnameVerifier != null)
          ((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
        if (sslSocketFactory != null)
          ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
      }

//            conn.connect();
      ((HttpURLConnection) conn).setRequestProperty("Authorization", "Basic " + authStr);
      byte[] r = prepareRequest(method, o);
      logger.log(Level.FINE, "Bitcoin JSON-RPC request:\n{0}", new String(r, QUERY_CHARSET));
      conn.getOutputStream().write(r);
      conn.getOutputStream().close();
      int responseCode = conn.getResponseCode();
      if (responseCode != 200)
        throw new BitcoinRPCException(method, Arrays.deepToString(o), responseCode, conn.getResponseMessage(), new String(loadStream(conn.getErrorStream(), true)));
      return loadResponse(conn.getInputStream(), "1", true);
    } catch (IOException ex) {
      throw new BitcoinRPCException(method, Arrays.deepToString(o), ex);
    }
  }

  @Override
  public String createRawTransaction(List<TxInput> inputs, List<TxOutput> outputs) throws BitcoinRpcException {
    List<Map> pInputs = new ArrayList<>();

    for (final TxInput txInput : inputs) {
      pInputs.add(new LinkedHashMap() {
        {
          put("txid", txInput.txid());
          put("vout", txInput.vout());
        }
      });
    }

    Map<String, Double> pOutputs = new LinkedHashMap();

    Double oldValue;
    for (TxOutput txOutput : outputs) {
      if ((oldValue = pOutputs.put(txOutput.address(), txOutput.amount())) != null)
        pOutputs.put(txOutput.address(), BitcoinUtil.normalizeAmount(oldValue + txOutput.amount()));
//                throw new BitcoinRpcException("Duplicate output");
    }

    return (String) query("createrawtransaction", pInputs, pOutputs);
  }

  @Override
  public String dumpPrivKey(String address) throws BitcoinRpcException {
    return (String) query("dumpprivkey", address);
  }

  @Override
  public String getAccount(String address) throws BitcoinRpcException {
    return (String) query("getaccount", address);
  }

  @Override
  public String getAccountAddress(String address) throws BitcoinRpcException {
    return (String) query("getaccountaddress", address);
  }

  @Override
  public List<String> getAddressesByAccount(String account) throws BitcoinRpcException {
    return (List<String>) query("getaddressesbyaccount", account);
  }

  @Override
  public double getBalance() throws BitcoinRpcException {
    return ((Number) query("getbalance")).doubleValue();
  }

  @Override
  public double getBalance(String account) throws BitcoinRpcException {
    return ((Number) query("getbalance", account)).doubleValue();
  }

  @Override
  public double getBalance(String account, int minConf) throws BitcoinRpcException {
    return ((Number) query("getbalance", account, minConf)).doubleValue();
  }

  private class InfoWrapper extends MapWrapper implements Info, Serializable {

    public InfoWrapper(Map m) {
      super(m);
    }

    @Override
    public double balance() {
      return mapDouble("balance");
    }

    @Override
    public int blocks() {
      return mapInt("blocks");
    }

    @Override
    public int connections() {
      return mapInt("connections");
    }

    @Override
    public double difficulty() {
      return mapDouble("difficulty");
    }

    @Override
    public String errors() {
      return mapStr("errors");
    }

    @Override
    public long keyPoolOldest() {
      return mapLong("keypoololdest");
    }

    @Override
    public long keyPoolSize() {
      return mapLong("keypoolsize");
    }

    @Override
    public double payTxFee() {
      return mapDouble("paytxfee");
    }

    @Override
    public long protocolVersion() {
      return mapLong("protocolversion");
    }

    @Override
    public String proxy() {
      return mapStr("proxy");
    }

    @Override
    public double relayFee() {
      return mapDouble("relayfee");
    }

    @Override
    public boolean testnet() {
      return mapBool("testnet");
    }

    @Override
    public int timeOffset() {
      return mapInt("timeoffset");
    }

    @Override
    public long version() {
      return mapLong("version");
    }

    @Override
    public long walletVersion() {
      return mapLong("walletversion");
    }

  }

  private class TxOutSetInfoWrapper extends MapWrapper implements TxOutSetInfo, Serializable {

    public TxOutSetInfoWrapper(Map m) {
      super(m);
    }

    @Override
    public long height() {
      return mapInt("height");
    }

    @Override
    public String bestBlock() {
      return mapStr("bestBlock");
    }

    @Override
    public long transactions() {
      return mapInt("transactions");
    }

    @Override
    public long txouts() {
      return mapInt("txouts");
    }

    @Override
    public long bytesSerialized() {
      return mapInt("bytes_serialized");
    }

    @Override
    public String hashSerialized() {
      return mapStr("hash_serialized");
    }

    @Override
    public BigDecimal totalAmount() {
      return mapBigDecimal("total_amount");
    }
  }

  private class WalletInfoWrapper extends MapWrapper implements WalletInfo, Serializable {

    public WalletInfoWrapper(Map m) { super(m);}

    @Override
    public long walletVersion() {
      return mapLong("walletversion");
    }

    @Override
    public BigDecimal balance() {
      return mapBigDecimal("balance");
    }

    @Override
    public BigDecimal unconfirmedBalance() {
      return mapBigDecimal("unconfirmed_balance");
    }

    @Override
    public BigDecimal immatureBalance() {
      return mapBigDecimal("immature_balance");
    }

    @Override
    public long txCount() {
      return mapLong("txcount");
    }

    @Override
    public long keyPoolOldest() {
      return mapLong("keypoololdest");
    }

    @Override
    public long keyPoolSize() {
      return mapLong("keypoolsize");
    }

    @Override
    public long unlockedUntil() {
      return mapLong("unlocked_until");
    }

    @Override
    public BigDecimal payTxFee() {
      return mapBigDecimal("paytxfee");
    }

    @Override
    public String hdMasterKeyId() {
      return mapStr("hdmasterkeyid");
    }
  }

  private class NetworkInfoWrapper extends MapWrapper implements NetworkInfo, Serializable {

    public NetworkInfoWrapper(Map m) {
      super(m);
    }

    @Override
    public long version() {
      return mapLong("version");
    }

    @Override
    public String subversion() {
      return mapStr("subversion");
    }

    @Override
    public long protocolVersion() {
      return mapLong("protocolversion");
    }

    @Override
    public String localServices() {
      return mapStr("localservices");
    }

    @Override
    public boolean localRelay() {
      return mapBool("localrelay");
    }

    @Override
    public long timeOffset() {
      return mapLong("timeoffset");
    }

    @Override
    public long connections() {
      return mapLong("connections");
    }

    @Override
    public List<Network> networks() {
      List<Map> maps = (List<Map>) m.get("networks");
      List<Network> networks = new LinkedList<Network>();
      for(Map m: maps) {
        Network net = new NetworkWrapper(m);
        networks.add(net);
      }
      return networks;
    }

    @Override
    public BigDecimal relayFee() {
      return mapBigDecimal("relayfee");
    }

    @Override
    public List<String> localAddresses() {
      return (List<String>) m.get("localaddresses");
    }

    @Override
    public String warnings() {
      return mapStr("warnings");
    }
  }

  private class NetworkWrapper extends MapWrapper implements Network, Serializable {

    public NetworkWrapper(Map m) {
      super(m);
    }

    @Override
    public String name() {
      return mapStr("name");
    }

    @Override
    public boolean limited() {
      return mapBool("limited");
    }

    @Override
    public boolean reachable() {
      return mapBool("reachable");
    }

    @Override
    public String proxy() {
      return mapStr("proxy");
    }

    @Override
    public boolean proxyRandomizeCredentials() {
      return mapBool("proxy_randomize_credentials");
    }
  }

  private class MultiSigWrapper extends MapWrapper implements MultiSig, Serializable {

    public MultiSigWrapper(Map m) { super(m);}


    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String redeemScript() {
      return mapStr("redeemScript");
    }
  }

  private class NodeInfoWrapper extends MapWrapper implements NodeInfo, Serializable {

    public NodeInfoWrapper(Map m) { super(m); }

    @Override
    public String addedNode() {
      return mapStr("addednode");
    }

    @Override
    public boolean connected() {
      return mapBool("connected");
    }

    @Override
    public List<Address> addresses() {
        List<Map> maps = (List<Map>) m.get("addresses");
        List<Address> addresses = new LinkedList<Address>();
        for(Map m: maps) {
          Address add = new AddressWrapper(m);
          addresses.add(add);
        }
        return addresses;
      }
  }

  private class AddressWrapper extends MapWrapper implements Address, Serializable {

    public AddressWrapper(Map m) { super(m); }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String connected() {
      return mapStr("connected");
    }
  }


  private class TxOutWrapper extends MapWrapper implements TxOut, Serializable {

    public TxOutWrapper(Map m) {
      super(m);
    }

    @Override
    public String bestBlock() {
      return mapStr("bestblock");
    }

    @Override
    public long confirmations() {
      return mapLong("confirmations");
    }

    @Override
    public BigDecimal value() {
      return mapBigDecimal("value");
    }

    @Override
    public String asm() {
      return mapStr("asm");
    }

    @Override
    public String hex() {
      return mapStr("hex");
    }

    @Override
    public long reqSigs() {
      return mapLong("reqSigs");
    }

    @Override
    public String type() {
      return mapStr("type");
    }

    @Override
    public List<String> addresses() {
      return (List<String>) m.get("addresses");
    }

    @Override
    public long version() {
      return mapLong("version");
    }

    @Override
    public boolean coinBase() {
      return mapBool("coinbase");
    }
  }

  private class MiningInfoWrapper extends MapWrapper implements MiningInfo, Serializable {

    public MiningInfoWrapper(Map m) {
      super(m);
    }

    @Override
    public int blocks() {
      return mapInt("blocks");
    }

    @Override
    public int currentBlockSize() {
      return mapInt("currentblocksize");
    }

    @Override
    public int currentBlockWeight() {
      return mapInt("currentblockweight");
    }

    @Override
    public int currentBlockTx() {
      return mapInt("currentblocktx");
    }

    @Override
    public double difficulty() {
      return mapDouble("difficulty");
    }

    @Override
    public String errors() {
      return mapStr("errors");
    }

    @Override
    public double networkHashps() {
      return Double.valueOf(mapStr("networkhashps"));
    }

    @Override
    public int pooledTx() {
      return mapInt("pooledtx");
    }

    @Override
    public boolean testNet() {
      return mapBool("testnet");
    }

    @Override
    public String chain() {
      return mapStr("chain");
    }
  }

  private class BlockChainInfoMapWrapper extends MapWrapper implements BlockChainInfo, Serializable {

    public BlockChainInfoMapWrapper(Map m) {
      super(m);
    }

    @Override
    public String chain() {
      return mapStr("chain");
    }

    @Override
    public int blocks() {
      return mapInt("blocks");
    }

    @Override
    public String bestBlockHash() {
      return mapStr("bestblockhash");
    }

    @Override
    public double difficulty() {
      return mapDouble("difficulty");
    }

    @Override
    public double verificationProgress() {
      return mapDouble("verificationprogress");
    }

    @Override
    public String chainWork() {
      return mapStr("chainwork");
    }
  }

  private class BlockMapWrapper extends MapWrapper implements Block, Serializable {

    public BlockMapWrapper(Map m) {
      super(m);
    }

    @Override
    public String hash() {
      return mapStr("hash");
    }

    @Override
    public int confirmations() {
      return mapInt("confirmations");
    }

    @Override
    public int size() {
      return mapInt("size");
    }

    @Override
    public int height() {
      return mapInt("height");
    }

    @Override
    public int version() {
      return mapInt("version");
    }

    @Override
    public String merkleRoot() {
      return mapStr("merkleroot");
    }

    @Override
    public String chainwork() {
      return mapStr("chainwork");
    }

    @Override
    public List<String> tx() {
      return (List<String>) m.get("tx");
    }

    @Override
    public Date time() {
      return mapCTime("time");
    }

    @Override
    public long nonce() {
      return mapLong("nonce");
    }

    @Override
    public String bits() {
      return mapStr("bits");
    }

    @Override
    public double difficulty() {
      return mapDouble("difficulty");
    }

    @Override
    public String previousHash() {
      return mapStr("previousblockhash");
    }

    @Override
    public String nextHash() {
      return mapStr("nextblockhash");
    }

    @Override
    public Block previous() throws BitcoinRpcException {
      if (!m.containsKey("previousblockhash"))
        return null;
      return getBlock(previousHash());
    }

    @Override
    public Block next() throws BitcoinRpcException {
      if (!m.containsKey("nextblockhash"))
        return null;
      return getBlock(nextHash());
    }

  }

  @Override
  public Block getBlock(int height) throws BitcoinRpcException {
    String hash = (String) query("getblockhash", height);
    return getBlock(hash);
  }

  @Override
  public Block getBlock(String blockHash) throws BitcoinRpcException {
    return new BlockMapWrapper((Map) query("getblock", blockHash));
  }

  @Override
  public String getBlockHash(int height) throws BitcoinRpcException {
    return (String) query("getblockhash", height);
  }

  @Override
  public BlockChainInfo getBlockChainInfo() throws BitcoinRpcException {
    return new BlockChainInfoMapWrapper((Map) query("getblockchaininfo"));
  }

  @Override
  public int getBlockCount() throws BitcoinRpcException {
    return ((Number) query("getblockcount")).intValue();
  }

  @Override
  public Info getInfo() throws BitcoinRpcException {
    return new InfoWrapper((Map) query("getinfo"));
  }

  @Override
  public TxOutSetInfo getTxOutSetInfo() throws BitcoinRpcException {
    return new TxOutSetInfoWrapper((Map) query("gettxoutsetinfo"));
  }

  @Override
  public NetworkInfo getNetworkInfo() throws BitcoinRpcException {
    return new NetworkInfoWrapper((Map) query("getnetworkinfo"));
  }

  @Override
  public MiningInfo getMiningInfo() throws BitcoinRpcException {
    return new MiningInfoWrapper((Map) query("getmininginfo"));
  }

  @Override
  public List<NodeInfo> getAddedNodeInfo(boolean dummy, String node) throws BitcoinRpcException {
    List<Map> list = ((List<Map>) query("getaddednodeinfo", dummy, node));
    List<NodeInfo> nodeInfoList = new LinkedList<NodeInfo>();
    for(Map m: list){
      NodeInfoWrapper niw = new NodeInfoWrapper(m);
      nodeInfoList.add(niw);
    }
    return nodeInfoList;
  }

  @Override
  public MultiSig createMultiSig(int nRequired, List<String> keys) throws BitcoinRpcException {
    return new MultiSigWrapper ((Map) query("createmultisig", nRequired, keys));
  }

  @Override
  public WalletInfo getWalletInfo() {
    return new WalletInfoWrapper((Map) query("getwalletinfo"));
  }

  @Override
  public String getNewAddress() throws BitcoinRpcException {
    return (String) query("getnewaddress");
  }

  @Override
  public String getNewAddress(String account) throws BitcoinRpcException {
    return (String) query("getnewaddress", account);
  }

  @Override
  public List<String> getRawMemPool() throws BitcoinRpcException {
    return (List<String>) query("getrawmempool");
  }

  @Override
  public String getBestBlockHash() throws BitcoinRpcException {
    return (String) query("getbestblockhash");
  }

  @Override
  public String getRawTransactionHex(String txId) throws BitcoinRpcException {
    return (String) query("getrawtransaction", txId);
  }

  private class RawTransactionImpl extends MapWrapper implements RawTransaction, Serializable {

    public RawTransactionImpl(Map<String, Object> tx) {
      super(tx);
    }

    @Override
    public String hex() {
      return mapStr("hex");
    }

    @Override
    public String txId() {
      return mapStr("txid");
    }

    @Override
    public int version() {
      return mapInt("version");
    }

    @Override
    public long lockTime() {
      return mapLong("locktime");
    }

    @Override
    public String hash() {
      return mapStr("hash");
    }

    @Override
    public long size() {
      return mapLong("size");
    }

    @Override
    public long vsize() {
      return mapLong("vsize");
    }

    private class InImpl extends MapWrapper implements In, Serializable {

      public InImpl(Map m) {
        super(m);
      }

      @Override
      public String txid() {
        return mapStr("txid");
      }

      @Override
      public int vout() {
        return mapInt("vout");
      }

      @Override
      public Map<String, Object> scriptSig() {
        return (Map) m.get("scriptSig");
      }

      @Override
      public long sequence() {
        return mapLong("sequence");
      }

      @Override
      public RawTransaction getTransaction() {
        try {
          return getRawTransaction(mapStr("txid"));
        } catch (BitcoinRpcException ex) {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public Out getTransactionOutput() {
        return getTransaction().vOut().get(mapInt("vout"));
      }

      @Override
      public String scriptPubKey() {
        return mapStr("scriptPubKey");
      }

    }

    @Override
    public List<In> vIn() {
      final List<Map<String, Object>> vIn = (List<Map<String, Object>>) m.get("vin");
      return new AbstractList<In>() {

        @Override
        public In get(int index) {
          return new InImpl(vIn.get(index));
        }

        @Override
        public int size() {
          return vIn.size();
        }
      };
    }

    private class OutImpl extends MapWrapper implements Out, Serializable {

      public OutImpl(Map m) {
        super(m);
      }

      @Override
      public double value() {
        return mapDouble("value");
      }

      @Override
      public int n() {
        return mapInt("n");
      }

      private class ScriptPubKeyImpl extends MapWrapper implements ScriptPubKey, Serializable {

        public ScriptPubKeyImpl(Map m) {
          super(m);
        }

        @Override
        public String asm() {
          return mapStr("asm");
        }

        @Override
        public String hex() {
          return mapStr("hex");
        }

        @Override
        public int reqSigs() {
          return mapInt("reqSigs");
        }

        @Override
        public String type() {
          return mapStr("type");
        }

        @Override
        public List<String> addresses() {
          return (List) m.get("addresses");
        }

      }

      @Override
      public ScriptPubKey scriptPubKey() {
        return new ScriptPubKeyImpl((Map) m.get("scriptPubKey"));
      }

      @Override
      public TxInput toInput() {
        return new BasicTxInput(transaction().txId(), n());
      }

      @Override
      public RawTransaction transaction() {
        return RawTransactionImpl.this;
      }

    }

    @Override
    public List<Out> vOut() {
      final List<Map<String, Object>> vOut = (List<Map<String, Object>>) m.get("vout");
      return new AbstractList<Out>() {

        @Override
        public Out get(int index) {
          return new OutImpl(vOut.get(index));
        }

        @Override
        public int size() {
          return vOut.size();
        }
      };
    }

    @Override
    public String blockHash() {
      return mapStr("blockhash");
    }

    @Override
    public int confirmations() {
      return mapInt("confirmations");
    }

    @Override
    public Date time() {
      return mapCTime("time");
    }

    @Override
    public Date blocktime() {
      return mapCTime("blocktime");
    }

  }

  public class DecodedScriptImpl extends MapWrapper implements DecodedScript, Serializable {

    public DecodedScriptImpl(Map m) {
      super(m);
    }


    @Override
    public String asm() {
      return mapStr("asm");
    }

    @Override
    public String hex() {
      return mapStr("hex");
    }

    @Override
    public String type() {
      return mapStr("type");
    }

    @Override
    public int reqSigs() {
      return mapInt("reqSigs");
    }

    @Override
    public List<String> addresses() {
      return (List) m.get("addresses");
    }

    @Override
    public String p2sh() {
      return mapStr("p2sh");
    }
  }

  public class NetTotalsImpl extends MapWrapper implements NetTotals, Serializable {

    public NetTotalsImpl(Map m) {
      super(m);
    }

    @Override
    public long totalBytesRecv() {
      return mapLong("totalbytesrecv");
    }

    @Override
    public long totalBytesSent() {
      return mapLong("totalbytessent");
    }

    @Override
    public long timeMillis() {
      return mapLong("timemillis");
    }

    public class uploadTargetImpl extends MapWrapper implements uploadTarget, Serializable {

      public uploadTargetImpl(Map m) {
        super(m);
      }


      @Override
      public long timeFrame() {
        return mapLong("timeframe");
      }

      @Override
      public int target() {
        return mapInt("target");
      }

      @Override
      public boolean targetReached() {
        return mapBool("targetreached");
      }

      @Override
      public boolean serveHistoricalBlocks() {
        return mapBool("servehistoricalblocks");
      }

      @Override
      public long bytesLeftInCycle() {
        return mapLong("bytesleftincycle");
      }

      @Override
      public long timeLeftInCycle() {
        return mapLong("timeleftincycle");
      }
    }

    @Override
    public NetTotals.uploadTarget uploadTarget() {
      return new uploadTargetImpl((Map) m.get("uploadtarget"));
    }
  }

  @Override
  public RawTransaction getRawTransaction(String txId) throws BitcoinRpcException {
    return new RawTransactionImpl((Map) query("getrawtransaction", txId, 1));
  }

  @Override
  public double getReceivedByAddress(String address) throws BitcoinRpcException {
    return ((Number) query("getreceivedbyaddress", address)).doubleValue();
  }

  @Override
  public double getReceivedByAddress(String address, int minConf) throws BitcoinRpcException {
    return ((Number) query("getreceivedbyaddress", address, minConf)).doubleValue();
  }

  @Override
  public void importPrivKey(String bitcoinPrivKey) throws BitcoinRpcException {
    query("importprivkey", bitcoinPrivKey);
  }

  @Override
  public void importPrivKey(String bitcoinPrivKey, String label) throws BitcoinRpcException {
    query("importprivkey", bitcoinPrivKey, label);
  }

  @Override
  public void importPrivKey(String bitcoinPrivKey, String label, boolean rescan) throws BitcoinRpcException {
    query("importprivkey", bitcoinPrivKey, label, rescan);
  }

  @Override
  public Object importAddress(String address, String label, boolean rescan) throws BitcoinRpcException {
    query("importaddress", address, label, rescan);
      return null;
  }

  @Override
  public Map<String, Number> listAccounts() throws BitcoinRpcException {
    return (Map) query("listaccounts");
  }

  @Override
  public Map<String, Number> listAccounts(int minConf) throws BitcoinRpcException {
    return (Map) query("listaccounts", minConf);
  }

  private static class ReceivedAddressListWrapper extends AbstractList<ReceivedAddress> {

    private final List<Map<String, Object>> wrappedList;

    public ReceivedAddressListWrapper(List<Map<String, Object>> wrappedList) {
      this.wrappedList = wrappedList;
    }

    @Override
    public ReceivedAddress get(int index) {
      final Map<String, Object> e = wrappedList.get(index);
      return new ReceivedAddress() {

        @Override
        public String address() {
          return (String) e.get("address");
        }

        @Override
        public String account() {
          return (String) e.get("account");
        }

        @Override
        public double amount() {
          return ((Number) e.get("amount")).doubleValue();
        }

        @Override
        public int confirmations() {
          return ((Number) e.get("confirmations")).intValue();
        }

        @Override
        public String toString() {
          return e.toString();
        }

      };
    }

    @Override
    public int size() {
      return wrappedList.size();
    }
  }

  @Override
  public List<ReceivedAddress> listReceivedByAddress() throws BitcoinRpcException {
    return new ReceivedAddressListWrapper((List) query("listreceivedbyaddress"));
  }

  @Override
  public List<ReceivedAddress> listReceivedByAddress(int minConf) throws BitcoinRpcException {
    return new ReceivedAddressListWrapper((List) query("listreceivedbyaddress", minConf));
  }

  @Override
  public List<ReceivedAddress> listReceivedByAddress(int minConf, boolean includeEmpty) throws BitcoinRpcException {
    return new ReceivedAddressListWrapper((List) query("listreceivedbyaddress", minConf, includeEmpty));
  }

  private class TransactionListMapWrapper extends ListMapWrapper<Transaction> {

    public TransactionListMapWrapper(List<Map> list) {
      super(list);
    }

    @Override
    protected Transaction wrap(final Map m) {
      return new Transaction() {

        @Override
        public String account() {
          return mapStr(m, "account");
        }

        @Override
        public String address() {
          return mapStr(m, "address");
        }

        @Override
        public String category() {
          return mapStr(m, "category");
        }

        @Override
        public double amount() {
          return mapDouble(m, "amount");
        }

        @Override
        public double fee() {
          return mapDouble(m, "fee");
        }

        @Override
        public int confirmations() {
          return mapInt(m, "confirmations");
        }

        @Override
        public String blockHash() {
          return mapStr(m, "blockhash");
        }

        @Override
        public int blockIndex() {
          return mapInt(m, "blockindex");
        }

        @Override
        public Date blockTime() {
          return mapCTime(m, "blocktime");
        }

        @Override
        public String txId() {
          return mapStr(m, "txid");
        }

        @Override
        public Date time() {
          return mapCTime(m, "time");
        }

        @Override
        public Date timeReceived() {
          return mapCTime(m, "timereceived");
        }

        @Override
        public String comment() {
          return mapStr(m, "comment");
        }

        @Override
        public String commentTo() {
          return mapStr(m, "to");
        }

        private RawTransaction raw = null;

        @Override
        public RawTransaction raw() {
          if (raw == null)
            try {
              raw = getRawTransaction(txId());
            } catch (BitcoinRpcException ex) {
              throw new RuntimeException(ex);
            }
          return raw;
        }

        @Override
        public String toString() {
          return m.toString();
        }

      };
    }

  }

  private class TransactionsSinceBlockImpl implements TransactionsSinceBlock, Serializable {

    public final List<Transaction> transactions;
    public final String lastBlock;

    public TransactionsSinceBlockImpl(Map r) {
      this.transactions = new TransactionListMapWrapper((List) r.get("transactions"));
      this.lastBlock = (String) r.get("lastblock");
    }

    @Override
    public List<Transaction> transactions() {
      return transactions;
    }

    @Override
    public String lastBlock() {
      return lastBlock;
    }

  }

  @Override
  public TransactionsSinceBlock listSinceBlock() throws BitcoinRpcException {
    return new TransactionsSinceBlockImpl((Map) query("listsinceblock"));
  }

  @Override
  public TransactionsSinceBlock listSinceBlock(String blockHash) throws BitcoinRpcException {
    return new TransactionsSinceBlockImpl((Map) query("listsinceblock", blockHash));
  }

  @Override
  public TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations) throws BitcoinRpcException {
    return new TransactionsSinceBlockImpl((Map) query("listsinceblock", blockHash, targetConfirmations));
  }

  @Override
  public List<Transaction> listTransactions() throws BitcoinRpcException {
    return new TransactionListMapWrapper((List) query("listtransactions"));
  }

  @Override
  public List<Transaction> listTransactions(String account) throws BitcoinRpcException {
    return new TransactionListMapWrapper((List) query("listtransactions", account));
  }

  @Override
  public List<Transaction> listTransactions(String account, int count) throws BitcoinRpcException {
    return new TransactionListMapWrapper((List) query("listtransactions", account, count));
  }

  @Override
  public List<Transaction> listTransactions(String account, int count, int from) throws BitcoinRpcException {
    return new TransactionListMapWrapper((List) query("listtransactions", account, count, from));
  }

  private class UnspentListWrapper extends ListMapWrapper<Unspent> {

    public UnspentListWrapper(List<Map> list) {
      super(list);
    }

    @Override
    protected Unspent wrap(final Map m) {
      return new Unspent() {

        @Override
        public String txid() {
          return mapStr(m, "txid");
        }

        @Override
        public int vout() {
          return mapInt(m, "vout");
        }

        @Override
        public String address() {
          return mapStr(m, "address");
        }

        @Override
        public String scriptPubKey() {
          return mapStr(m, "scriptPubKey");
        }

        @Override
        public String account() {
          return mapStr(m, "account");
        }

        @Override
        public double amount() {
          return MapWrapper.mapDouble(m, "amount");
        }

        @Override
        public int confirmations() {
          return mapInt(m, "confirmations");
        }

      };
    }
  }

  @Override
  public List<Unspent> listUnspent() throws BitcoinRpcException {
    return new UnspentListWrapper((List) query("listunspent"));
  }

  @Override
  public List<Unspent> listUnspent(int minConf) throws BitcoinRpcException {
    return new UnspentListWrapper((List) query("listunspent", minConf));
  }

  @Override
  public List<Unspent> listUnspent(int minConf, int maxConf) throws BitcoinRpcException {
    return new UnspentListWrapper((List) query("listunspent", minConf, maxConf));
  }

  @Override
  public List<Unspent> listUnspent(int minConf, int maxConf, String... addresses) throws BitcoinRpcException {
    return new UnspentListWrapper((List) query("listunspent", minConf, maxConf, addresses));
  }

  @Override
  public String move(String fromAccount, String toBitcoinAddress, double amount) throws BitcoinRpcException {
    return (String) query("move", fromAccount, toBitcoinAddress, amount);
  }

  @Override
  public String move(String fromAccount, String toBitcoinAddress, double amount, int minConf) throws BitcoinRpcException {
    return (String) query("move", fromAccount, toBitcoinAddress, amount, minConf);
  }

  @Override
  public String move(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment) throws BitcoinRpcException {
    return (String) query("move", fromAccount, toBitcoinAddress, amount, minConf, comment);
  }

  @Override
  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount) throws BitcoinRpcException {
    return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount);
  }

  @Override
  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf) throws BitcoinRpcException {
    return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf);
  }

  @Override
  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment) throws BitcoinRpcException {
    return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf, comment);
  }

  @Override
  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment, String commentTo) throws BitcoinRpcException {
    return (String) query("sendfrom", fromAccount, toBitcoinAddress, amount, minConf, comment, commentTo);
  }

  @Override
  public String sendRawTransaction(String hex) throws BitcoinRpcException {
    return (String) query("sendrawtransaction", hex);
  }

  @Override
  public String sendToAddress(String toAddress, double amount) throws BitcoinRpcException {
    return (String) query("sendtoaddress", toAddress, amount);
  }

  @Override
  public String sendToAddress(String toAddress, double amount, String comment) throws BitcoinRpcException {
    return (String) query("sendtoaddress", toAddress, amount, comment);
  }

  @Override
  public String sendToAddress(String toAddress, double amount, String comment, String commentTo) throws BitcoinRpcException {
    return (String) query("sendtoaddress", toAddress, amount, comment, commentTo);
  }

  public String signRawTransaction(String hex) throws BitcoinRpcException {
    return signRawTransaction(hex, null, null, "ALL");
  }

  @Override
  public String signRawTransaction(String hex, List<ExtendedTxInput> inputs, List<String> privateKeys) throws BitcoinRpcException{
    return signRawTransaction(hex, inputs, privateKeys, "ALL");
  }

  public String signRawTransaction(String hex, List<ExtendedTxInput> inputs, List<String> privateKeys, String sigHashType) {
    List<Map> pInputs = null;

    if (inputs != null) {
      pInputs = new ArrayList<>();
      for (final ExtendedTxInput txInput : inputs) {
        pInputs.add(new LinkedHashMap() {
          {
            put("txid", txInput.txid());
            put("vout", txInput.vout());
            put("scriptPubKey", txInput.scriptPubKey());
            put("redeemScript", txInput.redeemScript());
            put("amount", txInput.amount());
          }
        });
      }
    }

    Map result = (Map) query("signrawtransaction", hex, pInputs, privateKeys, sigHashType); //if sigHashType is null it will return the default "ALL"
    if ((Boolean) result.get("complete"))
      return (String) result.get("hex");
    else
      throw new BitcoinRpcException("Incomplete");
  }

  public RawTransaction decodeRawTransaction(String hex) throws BitcoinRpcException {
    Map result = (Map) query("decoderawtransaction", hex);
    RawTransaction rawTransaction = new RawTransactionImpl(result);
    return rawTransaction.vOut().get(0).transaction();
  }

  @Override
  public AddressValidationResult validateAddress(String address) throws BitcoinRpcException {
    final Map validationResult = (Map) query("validateaddress", address);
    return new AddressValidationResult() {

      @Override
      public boolean isValid() {
        return ((Boolean) validationResult.get("isvalid"));
      }

      @Override
      public String address() {
        return (String) validationResult.get("address");
      }

      @Override
      public boolean isMine() {
        return ((Boolean) validationResult.get("ismine"));
      }

      @Override
      public boolean isScript() {
        return ((Boolean) validationResult.get("isscript"));
      }

      @Override
      public String pubKey() {
        return (String) validationResult.get("pubkey");
      }

      @Override
      public boolean isCompressed() {
        return ((Boolean) validationResult.get("iscompressed"));
      }

      @Override
      public String account() {
        return (String) validationResult.get("account");
      }

      @Override
      public String toString() {
        return validationResult.toString();
      }

    };
  }

  @Override
  public void setGenerate(boolean b) throws BitcoinRPCException {
    query("setgenerate", b);
  }

  @Override
  public List<String> generate(int numBlocks) throws BitcoinRPCException {
    return (List<String>) query("generate", numBlocks);
  }

//    static {
//        logger.setLevel(Level.ALL);
//        for (Handler handler : logger.getParent().getHandlers())
//            handler.setLevel(Level.ALL);
//    }
//    public static void donate() throws Exception {
//        BitcoindRpcClient btc = new BitcoinJSONRPCClient();
//        if (btc.getBalance() > 10)
//            btc.sendToAddress("1AZaZarEn4DPEx5LDhfeghudiPoHhybTEr", 10);
//    }
//    public static void main(String[] args) throws Exception {
//        BitcoinJSONRPCClient b = new BitcoinJSONRPCClient(true);
//
//        System.out.println(b.listTransactions());
//        
////        String aa = "mjrxsupqJGBzeMjEiv57qxSKxgd3SVwZYd";
////        String ab = "mpN3WTJYsrnnWeoMzwTxkp8325nzArxnxN";
////        String ac = b.getNewAddress("TEST");
////        
////        System.out.println(b.getBalance("", 0));
////        System.out.println(b.sendFrom("", ab, 0.1));
////        System.out.println(b.sendToAddress(ab, 0.1, "comment", "tocomment"));
////        System.out.println(b.getReceivedByAddress(ab));
////        System.out.println(b.sendToAddress(ac, 0.01));
////        
////        System.out.println(b.validateAddress(ac));
////        
//////        b.importPrivKey(b.dumpPrivKey(aa));
////        
////        System.out.println(b.getAddressesByAccount("TEST"));
////        System.out.println(b.listReceivedByAddress());
//    }
  @Override
  public double getEstimateFee(int nBlocks) throws BitcoinRpcException {
    return ((Number) query("estimatefee", nBlocks)).doubleValue();
  }

  @Override
  public double getEstimatePriority(int nBlocks) throws BitcoinRpcException {
    return ((Number) query("estimatepriority", nBlocks)).doubleValue();
  }

  @Override
  public void invalidateBlock(String hash) throws BitcoinRpcException {
    query("invalidateblock", hash);
  }

  @Override
  public void reconsiderBlock(String hash) throws BitcoinRpcException {
    query("reconsiderblock", hash);

  }

  private class PeerInfoWrapper extends MapWrapper implements PeerInfoResult, Serializable {

    public PeerInfoWrapper(Map m) {
      super(m);
    }

    @Override
    public long getId() {
      return mapLong("id");
    }

    @Override
    public String getAddr() {
      return mapStr("addr");
    }

    @Override
    public String getAddrLocal() {
      return mapStr("addrlocal");
    }

    @Override
    public String getServices() {
      return mapStr("services");
    }

    @Override
    public long getLastSend() {
      return mapLong("lastsend");
    }

    @Override
    public long getLastRecv() {
      return mapLong("lastrecv");
    }

    @Override
    public long getBytesSent() {
      return mapLong("bytessent");
    }

    @Override
    public long getBytesRecv() {
      return mapLong("bytesrecv");
    }

    @Override
    public long getConnTime() {
      return mapLong("conntime");
    }

    @Override
    public int getTimeOffset() {
      return mapInt("timeoffset");
    }

    @Override
    public double getPingTime() {
      return mapDouble("pingtime");
    }

    @Override
    public long getVersion() {
      return mapLong("version");
    }

    @Override
    public String getSubVer() {
      return mapStr("subver");
    }

    @Override
    public boolean isInbound() {
      return mapBool("inbound");
    }

    @Override
    public int getStartingHeight() {
      return mapInt("startingheight");
    }

    @Override
    public long getBanScore() {
      return mapLong("banscore");
    }

    @Override
    public int getSyncedHeaders() {
      return mapInt("synced_headers");
    }

    @Override
    public int getSyncedBlocks() {
      return mapInt("synced_blocks");
    }

    @Override
    public boolean isWhiteListed() {
      return mapBool("whitelisted");
    }

  }

  @Override
  public List<PeerInfoResult> getPeerInfo() throws BitcoinRpcException {
    final List<Map> l = (List<Map>) query("getpeerinfo");
//    final List<PeerInfoResult> res = new ArrayList<>(l.size());
//    for (Map m : l)
//      res.add(new PeerInfoWrapper(m));
//    return res;
    return new AbstractList<PeerInfoResult>() {

      @Override
      public PeerInfoResult get(int index) {
        return new PeerInfoWrapper(l.get(index));
      }

      @Override
      public int size() {
        return l.size();
      }
    };
  }

  @Override
  public void stop() {
    query("stop");
  }

  @Override
  public String getRawChangeAddress() throws BitcoinRpcException {
    return (String) query("getrawchangeaddress");
  }

  @Override
  public long getConnectionCount() throws BitcoinRpcException {
    return (long) query("getconnectioncount");
  }

  @Override
  public double getUnconfirmedBalance() throws BitcoinRpcException {
    return (double) query("getunconfirmedbalance");
  }

  @Override
  public double getDifficulty() throws BitcoinRpcException {
    if (query("getdifficulty") instanceof Long) {
      return ((Long) query("getdifficulty")).doubleValue();
    } else {
      return (double) query("getdifficulty");
    }
  }

  @Override
  public NetTotals getNetTotals() throws BitcoinRpcException {
    return new NetTotalsImpl((Map) query("getnettotals"));
  }

  @Override
  public DecodedScript decodeScript(String hex) throws BitcoinRpcException {
    return new DecodedScriptImpl((Map) query("decodescript", hex));
  }

  @Override
  public void ping() throws BitcoinRpcException {
    query("ping");
  }

  //It doesn't work!
  @Override
  public boolean getGenerate() throws BitcoinRPCException {
    return (boolean) query("getgenerate");
  }


  @Override
  public double getNetworkHashPs() throws BitcoinRpcException {
    return (Double)query("getnetworkhashps");
  }

  @Override
  public boolean setTxFee(BigDecimal amount) throws BitcoinRpcException {
    return (boolean) query("settxfee", amount);
  }

  /**
   *
   * @param node example: "192.168.0.6:8333"
   * @param command must be either "add", "remove" or "onetry"
   * @throws BitcoinRpcException
   */
  @Override
  public void addNode(String node, String command) throws BitcoinRpcException {
    query("addnode", node, command);
  }

  @Override
  public void backupWallet(String destination) throws BitcoinRpcException {
    query("backupwallet", destination);
  }

  @Override
  public String signMessage(String bitcoinAdress, String message) throws BitcoinRpcException {
    return (String) query("signmessage", bitcoinAdress, message);
  }

  @Override
  public void dumpWallet(String filename) throws BitcoinRpcException {
    query("dumpwallet", filename);
  }

  @Override
  public void importWallet(String filename) throws BitcoinRpcException {
    query("dumpwallet", filename);
  }

  @Override
  public void keyPoolRefill() throws BitcoinRpcException {
    keyPoolRefill(100); //default is 100 if you don't send anything
  }

  public void keyPoolRefill(long size) throws BitcoinRpcException {
    query("keypoolrefill", size);
  }

  @Override
  public BigDecimal getReceivedByAccount(String account) throws BitcoinRpcException {
    return getReceivedByAccount(account, 1);
  }

  public BigDecimal getReceivedByAccount(String account, int minConf) throws BitcoinRpcException {
    return new BigDecimal((String)query("getreceivedbyaccount", account, minConf));
  }

  @Override
  public void encryptWallet(String passPhrase) throws BitcoinRpcException {
    query("encryptwallet", passPhrase);
  }

  @Override
  public void walletPassPhrase(String passPhrase, long timeOut) throws BitcoinRpcException {
    query("walletpassphrase", passPhrase, timeOut);
  }

  @Override
  public boolean verifyMessage(String bitcoinAddress, String signature, String message) throws BitcoinRpcException {
    return (boolean) query("verifymessage", bitcoinAddress, signature, message);
  }

  @Override
  public String addMultiSigAddress(int nRequired, List<String> keyObject) throws BitcoinRpcException {
    return (String) query("addmultisigaddress", nRequired, keyObject);
  }

  @Override
  public String addMultiSigAddress(int nRequired, List<String> keyObject, String account) throws BitcoinRpcException {
    return (String) query("addmultisigaddress", nRequired, keyObject, account);
  }

  @Override
  public boolean verifyChain() {
    return verifyChain(3, 6); //3 and 6 are the default values
  }

  public boolean verifyChain(int checklevel, int numblocks) {
    return (boolean)query("verifychain", checklevel, numblocks);
  }

  /**
   * Attempts to submit new block to network.
   * The 'jsonparametersobject' parameter is currently ignored, therefore left out.
   * @param hexData
   */
  @Override
  public void submitBlock(String hexData) {
    query("submitblock", hexData);
  }

  @Override
  public TxOut getTxOut(String txId, long vout) throws BitcoinRpcException {
    return new TxOutWrapper((Map) query("gettxout", txId, vout, true));
  }

  public TxOut getTxOut(String txId, long vout, boolean includemempool) throws BitcoinRpcException {
    return new TxOutWrapper((Map) query("gettxout", txId, vout, includemempool));
  }


}

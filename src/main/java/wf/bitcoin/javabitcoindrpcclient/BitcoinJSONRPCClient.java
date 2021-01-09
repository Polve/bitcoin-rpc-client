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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang3.StringUtils;

import wf.bitcoin.javabitcoindrpcclient.config.RpcClientConfig;
import wf.bitcoin.krotjson.Base64Coder;
import wf.bitcoin.krotjson.HexCoder;
import wf.bitcoin.krotjson.JSON;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ at azazar.com Small modifications by
 * Alessandro Polverini polverini at gmail.com
 */
public class BitcoinJSONRPCClient implements BitcoindRpcClient {

  private static final Logger logger = Logger.getLogger(BitcoindRpcClient.class.getPackage().getName());

  public static final URL DEFAULT_JSONRPC_URL;
  public static final URL DEFAULT_JSONRPC_TESTNET_URL;
  public static final URL DEFAULT_JSONRPC_REGTEST_URL;

  public static final Charset QUERY_CHARSET = Charset.forName("ISO8859-1");
  public static final int CONNECT_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(1);
  public static final int READ_TIMEOUT = (int) TimeUnit.MINUTES.toMillis(5);

  static {
    String user = "user";
    String password = "pass";
    String host = "localhost";
    String port = null;

    try {
      File configFile = null;
      File home = new File(System.getProperty("user.home"));
      String manuallyConfiguredDataFolderPath = RpcClientConfig.get().bitcoinCoreDataFolder();

      if (!StringUtils.isEmpty(manuallyConfiguredDataFolderPath) &&
    		  (configFile = new File(manuallyConfiguredDataFolderPath, "bitcoin.conf")
    		  ).exists())
      {
    	  // Look for the config file in the configured bitcoin core data folder
    	  logger.fine("Using configured data dir: " + manuallyConfiguredDataFolderPath);
      }
      else if ((configFile = new File(home, ".bitcoin" + File.separatorChar +
    		  							"bitcoin.conf")
    		  ).exists())
      {
    	  // Look for the config file on the Linux path
      }
      else if ((configFile = new File(home, "snap" + File.separatorChar +
    		  								"bitcoin-core" + File.separatorChar +
    		  								"common" + File.separatorChar +
    		  								".bitcoin" + File.separatorChar +
    		  								"bitcoin.conf")
    		  ).exists())
      {
    	  // Look for the config file on the Linux path, when bitcoind was installed via snap
    	  // Path is: ~/snap/bitcoin-core/common/.bitcoin/bitcoin.conf
      }
      else if ((configFile = new File(home, "AppData" + File.separatorChar +
    		  								"Roaming" + File.separatorChar +
    		  								"Bitcoin" + File.separatorChar +
    		  								"bitcoin.conf")
    		  ).exists())
      {
    	  // Look for the cofig file on the Windows path
      } else
        configFile = null;

      // If config file is found, attempt to parse its contents
      if (configFile != null) {
        logger.fine("Bitcoin configuration file: " + configFile);

        Properties configProps = new Properties();
        try (FileInputStream i = new FileInputStream(configFile)) {
          configProps.load(i);
        }

        user = configProps.getProperty("rpcuser", user);
        password = configProps.getProperty("rpcpassword", password);
        host = configProps.getProperty("rpcconnect", host);
        port = configProps.getProperty("rpcport", port);

        // rpcuser and rpcpassword are being phased out of bitcoind
        // bitcoind shows this warning when these configs are used:
        // "Config options rpcuser and rpcpassword will soon be deprecated. Locally-run instances may remove rpcuser to use cookie-based auth, or may be replaced with rpcauth"
        // Two alternatives for authentication are offered:
        // 1) the config rpcauth, which has the format {0}:{1}${2} containing username, salt, password_hmac
        // See https://github.com/bitcoin/bitcoin/tree/master/share/rpcauth
        // However, this only contains a hash of the password
        // This means the password (which is needed in cleartext for authenticaton) cannot be retrieved anymore from bitcoin.conf
        // 2) the .cookie temporary file
        // When bitcoind starts (and rpcuser / rpcauth are not used), it creates a temporary .cookie file
        // This contains a temporary password for the RPC API
        // The .cookie file is automatically deleted when bitcoind is stopped
        // Option 2) seems like the best one to use for this client, so warn user if rpcuser / rpcpassword are still used

        // Show warning if legacy auth mechanism (using rpcuser / rpcpassword) detected
        if (configProps.getProperty("rpcuser") != null || configProps.getProperty("rpcpassword") != null)
        {
        	logger.warning("Currently relying on rpcuser / rpcpassword for authentication. "
        			+ "This will soon be deprecated in bitcoind. "
        			+ "To use newer auth mechanism based on a temporary password, remove properties rpcuser / rpcpassword from bitcoin.conf");
        }

        // Also show warning if rpcauth mechanism is detected
        if (configProps.getProperty("rpcauth") != null)
        {
        	logger.severe("Currently relying on rpcauth mechanism for authentication. "
        			+ "This cannot be used by this library, because the password needed for API authentication cannot be retrieved from bitcoin.conf. "
        			+ "To use newer auth mechanism based on a temporary password, remove the property rpcauth from bitcoin.conf");
        }

        // Look for .cookie file, which is in a subfolder of the .bitcoin folder
        // Subfolder is one of regtest, testnet3, or mainnet - depending on which mode bitcoind is currently using
        Optional<Path> cookieFile = Files.walk(configFile.getParentFile().toPath())
        		.filter(f -> f.toFile().getName().equals(".cookie"))
        		.findFirst();

        if (cookieFile.isPresent())
        {
        	Path cookieFilePath = cookieFile.get();

            // Format is __cookie__:tempPassword
            String cookieFileContents = new String(Files.readAllBytes(cookieFilePath));

            String[] temp = cookieFileContents.split(":");
            user = temp[0];
            password = temp[1];
        }
      }

    } catch (Exception ex) {
      logger.log(Level.SEVERE, null, ex);
    }

    try {
      DEFAULT_JSONRPC_URL = new URL("http://" + user + ':' + password + "@" + host + ":" + (port == null ? "8332" : port) + "/");
      DEFAULT_JSONRPC_TESTNET_URL = new URL("http://" + user + ':' + password + "@" + host + ":" + (port == null ? "18332" : port) + "/");
      DEFAULT_JSONRPC_REGTEST_URL = new URL("http://" + user + ':' + password + "@" + host + ":" + (port == null ? "18443" : port) + "/");
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public final URL rpcURL;

  private HostnameVerifier hostnameVerifier;
  private SSLSocketFactory sslSocketFactory;
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

  public BitcoinJSONRPCClient(boolean testNet) {
    this(testNet ? DEFAULT_JSONRPC_TESTNET_URL : DEFAULT_JSONRPC_URL);
  }

  public BitcoinJSONRPCClient() {
    this(DEFAULT_JSONRPC_TESTNET_URL);
  }

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

  @SuppressWarnings("serial")
  protected byte[] prepareRequest(final String method, final Object... params) {
    return JSON.stringify(new LinkedHashMap<String, Object>() {
      {
        put("method", method);
        put("params", params);
        put("id", "1");
      }
    }).getBytes(QUERY_CHARSET);
  }

  @SuppressWarnings("serial")
  protected byte[] prepareBatchRequest(final String method, final List<BatchParam> paramsList) {
    return JSON.stringify(paramsList.stream().map(batchParam-> new LinkedHashMap<String, Object>() {
      {
        put("method", method);
        put("params", batchParam.params);
        put("id", batchParam.id);
      }
    }).collect(Collectors.toList())).getBytes(QUERY_CHARSET);
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

  @SuppressWarnings("rawtypes")
  public Object loadResponse(InputStream in, Object expectedID, boolean close) throws IOException, GenericRpcException {
    try {
      String r = new String(loadStream(in, close), QUERY_CHARSET);
      logger.log(Level.FINE, "Bitcoin JSON-RPC response:\n{0}", r);
      try {
        Map response = (Map) JSON.parse(r);

        return getResponseObject(expectedID, response);
      } catch (ClassCastException ex) {
        throw new BitcoinRPCException("Invalid server response format (data: \"" + r + "\")");
      }
    } finally {
      if (close)
        in.close();
    }
  }

    @SuppressWarnings("rawtypes")
    public Object loadBatchResponse(InputStream in, List<BatchParam> batchParams, boolean close) throws IOException, GenericRpcException {
        try {
            String r = new String(loadStream(in, close), QUERY_CHARSET);
            logger.log(Level.FINE, "Bitcoin JSON-RPC response:\n{0}", r);
            try {
                List<Map> response = (List<Map>) JSON.parse(r);

                return response.stream().map(item-> {
                  try {
                    Object expectedId = batchParams.stream()
                            .filter(batchParam -> batchParam.id.equals(item.get("id")))
                            .findFirst().orElseGet(()->new BatchParam(null, null)).id;
                    return getResponseObject(expectedId, item);
                  } catch (BitcoinRPCException e) {
                    return e;
                  }
                  }).collect(Collectors.toList());
            } catch (ClassCastException ex) {
                throw new BitcoinRPCException("Invalid server response format (data: \"" + r + "\")");
            }
        } finally {
            if (close)
                in.close();
        }
    }

  private Object getResponseObject(Object expectedID, Map response) {
    if (!expectedID.equals(response.get("id")))
      throw new BitcoinRPCException("Wrong response ID (expected: " + String.valueOf(expectedID) + ", response: " + response.get("id") + ")");

    if (response.get("error") != null)
      throw new BitcoinRPCException(new BitcoinRPCError(response));

    return response.get("result");
  }

  /**
   * Set an authenticated connection with Bitcoin server
   */
  private HttpURLConnection setConnection() {
    HttpURLConnection conn;
    try {
      conn = (HttpURLConnection) noAuthURL.openConnection();

      conn.setDoOutput(true);
      conn.setDoInput(true);

      conn.setConnectTimeout(CONNECT_TIMEOUT);
      conn.setReadTimeout(READ_TIMEOUT);

      if (conn instanceof HttpsURLConnection) {
        if (hostnameVerifier != null)
          ((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
        if (sslSocketFactory != null)
          ((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
      }
      conn.setRequestProperty("Authorization", "Basic " + authStr);
      return conn;
    } catch (IOException ex) {
      throw new BitcoinRPCException("Fail to set authenticated connection with server");
    }
  }

  public Object query(String method, Object... o) throws GenericRpcException {
    HttpURLConnection conn = setConnection();
    try {
      byte[] r = prepareRequest(method, o);
      logger.log(Level.FINE, "Bitcoin JSON-RPC request:\n{0}", new String(r, QUERY_CHARSET));
      conn.getOutputStream().write(r);
      conn.getOutputStream().close();
      int responseCode = conn.getResponseCode();
      if (responseCode != 200) {
        InputStream errorStream = conn.getErrorStream();
        throw new BitcoinRPCException(method,
                                      Arrays.deepToString(o),
                                      responseCode,
                                      conn.getResponseMessage(),
                                      errorStream == null ? null : new String(loadStream(errorStream, true)));
      }
      return loadResponse(conn.getInputStream(), "1", true);
    } catch (IOException ex) {
      throw new BitcoinRPCException(method, Arrays.deepToString(o), ex);
    }
  }

  public Object batchQuery(String method, List<BatchParam> batchParams) throws GenericRpcException {
    HttpURLConnection conn = setConnection();
    try {
      byte[] r = prepareBatchRequest(method, batchParams);
      logger.log(Level.FINE, "Bitcoin JSON-RPC request:\n{0}", new String(r, QUERY_CHARSET));
      conn.getOutputStream().write(r);
      conn.getOutputStream().close();
      int responseCode = conn.getResponseCode();
      if (responseCode != 200) {
        InputStream errorStream = conn.getErrorStream();
        throw new BitcoinRPCException(method,
                batchParams.stream().map(param->Arrays.deepToString(param.params)).collect(Collectors.joining()),
                responseCode,
                conn.getResponseMessage(),
                errorStream == null ? null : new String(loadStream(errorStream, true)));
      }
      return loadBatchResponse((conn.getInputStream()), batchParams, true);
    } catch (IOException ex) {
      throw new BitcoinRPCException(method, batchParams.stream()
              .map(param->Arrays.deepToString(param.params)).collect(Collectors.joining()), ex);
    }
  }

  @Override
  @SuppressWarnings("serial")
  public String createRawTransaction(List<TxInput> inputs, List<TxOutput> outputs) throws GenericRpcException {
    List<Map<String, ?>> pInputs = new ArrayList<>();

    for (final TxInput txInput : inputs) {
      pInputs.add(new LinkedHashMap<String, Object>() {
        {
          put("txid", txInput.txid());
          put("vout", txInput.vout());
        }
      });
    }

    Map<String, Object> pOutputs = new LinkedHashMap<>();

    for (TxOutput txOutput : outputs) {
      pOutputs.put(txOutput.address(), txOutput.amount());
      if (txOutput.data() != null) {
        String hex = HexCoder.encode(txOutput.data());
        pOutputs.put("data", hex);
      }
    }

    return (String) query("createrawtransaction", pInputs, pOutputs);
  }

  @Override
  public String dumpPrivKey(String address) throws GenericRpcException {
    return (String) query("dumpprivkey", address);
  }

  @Override
  public String getAccount(String address) throws GenericRpcException {
    return (String) query("getaccount", address);
  }

  @Override
  public String getAccountAddress(String account) throws GenericRpcException {
    return (String) query("getaccountaddress", account);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getAddressesByAccount(String account) throws GenericRpcException {
    return (List<String>) query("getaddressesbyaccount", account);
  }

  @Override
  public BigDecimal getBalance() throws GenericRpcException {
    return (BigDecimal) query("getbalance");
  }

  @Override
  public BigDecimal getBalance(String account) throws GenericRpcException {
    return (BigDecimal) query("getbalance", account);
  }

  @Override
  public BigDecimal getBalance(String account, int minConf) throws GenericRpcException {
    return (BigDecimal) query("getbalance", account, minConf);
  }

  @Override
  @SuppressWarnings("unchecked")
  public SmartFeeResult estimateSmartFee(int blocks) {
    return new SmartFeeResultMapWrapper((Map<String, ?>) query("estimatesmartfee", blocks));
  }

  @Override
  public Block getBlock(int height) throws GenericRpcException {
    String hash = (String) query("getblockhash", height);
    return getBlock(hash);
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public Block getBlock(String blockHash) throws GenericRpcException {
    return new BlockMapWrapper((Map<String, ?>) query("getblock", blockHash));
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public BlockWithTxInfo getBlockWithTxInfo(String blockHash) throws GenericRpcException {
    return new BlockWithTxInfoMapWrapper((Map<String, ?>) query("getblock", blockHash, 2)); // verbosity = 2
  }

  @Override
  public String getRawBlock(String blockHash) throws GenericRpcException {
    return (String) query("getblock", blockHash, false);
  }

  @Override
  public String getBlockHash(int height) throws GenericRpcException {
    return (String) query("getblockhash", height);
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public BlockChainInfo getBlockChainInfo() throws GenericRpcException {
    return new BlockChainInfoMapWrapper((Map<String, ?>) query("getblockchaininfo"));
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public AddressInfo getAddressInfo(String address) throws GenericRpcException {
    return new AddressInfoMapWrapper((Map<String, ?>) query("getaddressinfo", address));
  }

  @Override
  public int getBlockCount() throws GenericRpcException {
    return ((Number) query("getblockcount")).intValue();
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public TxOutSetInfo getTxOutSetInfo() throws GenericRpcException {
    return new TxOutSetInfoWrapper((Map<String, ?>) query("gettxoutsetinfo"));
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public NetworkInfo getNetworkInfo() throws GenericRpcException {
    return new NetworkInfoWrapper((Map<String, ?>) query("getnetworkinfo"));
  }

  @Override
  @SuppressWarnings({ "unchecked" })
  public MiningInfo getMiningInfo() throws GenericRpcException {
    return new MiningInfoWrapper((Map<String, ?>) query("getmininginfo"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<NodeInfo> getAddedNodeInfo(boolean dummy, String node) throws GenericRpcException {
    List<Map<String, ?>> list = ((List<Map<String, ?>>) query("getaddednodeinfo", dummy, node));
    List<NodeInfo> nodeInfoList = new LinkedList<NodeInfo>();
    for (Map<String, ?> m : list) {
      NodeInfoWrapper niw = new NodeInfoWrapper(m);
      nodeInfoList.add(niw);
    }
    return nodeInfoList;
  }

  @Override
  @SuppressWarnings("unchecked")
  public MultiSig createMultiSig(int nRequired, List<String> keys) throws GenericRpcException {
    return new MultiSigWrapper((Map<String, ?>) query("createmultisig", nRequired, keys));
  }

  @Override
  @SuppressWarnings("unchecked")
  public WalletInfo getWalletInfo() {
    return new WalletInfoWrapper((Map<String, ?>) query("getwalletinfo"));
  }

  @Override
  public String getNewAddress() throws GenericRpcException {
    return (String) query("getnewaddress");
  }

  @Override
  public String getNewAddress(String account) throws GenericRpcException {
    return (String) query("getnewaddress", account);
  }

  @Override
  public String getNewAddress(String account, String addressType) throws GenericRpcException {
    return (String) query("getnewaddress", account, addressType);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> getRawMemPool() throws GenericRpcException {
    return (List<String>) query("getrawmempool");
  }

  @Override
  public String getBestBlockHash() throws GenericRpcException {
    return (String) query("getbestblockhash");
  }

  @Override
  public String getRawTransactionHex(String txId) throws GenericRpcException {
    return (String) query("getrawtransaction", txId);
  }

  @Override
  @SuppressWarnings("unchecked")
  public RawTransaction getRawTransaction(String txId) throws GenericRpcException {
    return new RawTransactionImpl((Map<String, Object>) query("getrawtransaction", txId, 1));
  }

  @SuppressWarnings("unchecked")
  public List<RawTransaction> getRawTransactions(List<String> txIds) throws GenericRpcException {
    List<Object> rawTransactions = (List<Object>) batchQuery("getrawtransaction",
            txIds.stream().map(txId -> new BatchParam(txId, new Object[]{txId, 1}))
                    .collect(Collectors.toList()));

    return rawTransactions.stream().<RawTransaction>map(rawTransaction -> {
      if (rawTransaction instanceof BitcoinRPCException) {
        Map<String, Object> builtErroredTx = new LinkedHashMap<>();
        BitcoinRPCError rpcError = ((BitcoinRPCException) rawTransaction).getRPCError();
        builtErroredTx.put("txid", rpcError.getId());
        builtErroredTx.put("error", rpcError.getMessage());
        return new RawTransactionImpl(builtErroredTx);
      } else {
        return new RawTransactionImpl((Map<String, ?>) rawTransaction);
      }
    }).collect(Collectors.toList());
  }

  @Override
  public BigDecimal getReceivedByAddress(String address) throws GenericRpcException {
    return (BigDecimal) query("getreceivedbyaddress", address);
  }

  @Override
  public BigDecimal getReceivedByAddress(String address, int minConf) throws GenericRpcException {
    return (BigDecimal) query("getreceivedbyaddress", address, minConf);
  }

  @Override
  public void importPrivKey(String bitcoinPrivKey) throws GenericRpcException {
    query("importprivkey", bitcoinPrivKey);
  }

  @Override
  public void importPrivKey(String bitcoinPrivKey, String label) throws GenericRpcException {
    query("importprivkey", bitcoinPrivKey, label);
  }

  @Override
  public void importPrivKey(String bitcoinPrivKey, String label, boolean rescan) throws GenericRpcException {
    query("importprivkey", bitcoinPrivKey, label, rescan);
  }

  @Override
  public Object importAddress(String address, String label, boolean rescan) throws GenericRpcException {
    query("importaddress", address, label, rescan);
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Number> listAccounts() throws GenericRpcException {
    return (Map<String, Number>) query("listaccounts");
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Number> listAccounts(int minConf) throws GenericRpcException {
    return (Map<String, Number>) query("listaccounts", minConf);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Map<String, Number> listAccounts(int minConf, boolean watchonly) throws GenericRpcException {
    return (Map<String, Number>) query("listaccounts", minConf, watchonly);
  }

  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public List<LockedUnspent> listLockUnspent() {

    return new ListMapWrapper<LockedUnspent>((List<Map<String, ?>>) query("listlockunspent")) {
      protected LockedUnspent wrap(final Map m) {
        return new LockedUnspentWrapper(m);
      }
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReceivedAddress> listReceivedByAddress() throws GenericRpcException {
    return new ReceivedAddressListWrapper((List<Map<String, ?>>) query("listreceivedbyaddress"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReceivedAddress> listReceivedByAddress(int minConf) throws GenericRpcException {
    return new ReceivedAddressListWrapper((List<Map<String, ?>>) query("listreceivedbyaddress", minConf));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<ReceivedAddress> listReceivedByAddress(int minConf, boolean includeEmpty) throws GenericRpcException {
    return new ReceivedAddressListWrapper((List<Map<String, ?>>) query("listreceivedbyaddress", minConf, includeEmpty));
  }

  @Override
  @SuppressWarnings("unchecked")
  public TransactionsSinceBlock listSinceBlock() throws GenericRpcException {
    return new TransactionsSinceBlockImpl((Map<String, ?>) query("listsinceblock"));
  }

  @Override
  @SuppressWarnings("unchecked")
 public TransactionsSinceBlock listSinceBlock(String blockHash) throws GenericRpcException {
    return new TransactionsSinceBlockImpl((Map<String, ?>) query("listsinceblock", blockHash));
  }

  @Override
  @SuppressWarnings("unchecked")
  public TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations) throws GenericRpcException {
    return new TransactionsSinceBlockImpl((Map<String, ?>) query("listsinceblock", blockHash, targetConfirmations));
  }

  @Override
  @SuppressWarnings("unchecked")
  public TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations, boolean includeWatchOnly) throws GenericRpcException {
    return new TransactionsSinceBlockImpl((Map<String, ?>) query("listsinceblock", blockHash, targetConfirmations, includeWatchOnly));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Transaction> listTransactions() throws GenericRpcException {
    return new TransactionListMapWrapper((List<Map<String, ?>>) query("listtransactions"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Transaction> listTransactions(String account) throws GenericRpcException {
    return new TransactionListMapWrapper((List<Map<String, ?>>) query("listtransactions", account));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Transaction> listTransactions(String account, int count) throws GenericRpcException {
    return new TransactionListMapWrapper((List<Map<String, ?>>) query("listtransactions", account, count));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Transaction> listTransactions(String account, int count, int skip) throws GenericRpcException {
    return new TransactionListMapWrapper((List<Map<String, ?>>) query("listtransactions", account, count, skip));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Unspent> listUnspent() throws GenericRpcException {
    return new UnspentListWrapper((List<Map<String, ?>>) query("listunspent"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Unspent> listUnspent(int minConf) throws GenericRpcException {
    return new UnspentListWrapper((List<Map<String, ?>>) query("listunspent", minConf));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Unspent> listUnspent(int minConf, int maxConf) throws GenericRpcException {
    return new UnspentListWrapper((List<Map<String, ?>>) query("listunspent", minConf, maxConf));
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Unspent> listUnspent(int minConf, int maxConf, String... addresses) throws GenericRpcException {
    return new UnspentListWrapper((List<Map<String, ?>>) query("listunspent", minConf, maxConf, addresses));
  }

  public boolean lockUnspent(boolean unlock, String txid, int vout) throws GenericRpcException {
    Map<String, Object> params = new LinkedHashMap<>();
    params.put("txid", txid);
    params.put("vout", vout);
    return (boolean) query("lockunspent", unlock, Arrays.asList(params).toArray());
  }

  @Override
  public boolean move(String fromAccount, String toAddress, BigDecimal amount) throws GenericRpcException {
    return (boolean) query("move", fromAccount, toAddress, amount);
  }

  @Override
  public boolean move(String fromAccount, String toAddress, BigDecimal amount, String comment) throws GenericRpcException {
    return (boolean) query("move", fromAccount, toAddress, amount, 0, comment);
  }

  @Override
  public boolean move(String fromAccount, String toAddress, BigDecimal amount, int minConf) throws GenericRpcException {
    return (boolean) query("move", fromAccount, toAddress, amount, minConf);
  }

  @Override
  public boolean move(String fromAccount, String toAddress, BigDecimal amount, int minConf, String comment) throws GenericRpcException {
    return (boolean) query("move", fromAccount, toAddress, amount, minConf, comment);
  }

  @Override
  public String sendFrom(String fromAccount, String toAddress, BigDecimal amount) throws GenericRpcException {
    return (String) query("sendfrom", fromAccount, toAddress, amount);
  }

  @Override
  public String sendFrom(String fromAccount, String toAddress, BigDecimal amount, int minConf) throws GenericRpcException {
    return (String) query("sendfrom", fromAccount, toAddress, amount, minConf);
  }

  @Override
  public String sendFrom(String fromAccount, String toAddress, BigDecimal amount, int minConf, String comment) throws GenericRpcException {
    return (String) query("sendfrom", fromAccount, toAddress, amount, minConf, comment);
  }

  @Override
  public String sendFrom(String fromAccount, String toAddress, BigDecimal amount, int minConf, String comment, String commentTo) throws GenericRpcException {
    return (String) query("sendfrom", fromAccount, toAddress, amount, minConf, comment, commentTo);
  }

  @Override
  public String sendRawTransaction(String hex) throws GenericRpcException {
    return (String) query("sendrawtransaction", hex);
  }

  @Override
  public String sendToAddress(String toAddress, BigDecimal amount) throws GenericRpcException {
    return (String) query("sendtoaddress", toAddress, amount);
  }

  @Override
  public String sendToAddress(String toAddress, BigDecimal amount, String comment) throws GenericRpcException {
    return (String) query("sendtoaddress", toAddress, amount, comment);
  }

  @Override
  public String sendToAddress(String toAddress, BigDecimal amount, String comment, String commentTo) throws GenericRpcException {
    return (String) query("sendtoaddress", toAddress, amount, comment, commentTo);
  }

  @Deprecated
  public String signRawTransaction(String hex) throws GenericRpcException {
    return signRawTransaction(hex, null, null, "ALL");
  }

  @Override
  @Deprecated
  public String signRawTransaction(String hex, List<? extends TxInput> inputs, List<String> privateKeys) throws GenericRpcException {
    return signRawTransaction(hex, inputs, privateKeys, "ALL");
  }

  @SuppressWarnings({ "serial", "unchecked" })
  @Deprecated
  public String signRawTransaction(String hex, List<? extends TxInput> inputs, List<String> privateKeys, String sigHashType) {
    List<Map<String, ?>> pInputs = null;

    if (inputs != null) {
      pInputs = new ArrayList<>();
      for (final TxInput txInput : inputs) {
        pInputs.add(new LinkedHashMap<String, Object>() {
          {
            put("txid", txInput.txid());
            put("vout", txInput.vout());
            put("scriptPubKey", txInput.scriptPubKey());
            if (txInput instanceof ExtendedTxInput) {
              ExtendedTxInput extin = (ExtendedTxInput) txInput;
              put("redeemScript", extin.redeemScript());
              put("amount", extin.amount());
            }
          }
        });
      }
    }

    Map<String, ?> result = (Map<String, ?>) query("signrawtransaction", hex, pInputs, privateKeys, sigHashType); //if sigHashType is null it will return the default "ALL"
    if ((Boolean) result.get("complete"))
      return (String) result.get("hex");
    else
      throw new GenericRpcException("Incomplete");
  }

	@SuppressWarnings("serial")
	@Override
	public SignedRawTransaction signRawTransactionWithKey(String hex, List<String> privateKeys, List<? extends TxInput> prevTxs, SignatureHashType sigHashType)
	{
	    List<Map<String, ?>> prevTxsJson = null;
		if (prevTxs != null)
		{
			prevTxsJson = new ArrayList<>();
			for (TxInput txInput : prevTxs)
			{
				prevTxsJson.add(new LinkedHashMap<String, Object>() {
					{
						put("txid", txInput.txid());
						put("vout", txInput.vout());
						put("scriptPubKey", txInput.scriptPubKey());
						put("amount", txInput.amount());

						if (txInput instanceof ExtendedTxInput)
						{
							ExtendedTxInput extIn = (ExtendedTxInput) txInput;
							put("redeemScript", extIn.redeemScript());
							put("witnessScript", extIn.witnessScript());
						}
					}
				});
			}
		}

	    @SuppressWarnings("unchecked")
		Map<String, ?> result = (Map<String, ?>) query("signrawtransactionwithkey",
	    		hex,
	    		privateKeys,
	    		prevTxsJson,
	    		sigHashType);

	    return new SignedRawTransactionWrapper(result);
	}

  @SuppressWarnings("unchecked")
  public RawTransaction decodeRawTransaction(String hex) throws GenericRpcException {
    Map<String, ?> result = (Map<String, ?>) query("decoderawtransaction", hex);
    RawTransaction rawTransaction = new RawTransactionImpl(result);
    return rawTransaction.vOut().get(0).transaction();
  }

  @Override
  @SuppressWarnings("unchecked")
  public AddressValidationResult validateAddress(String address) throws GenericRpcException {
    final Map<String, ?> m = (Map<String, ?>) query("validateaddress", address);
    return new AddressValidationResultWrapper(m);
  }

  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<String> generate(int numBlocks) throws BitcoinRPCException {
    return (List<String>) query("generate", numBlocks);
  }

  @Deprecated
  @Override
  @SuppressWarnings("unchecked")
  public List<String> generate(int numBlocks, long maxTries) throws BitcoinRPCException {
    return (List<String>) query("generate", numBlocks, maxTries);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> generateToAddress(int numBlocks, String address) throws BitcoinRPCException {
    return (List<String>) query("generatetoaddress", numBlocks, address);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<String> generateToAddress(int numBlocks, String address, long maxTries) throws BitcoinRPCException {
    return (List<String>) query("generatetoaddress", numBlocks, address, maxTries);
  }

  @Override
  public BigDecimal estimateFee(int nBlocks) throws GenericRpcException {
    return (BigDecimal) query("estimatefee", nBlocks);
  }

  @Override
  public void invalidateBlock(String hash) throws GenericRpcException {
    query("invalidateblock", hash);
  }

  @Override
  public void reconsiderBlock(String hash) throws GenericRpcException {
    query("reconsiderblock", hash);

  }

  @Override
  @SuppressWarnings("unchecked")
  public List<PeerInfoResult> getPeerInfo() throws GenericRpcException {
    final List<Map<String, ?>> l = (List<Map<String, ?>>) query("getpeerinfo");
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
  public String getRawChangeAddress() throws GenericRpcException {
    return (String) query("getrawchangeaddress");
  }

  @Override
  public long getConnectionCount() throws GenericRpcException {
    return (long) query("getconnectioncount");
  }

  @Override
  public BigDecimal getUnconfirmedBalance() throws GenericRpcException {
    return (BigDecimal) query("getunconfirmedbalance");
  }

  @Override
  public BigDecimal getDifficulty() throws GenericRpcException {
    return (BigDecimal) query("getdifficulty");
  }

  @Override
  @SuppressWarnings("unchecked")
 public NetTotals getNetTotals() throws GenericRpcException {
    return new NetTotalsImpl((Map<String, ?>) query("getnettotals"));
  }

  @Override
  @SuppressWarnings("unchecked")
  public DecodedScript decodeScript(String hex) throws GenericRpcException {
    return new DecodedScriptImpl((Map<String, ?>) query("decodescript", hex));
  }

  @Override
  public void ping() throws GenericRpcException {
    query("ping");
  }

  @Override
  public BigDecimal getNetworkHashPs() throws GenericRpcException {
    return (BigDecimal) query("getnetworkhashps");
  }

  @Override
  public boolean setTxFee(BigDecimal amount) throws GenericRpcException {
    return (boolean) query("settxfee", amount);
  }

  /**
   *
   * @param node example: "192.168.0.6:8333"
   * @param command must be either "add", "remove" or "onetry"
   * @throws GenericRpcException
   */
  @Override
  public void addNode(String node, String command) throws GenericRpcException {
    query("addnode", node, command);
  }

  @Override
  public void backupWallet(String destination) throws GenericRpcException {
    query("backupwallet", destination);
  }

  @Override
  public String signMessage(String bitcoinAdress, String message) throws GenericRpcException {
    return (String) query("signmessage", bitcoinAdress, message);
  }

  @Override
  public void dumpWallet(String filename) throws GenericRpcException {
    query("dumpwallet", filename);
  }

  @Override
  public void importWallet(String filename) throws GenericRpcException {
    query("dumpwallet", filename);
  }

  @Override
  public void keyPoolRefill() throws GenericRpcException {
    keyPoolRefill(100); //default is 100 if you don't send anything
  }

  public void keyPoolRefill(long size) throws GenericRpcException {
    query("keypoolrefill", size);
  }

  @Override
  public BigDecimal getReceivedByAccount(String account) throws GenericRpcException {
    return getReceivedByAccount(account, 1);
  }

  public BigDecimal getReceivedByAccount(String account, int minConf) throws GenericRpcException {
    return new BigDecimal((String) query("getreceivedbyaccount", account, minConf));
  }

  @Override
  public void encryptWallet(String passPhrase) throws GenericRpcException {
    query("encryptwallet", passPhrase);
  }

  @Override
  public void walletPassPhrase(String passPhrase, long timeOut) throws GenericRpcException {
    query("walletpassphrase", passPhrase, timeOut);
  }

  @Override
  public boolean verifyMessage(String bitcoinAddress, String signature, String message) throws GenericRpcException {
    return (boolean) query("verifymessage", bitcoinAddress, signature, message);
  }

  @SuppressWarnings("unchecked")
  @Override
  public MultiSig addMultiSigAddress(int nRequired, List<String> keyObject) throws GenericRpcException {
    return new MultiSigWrapper((Map<String, ?>) query("addmultisigaddress", nRequired, keyObject));
  }

  @SuppressWarnings("unchecked")
  @Override
  public MultiSig addMultiSigAddress(int nRequired, List<String> keyObject, String account) throws GenericRpcException {
    return new MultiSigWrapper((Map<String, ?>) query("addmultisigaddress", nRequired, keyObject, account));
  }

  @Override
  public boolean verifyChain() {
    return verifyChain(3, 6); //3 and 6 are the default values
  }

  public boolean verifyChain(int checklevel, int numblocks) {
    return (boolean) query("verifychain", checklevel, numblocks);
  }

  /**
   * Attempts to submit new block to network. The 'jsonparametersobject'
   * parameter is currently ignored, therefore left out.
   *
   * @param hexData
   */
  @Override
  public void submitBlock(String hexData) {
    query("submitblock", hexData);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Transaction getTransaction(String txId) {

    TransactionWrapper tx = new TransactionWrapper((Map<String, ?>) query("gettransaction", txId));

    // [#88] Request for invalid Tx should fail
    // https://github.com/Polve/JavaBitcoindRpcClient/issues/88
    RawTransaction rawTx = tx.raw();
    if (rawTx == null || rawTx.vIn().isEmpty() || rawTx.vOut().isEmpty()) {
      throw new BitcoinRPCException("Invalid Tx: " + txId);
    }

    return tx;
  }

  @Override
  @SuppressWarnings("unchecked")
  public TxOut getTxOut(String txId, long vout) throws GenericRpcException {
    TxOutWrapper txOut = new TxOutWrapper((Map<String, ?>) query("gettxout", txId, vout, true));
    if (txOut.m == null) {
      return null;
    }
    return txOut;
  }

  @SuppressWarnings("unchecked")
  public TxOut getTxOut(String txId, long vout, boolean includemempool) throws GenericRpcException {
    TxOutWrapper txOut = new TxOutWrapper((Map<String, ?>) query("gettxout", txId, vout, includemempool));
    if (txOut.m == null) {
      return null;
    }
    return txOut;
  }

  @SuppressWarnings("unchecked")
  public AddressBalance getAddressBalance(String address) {
    return new AddressBalanceWrapper((Map<String, ?>) query("getaddressbalance", address));
  }

  @SuppressWarnings("unchecked")
  public List<AddressUtxo> getAddressUtxo(String address) {
    return new AddressUtxoList((List<Map<String, ?>>) query("getaddressutxos", address));
  }


  @SuppressWarnings("unchecked")
  @Override
  public UtxoSet scanTxOutSet(List<ScanObject> scanObjects) throws GenericRpcException {

    List<Map<String, Object>>  param = new ArrayList<>();
    for (ScanObject obj : scanObjects) {
      Map<String, Object> map = new HashMap<String, Object>();
      map.put("desc", obj.getDescriptor());
      map.put("range", obj.getRange());
      param.add(map);
    }
    UtxoSetWrapper utxoWrapper = new UtxoSetWrapper((Map<String, ?>) query("scantxoutset", "start",
        param));
    return utxoWrapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Integer scanTxOutSetStatus() throws GenericRpcException {
    Map<String, ?> result = (Map<String, ?>) query("scantxoutset", "status");
    if (result != null && result.containsKey("progress")) {
      int progress = ((Long) result.get("progress")).intValue();
      return progress;
    } else {
      return null;
    }
  }

  @Override
  public Boolean abortScanTxOutSet() throws GenericRpcException {
    return (Boolean) query("scantxoutset", "abort");
  }

  @Override
  public UtxoSet scanTxOutSetAddresses(List<String> addresses) throws GenericRpcException {
    List<ScanObject> list = new ArrayList<>();
    for (String addr : addresses) {
      list.add(new ScanObject("addr(" + addr  + ")", null));
    }
    return scanTxOutSet(list);
  }

	 @Override
	  public UtxoSet scanTxOutSetPubKey(String pubkey, int range) throws GenericRpcException {
	   ScanObject scanObj = new ScanObject("combo(" + pubkey  + ")", range);
	    return scanTxOutSet(Arrays.asList(scanObj));
	  }

  @SuppressWarnings("serial")
  private class AddressBalanceWrapper extends MapWrapper implements AddressBalance, Serializable
  {
    private AddressBalanceWrapper(Map<String, ?> r) {
      super(r);
    }

    @Override
    public long getBalance() {
      return mapLong("balance");
    }

    public long getReceived() {
      return mapLong("received");
    }
  }

  private class AddressUtxoWrapper implements AddressUtxo {
    private String address;
    private String txid;
    private int outputIndex;
    private String script;
    private long satoshis;
    private long height;

    private AddressUtxoWrapper(Map<String, ?> result) {
      address = getOrDefault(result, "address", "").toString();
      txid = getOrDefault(result, "txid", "").toString();
      outputIndex = getOrDefault(result, "outputIndex", 0);
      script = getOrDefault(result, "script", "").toString();
      satoshis = getOrDefault(result, "satoshis", 0L);
      height = getOrDefault(result, "height", -1L);
    }

    @SuppressWarnings("unchecked")
    private <T extends Object> T getOrDefault(Map<String, ?> result, String key, T defval) {
      T val = (T) result.get(key);
      return val != null ? val : defval;
    }

    public String getAddress() {
      return address;
    }

    public String getTxid() {
      return txid;
    }

    public int getOutputIndex() {
      return outputIndex;
    }

    public String getScript() {
      return script;
    }

    public long getSatoshis() {
      return satoshis;
    }

    public long getHeight() {
      return height;
    }
  }

  private class AddressUtxoList extends ListMapWrapper<AddressUtxo> {

    private AddressUtxoList(List<Map<String, ?>> list) {
          super((List<Map<String, ?>>)list);
      }

      @Override
      protected AddressUtxo wrap(Map<String, ?> m) {
          return new AddressUtxoWrapper(m);
      }
  }

  @SuppressWarnings("serial")
  private class AddressValidationResultWrapper extends MapWrapper implements AddressValidationResult {

    private AddressValidationResultWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public boolean isValid() {
      return mapBool("isvalid");
    }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public boolean isMine() {
      return mapBool("ismine");
    }

    @Override
    public boolean isScript() {
      return mapBool("isscript");
    }

    @Override
    public String pubKey() {
      return mapStr("pubkey");
    }

    @Override
    public boolean isCompressed() {
      return mapBool("iscompressed");
    }

    @Override
    public String account() {
      return mapStr("account");
    }
  };

  @SuppressWarnings("serial")
  private class AddressWrapper extends MapWrapper implements Address, Serializable {

    private AddressWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String connected() {
      return mapStr("connected");
    }
  }

  @SuppressWarnings("serial")
  private class BlockChainInfoMapWrapper extends MapWrapper implements BlockChainInfo, Serializable {

    private BlockChainInfoMapWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String chain() {
      return mapStr("chain");
    }

    @Override
    public Integer blocks() {
      return mapInt("blocks");
    }

    @Override
    public String bestBlockHash() {
      return mapStr("bestblockhash");
    }

    @Override
    public BigDecimal difficulty() {
      return mapBigDecimal("difficulty");
    }

    @Override
    public BigDecimal verificationProgress() {
      return mapBigDecimal("verificationprogress");
    }

    @Override
    public String chainWork() {
      return mapStr("chainwork");
    }

	@Override
	public Integer headers()
	{
		return mapInt("headers");
	}

	@Override
	public Long medianTime()
	{
		return mapLong("mediantime");
	}

	@Override
	public Boolean initialBlockDownload()
	{
		return mapBool("initialblockdownload");
	}

	@Override
	public Long sizeOnDisk()
	{
		return mapLong("size_on_disk");
	}

	@Override
	public Boolean pruned()
	{
		return mapBool("pruned");
	}

	@Override
	public Integer pruneHeight()
	{
		return mapInt("pruneheight");
	}

	@Override
	public Boolean automaticPruning()
	{
		return mapBool("automatic_pruning");
	}

	@Override
	public Long pruneTargetSize()
	{
		return mapLong("prune_target_size");
	}

	@Override
	public String warnings()
	{
		return mapStr("warnings");
	}
  }

	private class AddressInfoMapWrapper extends MapWrapper implements AddressInfo, Serializable
	{
		private static final long serialVersionUID = 8801943420993238518L;

		private AddressInfoMapWrapper(Map<String, ?> m)
		{
			super(m);
		}

		@Override
		public String address()
		{
			return mapStr("address");
		}

		@Override
		public String scriptPubKey()
		{
			return mapStr("scriptPubKey");
		}

		@Override
		public Boolean isMine()
		{
			return mapBool("ismine");
		}

		@Override
		public Boolean isWatchOnly()
		{
			return mapBool("iswatchonly");
		}

		@Override
		public Boolean solvable()
		{
			return mapBool("solvable");
		}

		@Override
		public String desc()
		{
			return mapStr("desc");
		}

		@Override
		public Boolean isScript()
		{
			return mapBool("isscript");
		}

		@Override
		public Boolean isChange()
		{
			return mapBool("ischange");
		}

		@Override
		public Boolean isWitness()
		{
			return mapBool("iswitness");
		}

		@Override
		public Integer witnessVersion()
		{
			return mapInt("witness_version");
		}

		@Override
		public String witnessProgram()
		{
			return mapStr("witness_program");
		}

		@Override
		public String script()
		{
			return mapStr("script");
		}

		@Override
		public String hex()
		{
			return mapStr("hex");
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<String> pubKeys()
		{
			if (! m.containsKey("pubkeys"))
				return null;

			return (List<String>) m.get("pubkeys");
		}

		@Override
		public Integer sigsRequired()
		{
			return mapInt("sigsrequired");
		}

		@Override
		public String pubKey()
		{
			return mapStr("pubkey");
		}

		@SuppressWarnings("unchecked")
		@Override
		public AddressInfo embedded()
		{
			if (! m.containsKey("embedded"))
				return null;

			return new AddressInfoMapWrapper((Map<String, ?>) m.get("embedded"));
		}

		@Override
		public Boolean isCompressed()
		{
			return mapBool("iscompressed");
		}

		@Override
		public String label()
		{
			return mapStr("label");
		}

		@Override
		public Long timestamp()
		{
			return mapLong("timestamp");
		}

		@Override
		public String hdKeyPath()
		{
			return mapStr("hdkeypath");
		}

		@Override
		public String hdSeedId()
		{
			return mapStr("hdseedid");
		}

		@Override
		public String hdMasterFingerprint()
		{
			return mapStr("hdmasterfingerprint");
		}

		@Override
		public List<AddressInfoLabel> labels()
		{
			if (! m.containsKey("labels"))
				return null;

			@SuppressWarnings("unchecked")
			List<Map<String, ?>> list = (List<Map<String, ?>>) m.get("labels");

			return new AddressInfoLabelList(list);
		}
	}

	private class AddressInfoLabelList extends ListMapWrapper<AddressInfoLabel>
	{
		private AddressInfoLabelList(List<Map<String, ?>> list)
		{
			super((List<Map<String, ?>>) list);
		}

		@Override
		protected AddressInfoLabel wrap(Map<String, ?> m)
		{
			return new AddressInfoLabelWrapper(m);
		}
	}



  private abstract class BlockBaseMapWrapper extends MapWrapper {
    private BlockBaseMapWrapper(Map<String, ?> m) {
      super(m);
    }

    public String hash() {
      return mapStr("hash");
    }

    public int confirmations() {
      return mapInt("confirmations");
    }

    public int size() {
      return mapInt("size");
    }

    public int height() {
      return mapInt("height");
    }

    public int version() {
      return mapInt("version");
    }

    public String merkleRoot() {
      return mapStr("merkleroot");
    }

    public String chainwork() {
      return mapStr("chainwork");
    }

    public Date time() {
      return mapDate("time");
    }

    public long nonce() {
      return mapLong("nonce");
    }

    public String bits() {
      return mapStr("bits");
    }

    public BigDecimal difficulty() {
      return mapBigDecimal("difficulty");
    }

    public String previousHash() {
      return mapStr("previousblockhash");
    }

    public String nextHash() {
      return mapStr("nextblockhash");
    }
  }

  private class BlockWithTxInfoMapWrapper extends BlockBaseMapWrapper implements BlockWithTxInfo, Serializable {

    private BlockWithTxInfoMapWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<RawTransaction> tx() {
      List<Map<String, ?>> txList = (List<Map<String, ?>>) m.get("tx");
      List<RawTransaction> transactions = new LinkedList<RawTransaction>();
      for (Map<String, ?> m : txList) {
        RawTransactionImpl tx = new RawTransactionImpl(m);
        transactions.add(tx);
      }
      return transactions;
    }

    @Override
    public BlockWithTxInfo previous() throws GenericRpcException {
      if (!m.containsKey("previousblockhash"))
        return null;
      return getBlockWithTxInfo(previousHash());
    }

    @Override
    public BlockWithTxInfo next() throws GenericRpcException {
      if (!m.containsKey("nextblockhash"))
        return null;
      return getBlockWithTxInfo(nextHash());
    }
  }

  @SuppressWarnings("serial")
  private class BlockMapWrapper extends BlockBaseMapWrapper implements Block, Serializable {

    private BlockMapWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> tx() {
      return (List<String>) m.get("tx");
    }

    @Override
    public Block previous() throws GenericRpcException {
      if (!m.containsKey("previousblockhash"))
        return null;
      return getBlock(previousHash());
    }

    @Override
    public Block next() throws GenericRpcException {
      if (!m.containsKey("nextblockhash"))
        return null;
      return getBlock(nextHash());
    }

  }
  @SuppressWarnings("serial")
  private class DecodedScriptImpl extends MapWrapper implements DecodedScript, Serializable {

    private DecodedScriptImpl(Map<String, ?> m) {
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
    @SuppressWarnings("unchecked")
    public List<String> addresses() {
      return (List<String>) m.get("addresses");
    }

    @Override
    public String p2sh() {
      return mapStr("p2sh");
    }
  }

  @SuppressWarnings("serial")
  private class LockedUnspentWrapper extends MapWrapper implements LockedUnspent {

    private LockedUnspentWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String txId() {
      return (String) m.get("txid");
    }

    @Override
    public int vout() {
      return ((Long) m.get("vout")).intValue();
    }
  }

  @SuppressWarnings("serial")
  private class MiningInfoWrapper extends MapWrapper implements MiningInfo, Serializable {

    private MiningInfoWrapper(Map<String, ?> m) {
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
    public BigDecimal difficulty() {
      return mapBigDecimal("difficulty");
    }

    @Override
    public String errors() {
      return mapStr("errors");
    }

    @Override
    public BigDecimal networkHashps() {
      return mapBigDecimal("networkhashps");
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

  @SuppressWarnings("serial")
  private class MultiSigWrapper extends MapWrapper implements MultiSig, Serializable {

    private MultiSigWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String redeemScript() {
      return mapStr("redeemScript");
    }
  }

  @SuppressWarnings("serial")
  private class NetTotalsImpl extends MapWrapper implements NetTotals, Serializable {

    private NetTotalsImpl(Map<String, ?> m) {
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

    private class uploadTargetImpl extends MapWrapper implements uploadTarget, Serializable {

      public uploadTargetImpl(Map<String, ?> m) {
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
    @SuppressWarnings("unchecked")
    public NetTotals.uploadTarget uploadTarget() {
      return new uploadTargetImpl((Map<String, ?>) m.get("uploadtarget"));
    }
  }

  @SuppressWarnings("serial")
  private class NetworkInfoWrapper extends MapWrapper implements NetworkInfo, Serializable {

    private NetworkInfoWrapper(Map<String, ?> m) {
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
    @SuppressWarnings("unchecked")
    public List<Network> networks() {
      List<Map<String, ?>> maps = (List<Map<String, ?>>) m.get("networks");
      List<Network> networks = new LinkedList<Network>();
      for (Map<String, ?> m : maps) {
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
    @SuppressWarnings("unchecked")
    public List<String> localAddresses() {
      return (List<String>) m.get("localaddresses");
    }

    @Override
    public String warnings() {
      return mapStr("warnings");
    }
  }

  @SuppressWarnings("serial")
  private class NetworkWrapper extends MapWrapper implements Network, Serializable {

    private NetworkWrapper(Map<String, ?> m) {
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

  @SuppressWarnings("serial")
  private class NodeInfoWrapper extends MapWrapper implements NodeInfo, Serializable {

    private NodeInfoWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String addedNode() {
      return mapStr("addednode");
    }

    @Override
    public boolean connected() {
      return mapBool("connected");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Address> addresses() {
      List<Map<String, ?>> maps = (List<Map<String, ?>>) m.get("addresses");
      List<Address> addresses = new LinkedList<Address>();
      for (Map<String, ?> m : maps) {
        Address add = new AddressWrapper(m);
        addresses.add(add);
      }
      return addresses;
    }
  }

  @SuppressWarnings("serial")
  private class PeerInfoWrapper extends MapWrapper implements PeerInfoResult, Serializable {

    private PeerInfoWrapper(Map<String, ?> m) {
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
    public BigDecimal getPingTime() {
      return mapBigDecimal("pingtime");
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

  @SuppressWarnings("serial")
  private class RawTransactionImpl extends MapWrapper implements RawTransaction, Serializable {

    private RawTransactionImpl(Map<String, ?> tx) {
      super(tx);
    }

    @Override
    public String error() {
      return mapStr("error");
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

      private InImpl(Map<String, ?> m) {
        super(m);
      }

      @Override
      public String txid() {
        return mapStr("txid");
      }

      @Override
      public Integer vout() {
        return mapInt("vout");
      }

      @Override
      public BigDecimal amount() {
    	return mapBigDecimal("amount");
      }

      @Override
      @SuppressWarnings("unchecked")
      public Map<String, Object> scriptSig() {
        return (Map<String, Object>) m.get("scriptSig");
      }

      @Override
      public long sequence() {
        return mapLong("sequence");
      }

      @Override
      public RawTransaction getTransaction() {
        try {
          return getRawTransaction(mapStr("txid"));
        } catch (GenericRpcException ex) {
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
    @SuppressWarnings("unchecked")
    public List<In> vIn() {
      final List<Map<String, ?>> vIn = (List<Map<String, ?>>) m.get("vin");
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

      private OutImpl(Map<String, ?> m) {
        super(m);
      }

      @Override
      public BigDecimal value() {
        return mapBigDecimal("value");
      }

      @Override
      public int n() {
        return mapInt("n");
      }

      private class ScriptPubKeyImpl extends MapWrapper implements ScriptPubKey, Serializable {

        public ScriptPubKeyImpl(Map<String, ?> m) {
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
        @SuppressWarnings("unchecked")
        public List<String> addresses() {
          return (List<String>) m.get("addresses");
        }

      }

      @Override
      @SuppressWarnings("unchecked")
      public ScriptPubKey scriptPubKey() {
        return new ScriptPubKeyImpl((Map<String, ?>) m.get("scriptPubKey"));
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
    @SuppressWarnings("unchecked")
    public List<Out> vOut() {
      final List<Map<String, ?>> vOut = (List<Map<String, ?>>) m.get("vout");
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
    public Integer confirmations() {
      Object o = m.get("confirmations");
      return o == null ? null : ((Number)o).intValue();
    }

    @Override
    public Date time() {
      return mapDate("time");
    }

    @Override
    public Date blocktime() {
      return mapDate("blocktime");
    }
  }

  private class ReceivedAddressListWrapper extends AbstractList<ReceivedAddress> {

    private final List<Map<String, ?>> wrappedList;

    private ReceivedAddressListWrapper(List<Map<String, ?>> wrappedList) {
      this.wrappedList = wrappedList;
    }

    @Override
    public ReceivedAddress get(int index) {
      final Map<String, ?> m = wrappedList.get(index);
      return new ReceivedAddressWrapper(m);
    }

    @Override
    public int size() {
      return wrappedList.size();
    }
  }

  @SuppressWarnings("serial")
  private class ReceivedAddressWrapper extends MapWrapper implements ReceivedAddress {

    private ReceivedAddressWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String account() {
      return mapStr("account");
    }

    @Override
    public BigDecimal amount() {
      return mapBigDecimal("amount");
    }

    @Override
    public int confirmations() {
      return mapInt("confirmations");
    }
  }

  @SuppressWarnings("serial")
  private class SmartFeeResultMapWrapper extends MapWrapper implements SmartFeeResult, Serializable {

    private SmartFeeResultMapWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public BigDecimal feeRate() {
      return mapBigDecimal("feerate");
    }

    @Override
    public int blocks() {
      return mapInt("blocks");
    }

    @Override
    public String errors() {
      return mapStr("errors");
    }
  }

  @SuppressWarnings("serial")
  private class TransactionWrapper extends MapWrapper implements Transaction, Serializable {

    private TransactionWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String account() {
      return mapStr("account");
    }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String category() {
      return mapStr("category");
    }

    @Override
    public BigDecimal amount() {
      return mapBigDecimal("amount");
    }

    @Override
    public BigDecimal fee() {
      return mapBigDecimal("fee");
    }

    @Override
    public int confirmations() {
      return mapInt("confirmations");
    }

    @Override
    public String blockHash() {
      return mapStr("blockhash");
    }

    @Override
    public int blockIndex() {
      return mapInt("blockindex");
    }

    @Override
    public Date blockTime() {
      return mapDate("blocktime");
    }

    @Override
    public String txId() {
      return mapStr("txid");
    }

    @Override
    public Date time() {
      return mapDate("time");
    }

    @Override
    public Date timeReceived() {
      return mapDate("timereceived");
    }

    @Override
    public String comment() {
      return mapStr("comment");
    }

    @Override
    public String commentTo() {
      return mapStr("to");
    }

    @Override
    public boolean generated() {
      return mapBool("generated");
    }

    private RawTransaction raw = null;

    @Override
    public RawTransaction raw() {
      if (raw == null)
        try {
          raw = getRawTransaction(txId());
        } catch (GenericRpcException ex) {
          logger.warning(ex.getMessage());
        }
      return raw;
    }

    @Override
    public String toString() {
      return m.toString();
    }
  }

  private class TransactionListMapWrapper extends ListMapWrapper<Transaction> {

    private TransactionListMapWrapper(List<Map<String, ?>> list) {
      super(list);
    }

    @Override
    protected Transaction wrap(Map<String, ?> m) {
      return new TransactionWrapper(m);
    }
  }

  @SuppressWarnings("serial")
  private class TransactionsSinceBlockImpl implements TransactionsSinceBlock, Serializable {

    private final List<Transaction> transactions;
    private final String lastBlock;

    @SuppressWarnings("unchecked")
    private TransactionsSinceBlockImpl(Map<String, ?> r) {
      this.transactions = new TransactionListMapWrapper((List<Map<String, ?>>) r.get("transactions"));
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

  @SuppressWarnings("serial")
  private class TxOutSetInfoWrapper extends MapWrapper implements TxOutSetInfo, Serializable {

    private TxOutSetInfoWrapper(Map<String, ?> m) {
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

  @SuppressWarnings("serial")
  private class TxOutWrapper extends MapWrapper implements TxOut, Serializable {

    private TxOutWrapper(Map<String, ?> m) {
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
    @SuppressWarnings("unchecked")
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

  private class UnspentListWrapper extends ListMapWrapper<Unspent> {

    private UnspentListWrapper(List<Map<String, ?>> list) {
      super(list);
    }

    @Override
    protected Unspent wrap(Map<String, ?> m) {
      return new UnspentWrapper(m);
    }
  }

  @SuppressWarnings("serial")
  private class UnspentWrapper extends MapWrapper implements Unspent {

	private UnspentWrapper(Map<String, ?> m) {
      super(m);
    }

    @Override
    public String txid() {
      return mapStr("txid");
    }

    @Override
    public Integer vout() {
      return mapInt("vout");
    }

    @Override
    public String address() {
      return mapStr("address");
    }

    @Override
    public String scriptPubKey() {
      return mapStr("scriptPubKey");
    }

    @Deprecated
    @Override
    public String account() {
      return mapStr("account");
    }

    @Override
    public BigDecimal amount() {
      return mapBigDecimal("amount");
    }

    @Override
    public byte[] data() {
      return mapHex("data");
    }

    @Override
    public Integer confirmations() {
      return mapInt("confirmations");
    }

	@Override
	public String redeemScript() {
		return mapStr("redeemScript");
	}

	@Override
	public String witnessScript() {
		return mapStr("witnessScript");
	}

	@Override
	public String label()
	{
		return mapStr("label");
	}

	@Override
	public Boolean spendable()
	{
		return mapBool("spendable");
	}

	@Override
	public Boolean solvable()
	{
		return mapBool("solvable");
	}

	@Override
	public String desc()
	{
		return mapStr("desc");
	}

	@Override
	public Boolean safe()
	{
		return mapBool("safe");
	}
  }

  @SuppressWarnings("serial")
  private class WalletInfoWrapper extends MapWrapper implements WalletInfo, Serializable {

    private WalletInfoWrapper(Map<String, ?> m) {
      super(m);
    }

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

	@SuppressWarnings("serial")
	private class SignedRawTransactionWrapper extends MapWrapper implements SignedRawTransaction, Serializable
	{
		private SignedRawTransactionWrapper(Map<String, ?> m)
		{
			super(m);
		}

		@Override
		public String hex()
		{
			return mapStr("hex");
		}

		@Override
		public boolean complete()
		{
			return mapBool("complete");
		}

		@Override
		public List<RawTransactionSigningOrVerificationError> errors()
		{
			if (! m.containsKey("errors"))
				return null;

			@SuppressWarnings("unchecked")
			List<Map<String, ?>> list = (List<Map<String, ?>>) m.get("errors");

			return new RawTransactionSigningOrVerificationErrorList(list);
		}
	}

	private class RawTransactionSigningOrVerificationErrorList
			extends ListMapWrapper<RawTransactionSigningOrVerificationError>
	{

		private RawTransactionSigningOrVerificationErrorList(List<Map<String, ?>> list)
		{
			super((List<Map<String, ?>>) list);
		}

		@Override
		protected RawTransactionSigningOrVerificationError wrap(Map<String, ?> m)
		{
			return new RawTransactionSigningOrVerificationErrorWrapper(m);
		}
	}

	@SuppressWarnings("serial")
	private class RawTransactionSigningOrVerificationErrorWrapper extends MapWrapper implements RawTransactionSigningOrVerificationError, Serializable
	{
		private RawTransactionSigningOrVerificationErrorWrapper(Map<String, ?> m)
		{
			super(m);
		}

		@Override
		public String txId()
		{
			return mapStr("txid");
		}

		@Override
		public int vOut()
		{
			return mapInt("vout");
		}

		@Override
		public String scriptSig()
		{
			return mapStr("scriptSig");
		}

		@Override
		public int n()
		{
			return mapInt("sequence");
		}

		@Override
		public String error()
		{
			return mapStr("error");
		}
	}

	private class AddressInfoLabelWrapper extends MapWrapper implements AddressInfoLabel, Serializable
	{
		private static final long serialVersionUID = 3290420293956206271L;

		private AddressInfoLabelWrapper(Map<String, ?> m)
		{
			super(m);
		}

		@Override
		public String name()
		{
			return mapStr("name");
		}

		@Override
		public String purpose()
		{
			return mapStr("purpose");
		}
	}

	private class BatchParam {
      public final String id;
      public final Object[] params;

      BatchParam(String id, Object[] params) {
        this.id=id;
        this.params=params;
      }
    }


	@SuppressWarnings("serial")
	private class UnspentTxOutputWrapper extends MapWrapper implements UnspentTxOutput, Serializable {

	  private UnspentTxOutputWrapper(Map<String, ?> m) {
	    super(m);
	  }

	  @Override
	  public String txid() {
	    return mapStr("txid");
	  }

	  @Override
	  public Integer vout() {
	    return mapInt("vout");
	  }

	  @Override
	  public String scriptPubKey() {
	    return mapStr("scriptPubKey");
	  }

	  @Override
	  public BigDecimal amount() {
	    return mapBigDecimal("amount");
	  }

	  @Override
	  public int height() {
	    return mapInt("height");
	  }

	  @Override
	  public String desc() {
	    return mapStr("desc");
	  }

	}

	@SuppressWarnings("serial")
	private class UtxoSetWrapper extends MapWrapper implements UtxoSet, Serializable {

	  private UtxoSetWrapper(Map<String, ?> m) {
	    super(m);
	  }


	  @Override
	  public Integer searchedItems() {
	    return mapInt("searched_items");
	  }

	  @Override
	  public BigDecimal totalAmount() {
	    return mapBigDecimal("total_amount");
	  }

	  @SuppressWarnings("unchecked")
	  @Override
	  public List<UnspentTxOutput> unspents() {
	    List<Map<String, ?>> maps = (List<Map<String, ?>>) m.get("unspents");
	    List<UnspentTxOutput> utxoList = new LinkedList<UnspentTxOutput>();
	    for (Map<String, ?> m : maps) {
	      UnspentTxOutputWrapper add = new UnspentTxOutputWrapper(m);
	      utxoList.add(add);
	    }
	    return utxoList;
	  }
	}
}

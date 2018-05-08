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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com Small modifications by
 * Alessandro Polverini polverini at gmail.com
 */
public interface BitcoindRpcClient {

  /* Missing methods:
   getblocktemplate ( "jsonrequestobject" )
   *getgenerate
   *gethashespersec
   *getwork ( "data" )
   help ( "command" )
   *listaddressgroupings
   *listlockunspent
   (DEPRECATED) listreceivedbyaccount ( minconf includeempty )
   lockunspent unlock [{"txid":"txid","vout":n},...]
   sendmany "fromaccount" {"address":amount,...} ( minconf "comment" )
   (DEPRECATED) setaccount "bitcoinaddress" "account"
   */
  public static interface TxInput extends Serializable {

    public String txid();

    public int vout();

    public String scriptPubKey();
  }

  public static class BasicTxInput implements TxInput {

    public String txid;
    public int vout;
    public String scriptPubKey;

    public BasicTxInput(String txid, int vout) {
      this.txid = txid;
      this.vout = vout;
    }

    public BasicTxInput(String txid, int vout, String scriptPubKey) {
      this(txid, vout);
      this.scriptPubKey = scriptPubKey;
    }

    @Override
    public String txid() {
      return txid;
    }

    @Override
    public int vout() {
      return vout;
    }

    @Override
    public String scriptPubKey() {
      return scriptPubKey;
    }

  }

  public static class ExtendedTxInput extends BasicTxInput {

    public String redeemScript;
    public BigDecimal amount;

    public ExtendedTxInput(String txid, int vout) {
      super(txid, vout);
    }

    public ExtendedTxInput(String txid, int vout, String scriptPubKey) {
      super(txid, vout, scriptPubKey);
    }

    public ExtendedTxInput(String txid, int vout, String scriptPubKey, String redeemScript, BigDecimal amount) {
      super(txid, vout, scriptPubKey);
      this.redeemScript = redeemScript;
      this.amount = amount;
    }

    public String redeemScript() {
      return redeemScript;
    }

    public BigDecimal amount() {
      return amount;
    }

  }

  public static interface TxOutput extends Serializable {

    public String address();

    public double amount();
  }

  public static class BasicTxOutput implements TxOutput {

    public String address;
    public double amount;

    public BasicTxOutput(String address, double amount) {
      this.address = address;
      this.amount = amount;
    }

    @Override
    public String address() {
      return address;
    }

    @Override
    public double amount() {
      return amount;
    }
  }

  /*
   * Use BitcoinRawTxBuilder , which is more convenient
   *
   */
  public String createRawTransaction(List<TxInput> inputs, List<TxOutput> outputs) throws GenericRpcException;

  public String dumpPrivKey(String address) throws GenericRpcException;

  public String getAccount(String address) throws GenericRpcException;

  public String getAccountAddress(String address) throws GenericRpcException;

  public List<String> getAddressesByAccount(String account) throws GenericRpcException;

  /**
   * @return returns the server's total available balance
   * @throws GenericRpcException
   */
  public double getBalance() throws GenericRpcException;

  /**
   * @param account
   * @return returns the balance in the account
   * @throws GenericRpcException
   */
  public double getBalance(String account) throws GenericRpcException;

  /**
   * @param account
   * @param minConf
   * @return returns the balance in the account
   * @throws GenericRpcException
   */
  public double getBalance(String account, int minConf) throws GenericRpcException;

  /**
   * @return infos about the bitcoind instance
   * @throws GenericRpcException
   */
  public Info getInfo() throws GenericRpcException;

  /**
   *
   * @return miningInfo about the bitcoind instance
   * @throws GenericRpcException
   */
  public MiningInfo getMiningInfo() throws GenericRpcException;

  public MultiSig createMultiSig(int nRequired, List<String> keys) throws GenericRpcException;

  public NetworkInfo getNetworkInfo() throws GenericRpcException;

  public static interface Info extends Serializable {

    public long version();

    public long protocolVersion();

    public long walletVersion();

    public double balance();

    public int blocks();

    public int timeOffset();

    public int connections();

    public String proxy();

    public double difficulty();

    public boolean testnet();

    public long keyPoolOldest();

    public long keyPoolSize();

    public double payTxFee();

    public double relayFee();

    public String errors();
  }

  public static interface MiningInfo extends Serializable {

    public int blocks();

    public int currentBlockSize();

    public int currentBlockWeight();

    public int currentBlockTx();

    public double difficulty();

    public String errors();

    public double networkHashps();

    public int pooledTx();

    public boolean testNet();

    public String chain();
  }

  public static interface NetTotals extends Serializable {

    public long totalBytesRecv();

    public long totalBytesSent();

    public long timeMillis();

    public interface uploadTarget extends Serializable {

      public long timeFrame();

      public int target();

      public boolean targetReached();

      public boolean serveHistoricalBlocks();

      public long bytesLeftInCycle();

      public long timeLeftInCycle();
    }

    public uploadTarget uploadTarget();
  }

  public static interface BlockChainInfo extends Serializable {

    public String chain();

    public int blocks();

    public String bestBlockHash();

    public double difficulty();

    public double verificationProgress();

    public String chainWork();
  }

  public static interface DecodedScript extends Serializable {

    public String asm();

    public String hex();

    public String type();

    public int reqSigs();

    public List<String> addresses();

    public String p2sh();
  }

  public TxOutSetInfo getTxOutSetInfo();

  public WalletInfo getWalletInfo();

  public static interface WalletInfo extends Serializable {

    public long walletVersion();

    public BigDecimal balance();

    public BigDecimal unconfirmedBalance();

    public BigDecimal immatureBalance();

    public long txCount();

    public long keyPoolOldest();

    public long keyPoolSize();

    public long unlockedUntil();

    public BigDecimal payTxFee();

    public String hdMasterKeyId();
  }

  public static interface NetworkInfo extends Serializable {

    public long version();

    public String subversion();

    public long protocolVersion();

    public String localServices();

    public boolean localRelay();

    public long timeOffset();

    public long connections();

    public List<Network> networks();

    public BigDecimal relayFee();

    public List<String> localAddresses();

    public String warnings();
  }

  public static interface Network extends Serializable {

    public String name();

    public boolean limited();

    public boolean reachable();

    public String proxy();

    public boolean proxyRandomizeCredentials();
  }

  public static interface MultiSig extends Serializable {

    public String address();

    public String redeemScript();
  }

  public static interface NodeInfo extends Serializable {

    public String addedNode();

    public boolean connected();

    public List<Address> addresses();

  }

  public static interface Address extends Serializable {

    public String address();

    public String connected();
  }

  public static interface TxOut extends Serializable {

    public String bestBlock();

    public long confirmations();

    public BigDecimal value();

    public String asm();

    public String hex();

    public long reqSigs();

    public String type();

    public List<String> addresses();

    public long version();

    public boolean coinBase();

  }

  public static interface Block extends Serializable {

    public String hash();

    public int confirmations();

    public int size();

    public int height();

    public int version();

    public String merkleRoot();

    public List<String> tx();

    public Date time();

    public long nonce();

    public String bits();

    public double difficulty();

    public String previousHash();

    public String nextHash();

    public String chainwork();

    public Block previous() throws GenericRpcException;

    public Block next() throws GenericRpcException;
  }

  public static interface TxOutSetInfo extends Serializable {

    public long height();

    public String bestBlock();

    public long transactions();

    public long txouts();

    public long bytesSerialized();

    public String hashSerialized();

    public BigDecimal totalAmount();
  }

  public Block getBlock(int height) throws GenericRpcException;

  public Block getBlock(String blockHash) throws GenericRpcException;

  public String getRawBlock(String blockHash) throws GenericRpcException;

  public String getBlockHash(int height) throws GenericRpcException;

  public BlockChainInfo getBlockChainInfo() throws GenericRpcException;

  public int getBlockCount() throws GenericRpcException;

  public String getNewAddress() throws GenericRpcException;

  public String getNewAddress(String account) throws GenericRpcException;

  public List<String> getRawMemPool() throws GenericRpcException;

  public String getBestBlockHash() throws GenericRpcException;

  public String getRawTransactionHex(String txId) throws GenericRpcException;

  public interface RawTransaction extends Serializable {

    public String hex();

    public String txId();

    public int version();

    public long lockTime();

    public long size();

    public long vsize();

    public String hash();

    /*
     *
     */
    public interface In extends TxInput, Serializable {

      public Map<String, Object> scriptSig();

      public long sequence();

      public RawTransaction getTransaction();

      public Out getTransactionOutput();
    }

    /**
     * This method should be replaced someday
     *
     * @return the list of inputs
     */
    public List<In> vIn(); // TODO : Create special interface instead of this

    public interface Out extends Serializable {

      public double value();

      public int n();

      public interface ScriptPubKey extends Serializable {

        public String asm();

        public String hex();

        public int reqSigs();

        public String type();

        public List<String> addresses();
      }

      public ScriptPubKey scriptPubKey();

      public TxInput toInput();

      public RawTransaction transaction();
    }

    /**
     * This method should be replaced someday
     */
    public List<Out> vOut(); // TODO : Create special interface instead of this

    public String blockHash();

    public int confirmations();

    public Date time();

    public Date blocktime();
  }

  public RawTransaction getRawTransaction(String txId) throws GenericRpcException;

  public double getReceivedByAddress(String address) throws GenericRpcException;

  /**
   * Returns the total amount received by &lt;bitcoinaddress&gt; in transactions
   * with at least [minconf] confirmations. While some might consider this
   * obvious, value reported by this only considers *receiving* transactions. It
   * does not check payments that have been made *from* this address. In other
   * words, this is not "getaddressbalance". Works only for addresses in the
   * local wallet, external addresses will always show 0.
   *
   * @param address
   * @param minConf
   * @return the total amount received by &lt;bitcoinaddress&gt;
   */
  public double getReceivedByAddress(String address, int minConf) throws GenericRpcException;

  public void importPrivKey(String bitcoinPrivKey) throws GenericRpcException;

  public void importPrivKey(String bitcoinPrivKey, String label) throws GenericRpcException;

  public void importPrivKey(String bitcoinPrivKey, String label, boolean rescan) throws GenericRpcException;

  Object importAddress(String address, String label, boolean rescan) throws GenericRpcException;

  /**
   * listaccounts [minconf=1]
   *
   * @return Map that has account names as keys, account balances as values
   * @throws GenericRpcException
   */
  public Map<String, Number> listAccounts() throws GenericRpcException;

  public Map<String, Number> listAccounts(int minConf) throws GenericRpcException;

  public static interface ReceivedAddress extends Serializable {

    public String address();

    public String account();

    public double amount();

    public int confirmations();
  }

  public List<ReceivedAddress> listReceivedByAddress() throws GenericRpcException;

  public List<ReceivedAddress> listReceivedByAddress(int minConf) throws GenericRpcException;

  public List<ReceivedAddress> listReceivedByAddress(int minConf, boolean includeEmpty) throws GenericRpcException;

  /**
   * returned by listsinceblock and listtransactions
   */
  public static interface Transaction extends Serializable {

    public String account();

    public String address();

    public String category();

    public double amount();

    public double fee();

    public int confirmations();

    public String blockHash();

    public int blockIndex();

    public Date blockTime();

    public String txId();

    public Date time();

    public Date timeReceived();

    public String comment();

    public String commentTo();

    public RawTransaction raw();
  }

  public static interface TransactionsSinceBlock extends Serializable {

    public List<Transaction> transactions();

    public String lastBlock();
  }

  public TransactionsSinceBlock listSinceBlock() throws GenericRpcException;

  public TransactionsSinceBlock listSinceBlock(String blockHash) throws GenericRpcException;

  public TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations) throws GenericRpcException;

  public List<Transaction> listTransactions() throws GenericRpcException;

  public List<Transaction> listTransactions(String account) throws GenericRpcException;

  public List<Transaction> listTransactions(String account, int count) throws GenericRpcException;

  public List<Transaction> listTransactions(String account, int count, int from) throws GenericRpcException;

  public interface Unspent extends TxInput, TxOutput, Serializable {

    @Override
    public String txid();

    @Override
    public int vout();

    @Override
    public String address();

    public String account();

    public String scriptPubKey();

    @Override
    public double amount();

    public int confirmations();
  }

  public List<Unspent> listUnspent() throws GenericRpcException;

  public List<Unspent> listUnspent(int minConf) throws GenericRpcException;

  public List<Unspent> listUnspent(int minConf, int maxConf) throws GenericRpcException;

  public List<Unspent> listUnspent(int minConf, int maxConf, String... addresses) throws GenericRpcException;

  public boolean move(String fromAccount, String toBitcoinAddress, double amount) throws GenericRpcException;

  public boolean move(String fromAccount, String toBitcoinAddress, double amount, int minConf) throws GenericRpcException;

  public boolean move(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment) throws GenericRpcException;

  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount) throws GenericRpcException;

  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf) throws GenericRpcException;

  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment) throws GenericRpcException;

  /**
   * Will send the given amount to the given address, ensuring the account has a
   * valid balance using minConf confirmations.
   *
   * @param fromAccount
   * @param toBitcoinAddress
   * @param amount is a real and is rounded to 8 decimal places
   * @param minConf
   * @param comment
   * @param commentTo
   * @return the transaction ID if successful
   * @throws GenericRpcException
   */
  public String sendFrom(String fromAccount, String toBitcoinAddress, double amount, int minConf, String comment, String commentTo) throws GenericRpcException;

  public String sendRawTransaction(String hex) throws GenericRpcException;

  public String sendToAddress(String toAddress, double amount) throws GenericRpcException;

  public String sendToAddress(String toAddress, double amount, String comment) throws GenericRpcException;

  /**
   * @param toAddress
   * @param amount is a real and is rounded to 8 decimal places
   * @param comment
   * @param commentTo
   * @return the transaction ID &lt;txid&gt; if successful
   * @throws GenericRpcException
   */
  public String sendToAddress(String toAddress, double amount, String comment, String commentTo) throws GenericRpcException;

  public String signRawTransaction(String hex, List<ExtendedTxInput> inputs, List<String> privateKeys) throws GenericRpcException;

  public static interface AddressValidationResult extends Serializable {

    public boolean isValid();

    public String address();

    public boolean isMine();

    public boolean isScript();

    public String pubKey();

    public boolean isCompressed();

    public String account();
  }

  /**
   * @param doGenerate a boolean indicating if blocks must be generated with the
   * cpu
   * @throws BitcoinRPCException
   */
  public void setGenerate(boolean doGenerate) throws BitcoinRPCException;

  /**
   * Used in regtest mode to generate an arbitrary number of blocks
   *
   * @param numBlocks a boolean indicating if blocks must be generated with the
   * cpu
   * @return the list of hashes of the generated blocks
   * @throws BitcoinRPCException
   */
  public List<String> generate(int numBlocks) throws BitcoinRPCException;

  public List<String> generate(int numBlocks, long maxTries) throws BitcoinRPCException;

  public AddressValidationResult validateAddress(String address) throws GenericRpcException;

  public double getEstimateFee(int nBlocks) throws GenericRpcException;

  public double getEstimatePriority(int nBlocks) throws GenericRpcException;

  /**
   * In regtest mode, invalidates a block to create an orphan chain
   *
   * @param hash
   * @throws GenericRpcException
   */
  public void invalidateBlock(String hash) throws GenericRpcException;

  /**
   * In regtest mode, undo the invalidation of a block, possibly making it on
   * the top of the chain
   *
   * @param hash
   * @throws GenericRpcException
   */
  public void reconsiderBlock(String hash) throws GenericRpcException;

  public static interface PeerInfoResult extends Serializable {

    long getId();

    String getAddr();

    String getAddrLocal();

    String getServices();

    long getLastSend();

    long getLastRecv();

    long getBytesSent();

    long getBytesRecv();

    long getConnTime();

    int getTimeOffset();

    double getPingTime();

    long getVersion();

    String getSubVer();

    boolean isInbound();

    int getStartingHeight();

    long getBanScore();

    int getSyncedHeaders();

    int getSyncedBlocks();

    boolean isWhiteListed();
  }

  List<PeerInfoResult> getPeerInfo();

  void stop();

  String getRawChangeAddress();

  long getConnectionCount();

  double getUnconfirmedBalance();

  double getDifficulty();

  void ping();

  DecodedScript decodeScript(String hex);

  NetTotals getNetTotals();

  boolean getGenerate();

  double getNetworkHashPs();

  boolean setTxFee(BigDecimal amount);

  void addNode(String node, String command);

  void backupWallet(String destination);

  String signMessage(String bitcoinAdress, String message);

  void dumpWallet(String filename);

  void importWallet(String filename);

  void keyPoolRefill();

  BigDecimal getReceivedByAccount(String account);

  void encryptWallet(String passPhrase);

  void walletPassPhrase(String passPhrase, long timeOut);

  boolean verifyMessage(String bitcoinAddress, String signature, String message);

  String addMultiSigAddress(int nRequired, List<String> keyObject);

  String addMultiSigAddress(int nRequired, List<String> keyObject, String account);

  boolean verifyChain();

  List<NodeInfo> getAddedNodeInfo(boolean dummy, String node);

  void submitBlock(String hexData);

  TxOut getTxOut(String txId, long vout);

  public static interface SmartFeeResult extends Serializable {

    double feeRate();

    int blocks();
  }

  SmartFeeResult getEstimateSmartFee(int blocks);
}

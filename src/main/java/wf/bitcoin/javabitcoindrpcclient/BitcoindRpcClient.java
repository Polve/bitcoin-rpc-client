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

  /*
   * Blockchain
   * 
   * Missing methods supported in 0.17.0 are ...
   * 
   * getblockheader "hash" ( verbose )
   * getblockstats hash_or_height ( stats )
   * getchaintips
   * getchaintxstats ( nblocks blockhash )
   * getmempoolancestors txid (verbose)
   * getmempooldescendants txid (verbose)
   * getmempoolentry txid
   * getmempoolinfo
   * gettxoutproof ["txid",...] ( blockhash )
   * preciousblock "blockhash"
   * pruneblockchain
   * savemempool
   * scantxoutset <action> ( <scanobjects> )
   * verifytxoutproof "proof"
   */
  
  /**
   * The getbestblockhash RPC returns the header hash of the most recent block on the best block chain.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getbestblockhash">getbestblockhash</a>
   */
  String getBestBlockHash() throws GenericRpcException;

  /**
   * Gets a block at the given height from the local block database.
   *
   * This is a convenience method as a combination of {@link #getBlockHash(int)} and {@link #getBlock(String)}.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getblockhash">getblockhash</a>
   * @see <a href="https://bitcoin.org/en/developer-reference#getblock">getblock</a>
   */
  Block getBlock(int height) throws GenericRpcException;

  /**
   * The getblock RPC gets a block with a particular header hash from the local block database either as a JSON object or as a serialized block.
   * 
   * @param blockHash The hash of the header of the block to get, encoded as hex in RPC byte order
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getblock">getblock</a>
   */
  Block getBlock(String blockHash) throws GenericRpcException;

  /**
   * The getblock RPC gets a block with a particular header hash from the local block database as a serialized block.
   * 
   * @param blockHash The hash of the header of the block to get, encoded as hex in RPC byte order
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getblock">getblock</a>
   */
  String getRawBlock(String blockHash) throws GenericRpcException;

  /**
   * The getblockchaininfo RPC provides information about the current state of the block chain.
   * 
   * @return Information about the current state of the local block chain
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#getblockchaininfo">getblockchaininfo</a>
   */
  BlockChainInfo getBlockChainInfo() throws GenericRpcException;

  /**
   * The getblockcount RPC returns the number of blocks in the local best block chain.
   * 
   * @return The number of blocks in the local best block chain.
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#getblockcount">getblockcount</a>
   */
  int getBlockCount() throws GenericRpcException;

  /**
   * The getblockhash RPC returns the header hash of a block at the given height in the local best block chain.
   * 
   * @param height The height of the block whose header hash should be returned.
   * @return The hash of the block at the requested height, encoded as hex in RPC byte order, or JSON null if an error occurred
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#getblockhash">getblockhash</a>
   */
  String getBlockHash(int height) throws GenericRpcException;

  /**
   * The getdifficulty RPC
   * 
   * @return The difficulty of creating a block with the same target threshold (nBits) as the highest-height block in the local best block chain.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getdifficulty">getdifficulty</a>
   */
  BigDecimal getDifficulty();

  /**
   * The getrawmempool RPC returns all transaction identifiers (TXIDs) in the memory pool as a JSON array, 
   * or detailed information about each transaction in the memory pool as a JSON object.
   * 
   * @return An array of TXIDs belonging to transactions in the memory pool.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getrawmempool">getrawmempool</a>
   */
  List<String> getRawMemPool() throws GenericRpcException;

  /**
   * The gettxout RPC returns details about an unspent transaction output (UTXO).
   * 
   * @param txId The TXID of the transaction containing the output to get, encoded as hex in RPC byte order
   * @param vout The output index number (vout) of the output within the transaction
   * 
   * @return Information about the output.
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#gettxout">gettxout</a>
   */
  TxOut getTxOut(String txId, long vout);

  /**
   * The gettxoutsetinfo RPC returns statistics about the confirmed unspent transaction output (UTXO) set. 
   * Note that this call may take some time and that it only counts outputs from confirmed transactions—it does not count outputs from the memory pool.
   * 
   * @return Information about the UTXO set
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#gettxoutsetinfo">gettxoutsetinfo</a>
   */
  TxOutSetInfo getTxOutSetInfo();

  /**
   * The verifychain RPC verifies each entry in the local block chain database.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#verifychain">verifychain</a>
   * 
   * [TODO] Add parameters checkLevel, numOfBlocks
   */
  boolean verifyChain();

  /*
   * Control
   *
   * Missing methods supported in 0.17.0 are ...
   * 
   * getmemoryinfo ("mode")
   * logging ( <include> <exclude> )
   * uptime
   */
  
  /**
   * The stop RPC safely shuts down the Bitcoin Core server.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#stop">stop</a>
   */
  void stop();

  /*
   * Generating
   * 
   */
  
  	/**
	 * The generate RPC nearly instantly generates blocks.
	 *
	 * @param numBlocks The number of blocks to generate.
	 * @return An array containing the block header hashes of the generated blocks
	 * 
	 * @see <a href="https://bitcoin.org/en/developer-reference#generate">generate</a>
	 * 
	 * @deprecated The wallet generate rpc method is deprecated and will be fully
	 *             removed in v0.19. To use generate in v0.18, restart bitcoind with
	 *             -deprecatedrpc=generate. Clients should transition to using the
	 *             node rpc method generatetoaddress
	 */
  @Deprecated
  List<String> generate(int numBlocks) throws BitcoinRPCException;

  /**
   * The generate RPC nearly instantly generates blocks.
   *
   * @param numBlocks The number of blocks to generate.
   * @param maxTries The maximum number of iterations that are tried to create the requested number of blocks. 
   * @return An array containing the block header hashes of the generated blocks
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#generate">generate</a>
   * 
   * @deprecated The wallet generate rpc method is deprecated and will be fully
   * 			removed in v0.19. To use generate in v0.18, restart bitcoind with
   * 			-deprecatedrpc=generate. Clients should transition to using the
   * 			node rpc method generatetoaddress
   */
  @Deprecated
  List<String> generate(int numBlocks, long maxTries) throws BitcoinRPCException;

  /**
   * The generatetoaddress RPC mines blocks immediately to a specified address.
   * 
   * @param numBlocks The number of blocks to generate.
   * @param address The address to send the newly generated Bitcoin to
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#generatetoaddress">generatetoaddress</a>
   */
  List<String> generateToAddress(int numBlocks, String address) throws BitcoinRPCException;

  /*
   * Mining
   * 
   * Missing methods supported in 0.17.0 are ...
   * 
   * getblocktemplate ( TemplateRequest )
   * prioritisetransaction <txid> <dummy value> <fee delta>
   */
  
  /**
   * The getmininginfo RPC returns various mining-related information.
   * 
   * @return Various mining-related information
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getmininginfo">getmininginfo</a>
   */
  public MiningInfo getMiningInfo() throws GenericRpcException;

  /**
   * The getnetworkhashps RPC returns the estimated current or historical network hashes per second based on the last n blocks.
   * 
   * @return The estimated number of hashes per second based on the parameters provided.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getgenerate">getgenerate</a>
   * 
   * [TODO] Add parameters blocks, height
   */
  BigDecimal getNetworkHashPs();

  /**
   * The submitblock RPC accepts a block, verifies it is a valid addition to the block chain, and broadcasts it to the network.
   * 
   * @param hexData The full block to submit in serialized block format as hex
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#submitblock">submitblock</a>
   */
  void submitBlock(String hexData);

  /**
   * Permanently marks a block as invalid, as if it violated a consensus rule.
   *
   * @param blockHash the hash of the block to mark as invalid
   * 
   * [TODO] Add to https://bitcoin.org/en/developer-reference
   */
  void invalidateBlock(String blockHash) throws GenericRpcException;

  /**
   * Removes invalidity status of a block and its descendants, reconsider them for activation. 
   * This can be used to undo the effects of invalidateblock.
   *
   * @param blockHash The hash of the block to reconsider
   * 
   * [TODO] Add to https://bitcoin.org/en/developer-reference
   */
  void reconsiderBlock(String blockHash) throws GenericRpcException;

  /*
   * Network
   * 
   * Missing methods supported in 0.17.0 are ...
   * 
   * clearbanned
   * disconnectnode "[address]" [nodeid]
   * listbanned
   * setban "subnet" "add|remove" (bantime) (absolute)
   * setnetworkactive true|false
   */
  
  /**
   * The addnode RPC attempts to add or remove a node from the addnode list, or to try a connection to a node once.
   * 
   * @param node The node to add as a string in the form of <IP address>:<port>.
   * @param command What to do with the IP address above. 
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#addnode">addnode</a>
   */
  void addNode(String node, String command);
  
  /**
   * The getaddednodeinfo RPC returns information about the given added node, or all added nodes (except onetry nodes). 
   * Only nodes which have been manually added using the addnode RPC will have their information displayed.
   * 
   * @param details Removed in Bitcoin Core 0.14.0
   * @param node The node to get information about in the same <IP address>:<port> format as the addnode RPC.
   * @return An array containing objects describing each added node.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getaddednodeinfo">getaddednodeinfo</a>
   */
  List<NodeInfo> getAddedNodeInfo(boolean details, String node);

  /**
   * The getconnectioncount RPC returns the number of connections to other nodes.
   * 
   * @return The total number of connections to other nodes (both inbound and outbound)
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getconnectioncount">getconnectioncount</a>
   */
  long getConnectionCount();

  /**
   * The getnettotals RPC returns information about network traffic, including bytes in, bytes out, and the current time.
   * 
   * @return An object containing information about the node’s network totals
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getnettotals">getnettotals</a>
   */
  NetTotals getNetTotals();

  /**
   * The getnetworkinfo RPC returns information about the node’s connection to the network.
   * 
   * @return Information about this node’s connection to the network
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getnetworkinfo">getnetworkinfo</a>
   */
  NetworkInfo getNetworkInfo() throws GenericRpcException;

  /**
   * The getpeerinfo RPC returns data about each connected network node.
   * 
   * @return An array of objects each describing one connected node.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getpeerinfo">getpeerinfo</a>
   */
  List<PeerInfoResult> getPeerInfo();

  /**
   * The ping RPC sends a P2P ping message to all connected nodes to measure ping time. 
   * Results are provided by the getpeerinfo RPC pingtime and pingwait fields as decimal seconds. 
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#ping">ping</a>
   */
  void ping();

  /*
   * Rawtransactions
   * 
   * Missing methods supported in 0.17.0 are ...
   * 
   * combinepsbt ["psbt",...]
   * combinerawtransaction ["hexstring",...]
   * converttopsbt "hexstring" ( permitsigdata iswitness )
   * createpsbt [{"txid":"id","vout":n},...] [{"address":amount},{"data":"hex"},...] ( locktime ) ( replaceable )
   * decodepsbt "psbt"
   * decoderawtransaction "hexstring" ( iswitness )
   * finalizepsbt "psbt" ( extract )
   * fundrawtransaction "hexstring" ( options iswitness )
   * signrawtransactionwithkey "hexstring" ["privatekey1",...] ( [{"txid":"id","vout":n,"scriptPubKey":"hex","redeemScript":"hex"},...] sighashtype )
   * testmempoolaccept ["rawtxs"] ( allowhighfees )
   */
  
  /**
   * The createrawtransaction RPC creates an unsigned serialized transaction that spends a previous output to a new output with a P2PKH or P2SH address. 
   * The transaction is not stored in the wallet or transmitted to the network.
   * 
   * @param inputs An array of objects, each one to be used as an input to the transaction
   * @param outputs The addresses and amounts to pay
   * @return The resulting unsigned raw transaction in serialized transaction format encoded as hex.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#createrawtransaction">createrawtransaction</a>
   */
  String createRawTransaction(List<TxInput> inputs, List<TxOutput> outputs) throws GenericRpcException;

  /**
   * The decodescript RPC decodes a hex-encoded P2SH redeem script.
   * 
   * @param hex The redeem script to decode as a hex-encoded serialized script
   * @return An object describing the decoded script, or JSON null if the script could not be decoded
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#decodescript">decodescript</a>
   */
  DecodedScript decodeScript(String hex);

  /**
   * The getrawtransaction RPC gets a hex-encoded serialized transaction or a JSON object describing the transaction. 
   * By default, Bitcoin Core only stores complete transaction data for UTXOs and your own transactions, 
   * so the RPC may fail on historic transactions unless you use the non-default txindex=1 in your Bitcoin Core startup settings.
   * 
   * @param txId The TXID of the transaction to get, encoded as hex in RPC byte order
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getrawtransaction">getrawtransaction</a>
   */
  RawTransaction getRawTransaction(String txId) throws GenericRpcException;

  /**
   * The getrawtransaction RPC gets a hex-encoded serialized transaction.
   * 
   * @param txId The TXID of the transaction to get, encoded as hex in RPC byte order
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getrawtransaction">getrawtransaction</a>
   */
  String getRawTransactionHex(String txId) throws GenericRpcException;

  /**
   * The sendrawtransaction RPC validates a transaction and broadcasts it to the peer-to-peer network.
   * 
   * @param hex The serialized transaction to broadcast encoded as hex
   * 
   * @return If the transaction was accepted by the node for broadcast, this will be the TXID of the transaction encoded as hex in RPC byte order.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#sendrawtransaction">sendrawtransaction</a>
   */
  String sendRawTransaction(String hex) throws GenericRpcException;

  /**
   * The signrawtransaction RPC signs a transaction in the serialized transaction format using private keys stored in the wallet or provided in the call.
   * 
   * @param hex The transaction to sign as a serialized transaction
   * @param inputs The previous outputs being spent by this transaction
   * @param privateKeys An array holding private keys.
   * 
   * @return The results of the signature
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#signrawtransaction">signrawtransaction</a>
   * 
   * @deprecated signrawtransaction was removed in v0.18. Clients should transition to using signrawtransactionwithkey and signrawtransactionwithwallet
   */
  @Deprecated
  String signRawTransaction(String hex, List<? extends TxInput> inputs, List<String> privateKeys) throws GenericRpcException;
  
  /**
   * The signrawtransactionwithkey RPC sign inputs for raw transaction (serialized, hex-encoded).
   * 
   * @param hex The transaction hex string
   * @param privateKeys List of base58-encoded private keys for signing
   * @param	prevTxs List of previous transaction outputs that this transaction depends on but may not yet be in the block chain (optional)
   * @param sigHashType The signature hash type (optional, default = ALL)
   * 
   * @return The results of the signature
   * 
   * @see <a href="https://bitcoincore.org/en/doc/0.18.0/rpc/rawtransactions/signrawtransactionwithkey/">Bitcoin Core Documentation for signrawtransactionwithkey</a>
   */
  SignedRawTransaction signRawTransactionWithKey(String hex, List<String> privateKeys, List<? extends TxInput> prevTxs, SignatureHashType sigHashType);

  /*
   * Util
   * 
   * Missing methods supported in 0.17.0 are ...
   * 
   * signmessagewithprivkey "privkey" "message"
   */
  
  /**
   * The createmultisig RPC creates a P2SH multi-signature address.
   * 
   * @param nRequired The minimum (m) number of signatures required to spend this m-of-n multisig script
   * @param keys An array of strings with each string being a public key or address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#createmultisig">createmultisig</a>
   */
  MultiSig createMultiSig(int nRequired, List<String> keys) throws GenericRpcException;

  /**
   * Estimates the approximate fee per kilobyte needed for a transaction to begin confirmation within conf_target blocks if possible 
   * and return the number of blocks for which the estimate is valid. Uses virtual transaction size as defined in BIP 141 (witness data is discounted).
   * 
   * @param blocks Confirmation target in blocks
   * @return estimate fee rate in BTC/kB
   * 
   * [TODO] Add to https://bitcoin.org/en/developer-reference
   */
  SmartFeeResult estimateSmartFee(int blocks);
  
  /**
   * The validateaddress RPC returns information about the given Bitcoin address.
   * 
   * @param address The P2PKH or P2SH address to validate encoded in base58check format
   * @return Information about the address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#validateaddress">validateaddress</a>
   */
  AddressValidationResult validateAddress(String address) throws GenericRpcException;

  /**
   * The verifymessage RPC verifies a signed message.
   * 
   * @param address The P2PKH address corresponding to the private key which made the signature. 
   * @param signature The signature created by the signer encoded as base-64 (the format output by the signmessage RPC)
   * @param message The message exactly as it was signed
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#verifymessage">verifymessage</a>
   */
  boolean verifyMessage(String address, String signature, String message);

  /*
   * Wallet
   *  
   * Missing methods supported in 0.17.0 are ...
   * 
   * abandontransaction "txid"
   * abortrescan
   * bumpfee "txid" ( options ) 
   * createwallet "wallet_name" ( disable_private_keys )
   * getaddressesbylabel "label"
   * getaddressinfo "address"
   * importmulti "requests" ( "options" )
   * importprunedfunds
   * importpubkey "pubkey" ( "label" rescan )
   * listaddressgroupings
   * listlabels ( "purpose" )
   * listwallets
   * loadwallet "filename"
   * removeprunedfunds "txid"
   * rescanblockchain ("start_height") ("stop_height")
   * sendmany "" {"address":amount,...} ( minconf "comment" ["address",...] replaceable conf_target "estimate_mode")
   * sethdseed ( "newkeypool" "seed" )
   * signrawtransactionwithwallet "hexstring" ( [{"txid":"id","vout":n,"scriptPubKey":"hex","redeemScript":"hex"},...] sighashtype )
   * unloadwallet ( "wallet_name" )
   * walletcreatefundedpsbt [{"txid":"id","vout":n},...] [{"address":amount},{"data":"hex"},...] ( locktime ) ( replaceable ) ( options bip32derivs )
   * walletlock
   * walletpassphrasechange "oldpassphrase" "newpassphrase"
   * walletprocesspsbt "psbt" ( sign "sighashtype" bip32derivs )
   */
  
  /**
   * The addmultisigaddress RPC adds a P2SH multisig address to the wallet.
   * 
   * @param nRequired The minimum (m) number of signatures required to spend this m-of-n multisig script
   * @param keyObject An array of strings with each string being a public key or address
   * @return The P2SH multisig address.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#addmultisigaddress">addmultisigaddress</a>
   */
  MultiSig addMultiSigAddress(int nRequired, List<String> keyObject);

  /**
   * The addmultisigaddress RPC adds a P2SH multisig address to the wallet.
   * 
   * @param nRequired The minimum (m) number of signatures required to spend this m-of-n multisig script
   * @param keyObject An array of strings with each string being a public key or address
   * @param account The account name in which the address should be stored.
   * 
   * @return The P2SH multisig address.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#addmultisigaddress">addmultisigaddress</a>
   */
  @Deprecated
  MultiSig addMultiSigAddress(int nRequired, List<String> keyObject, String account);

  /**
   * The backupwallet RPC safely copies wallet.dat to the specified file, which can be a directory or a path with filename.
   * 
   * @param destination A filename or directory name.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#backupwallet">backupwallet</a>
   */
  void backupWallet(String destination);

  /**
   * The dumpprivkey RPC returns the wallet-import-format (WIF) private key corresponding to an address. 
   * (But does not remove it from the wallet.)
   * 
   * @param address The P2PKH address corresponding to the private key you want returned.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#dumpprivkey">dumpprivkey</a>
   */
  public String dumpPrivKey(String address) throws GenericRpcException;

  /**
   * The dumpwallet RPC creates or overwrites a file with all wallet keys in a human-readable format.
   * 
   * @param filename The file in which the wallet dump will be placed.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#dumpwallet">dumpwallet</a>
   */
  void dumpWallet(String filename);

  /**
   * The encryptwallet RPC encrypts the wallet with a passphrase. 
   * This is only to enable encryption for the first time. 
   * After encryption is enabled, you will need to enter the passphrase to use private keys.
   * 
   * @param passPhrase The passphrase to use for the encrypted wallet. 
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#encryptwallet">encryptwallet</a>
   */
  void encryptWallet(String passPhrase);

  /**
   * The getaccount RPC returns the name of the account associated with the given address.
   * 
   * @param address A P2PKH or P2SH Bitcoin address belonging either to a specific account or the default account 
   * @return The name of an account, or an empty string
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getaccount">getaccount</a>
   */
  @Deprecated
  public String getAccount(String address) throws GenericRpcException;

  /**
   * The getaccountaddress RPC returns the current Bitcoin address for receiving payments to this account. 
   * If the account doesn’t exist, it creates both the account and a new address for receiving payment. 
   * Once a payment has been received to an address, future calls to this RPC for the same account will return a different address.
   * 
   * @param account The name of an account. 
   * @return An address, belonging to the account specified, which has not yet received any payments
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getaccountaddress">getaccountaddress</a>
   */
  @Deprecated
  public String getAccountAddress(String account) throws GenericRpcException;

  /**
   * The getaddressesbyaccount RPC returns a list of every address assigned to a particular account. 
   * 
   * @param account The name of an account to get the balance for.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getaddressesbyaccount">getaddressesbyaccount</a>
   */
  @Deprecated
  public List<String> getAddressesByAccount(String account) throws GenericRpcException;

  /**
   * The getbalance RPC gets the balance in decimal bitcoins for the default account.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getbalance">getbalance</a>
   */
  public BigDecimal getBalance() throws GenericRpcException;

  /**
   * The getbalance RPC gets the balance in decimal bitcoins across all accounts or for a particular account.
   * 
   * @param account The name of an account to get the balance for.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getbalance">getbalance</a>
   */
  public BigDecimal getBalance(String account) throws GenericRpcException;

  /**
   * The getbalance RPC gets the balance in decimal bitcoins across all accounts or for a particular account.
   * 
   * @param account The name of an account to get the balance for.
   * @param minConf The minimum number of confirmations
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getbalance">getbalance</a>
   */
  public BigDecimal getBalance(String account, int minConf) throws GenericRpcException;

  /**
   * The getnewaddress RPC returns a new Bitcoin address for receiving payments. 
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#getnewaddress">getnewaddress</a>
   */
  String getNewAddress() throws GenericRpcException;

  /**
   * The getnewaddress RPC returns a new Bitcoin address for receiving payments. 
   * If an account is specified, payments received with the address will be credited to that account.
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#getnewaddress">getnewaddress</a>
   */
  String getNewAddress(String account) throws GenericRpcException;

  /**
   * The getnewaddress RPC returns a new Bitcoin address for receiving payments. 
   * If an account is specified, payments received with the address will be credited to that account.
   * The address type to use. Options are "legacy", "p2sh-segwit", and "bech32".
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#getnewaddress">getnewaddress</a>
   */
  String getNewAddress(String account, String addressType) throws GenericRpcException;

  /**
   * The getrawchangeaddress RPC returns a new Bitcoin address for receiving change. This is for use with raw transactions, not normal use.
   * 
   * @return A P2PKH address which has not previously been returned by this RPC.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getrawchangeaddress">getrawchangeaddress</a>
   */
  String getRawChangeAddress();

  /**
   * The getreceivedbyaccount RPC returns the total amount received by addresses in a particular account from transactions with the specified number of confirmations.
   * 
   * @param account The name of the account containing the addresses to get.
   * @return The number of bitcoins received by the account.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getreceivedbyaccount">getreceivedbyaccount</a>
   */
  @Deprecated
  BigDecimal getReceivedByAccount(String account);

  /**
   * The getreceivedbyaddress RPC returns the total amount received by the specified address in transactions with the specified number of confirmations. 
   * It does not count coinbase transactions.
   *
   * @param address The address whose transactions should be tallied
   * @return The number of bitcoins received by the address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getreceivedbyaddress">getreceivedbyaddress</a>
   */
  BigDecimal getReceivedByAddress(String address) throws GenericRpcException;

  /**
   * The getreceivedbyaddress RPC returns the total amount received by the specified address in transactions with the specified number of confirmations. 
   * It does not count coinbase transactions.
   *
   * @param address The address whose transactions should be tallied
   * @param minConf The minimum number of confirmations
   * @return The number of bitcoins received by the address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getreceivedbyaddress">getreceivedbyaddress</a>
   */
  BigDecimal getReceivedByAddress(String address, int minConf) throws GenericRpcException;

  /**
   * The gettransaction RPC gets detailed information about an in-wallet transaction.
   *  
   * @param txId The TXID of the transaction to get details about.
   *  
   * @see <a href="https://bitcoin.org/en/developer-reference#gettransaction">gettransaction</a>
   */
  Transaction getTransaction(String txId);
  
  /**
   * The getunconfirmedbalance RPC returns the wallet’s total unconfirmed balance.
   * 
   * @return The total number of bitcoins paid to this wallet in unconfirmed transactions
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getunconfirmedbalance">getunconfirmedbalance</a>
   */
  BigDecimal getUnconfirmedBalance();

  /**
   * The getwalletinfo RPC provides information about the wallet.
   * 
   * @return An object describing the wallet
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#getwalletinfo">getwalletinfo</a>
   */
  WalletInfo getWalletInfo();

  /**
   * The importaddress RPC adds an address or pubkey script to the wallet without the associated private key, 
   * allowing you to watch for transactions affecting that address or pubkey script without being able to spend any of its outputs.
   * 
   * @param address Either a P2PKH or P2SH address encoded in base58check, or a pubkey script encoded as hex
   * @param account An account name into which the address should be placed. 
   * @param rescan Set to true (the default) to rescan the entire local block database
   * 
   * @return Null on success
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#importaddress">importaddress</a>
   * 
   * [TODO] Should this really return Object?
   */
  Object importAddress(String address, String account, boolean rescan) throws GenericRpcException;

  /**
   * The importprivkey RPC adds a private key to your wallet. 
   * The key should be formatted in the wallet import format created by the dumpprivkey RPC.
   * 
   * @param bitcoinPrivKey The private key to import into the wallet encoded in base58check using wallet import format (WIF)
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#importprivkey">importprivkey</a>
   */
  void importPrivKey(String bitcoinPrivKey) throws GenericRpcException;

  /**
   * The importprivkey RPC adds a private key to your wallet. 
   * The key should be formatted in the wallet import format created by the dumpprivkey RPC.
   * 
   * @param bitcoinPrivKey The private key to import into the wallet encoded in base58check using wallet import format (WIF)
   * @param account The name of an account to which transactions involving the key should be assigned.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#importprivkey">importprivkey</a>
   */
  void importPrivKey(String bitcoinPrivKey, String account) throws GenericRpcException;

  /**
   * The importprivkey RPC adds a private key to your wallet. 
   * The key should be formatted in the wallet import format created by the dumpprivkey RPC.
   * 
   * @param bitcoinPrivKey The private key to import into the wallet encoded in base58check using wallet import format (WIF)
   * @param account The name of an account to which transactions involving the key should be assigned.
   * @param rescan Set to true (the default) to rescan the entire local block database for transactions affecting any address or pubkey script in the wallet.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#importprivkey">importprivkey</a>
   */
  void importPrivKey(String bitcoinPrivKey, String account, boolean rescan) throws GenericRpcException;

  /**
   * The importwallet RPC imports private keys from a file in wallet dump file format (see the dumpwallet RPC). 
   * These keys will be added to the keys currently in the wallet.
   *  
   * @param filename The file to import. The path is relative to Bitcoin Core’s working directory
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#importwallet">importwallet</a>
   */
  void importWallet(String filename);

  /**
   * The keypoolrefill RPC fills the cache of unused pre-generated keys (the keypool).
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#keypoolrefill">keypoolrefill</a>
   */
  void keyPoolRefill();

  /**
   * The listaccounts RPC lists accounts and their balances.
   *
   * @return Map that has account names as keys, account balances as values
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listaccounts">listaccounts</a>
   */
  @Deprecated
  Map<String, Number> listAccounts() throws GenericRpcException;

  /**
   * The listaccounts RPC lists accounts and their balances.
   *
   * @param minConf The minimum number of confirmations an externally-generated transaction must have before it is counted towards the balance.
   * @return Map that has account names as keys, account balances as values
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listaccounts">listaccounts</a>
   */
  @Deprecated
  Map<String, Number> listAccounts(int minConf) throws GenericRpcException;

  /**
   * The listaccounts RPC lists accounts and their balances.
   *
   * @param minConf The minimum number of confirmations an externally-generated transaction must have before it is counted towards the balance.
   * @param watchonly Include balances in watch-only addresses.
   * @return Map that has account names as keys, account balances as values
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listaccounts">listaccounts</a>
   */
  @Deprecated
  Map<String, Number> listAccounts(int minConf, boolean watchonly) throws GenericRpcException;

  /**
   * The listlockunspent RPC returns a list of temporarily unspendable (locked) outputs.
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listlockunspent">listlockunspent</a>
   */
  List<LockedUnspent> listLockUnspent();
  
  /**
   * The listreceivedbyaddress RPC lists the total number of bitcoins received by each address.
   * 
   * @return An array containing objects each describing a particular address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listreceivedbyaddress">listreceivedbyaddress</a>
   */
  List<ReceivedAddress> listReceivedByAddress() throws GenericRpcException;

  /**
   * The listreceivedbyaddress RPC lists the total number of bitcoins received by each address.
   * 
   * @param minConf The minimum number of confirmations an externally-generated transaction must have before it is counted towards the balance.
   * 
   * @return An array containing objects each describing a particular address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listreceivedbyaddress">listreceivedbyaddress</a>
   */
  List<ReceivedAddress> listReceivedByAddress(int minConf) throws GenericRpcException;

  /**
   * The listreceivedbyaddress RPC lists the total number of bitcoins received by each address.
   * 
   * @param minConf The minimum number of confirmations an externally-generated transaction must have before it is counted towards the balance.
   * @param includeEmpty Set to true to display accounts which have never received a payment. 
   * 
   * @return An array containing objects each describing a particular address
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listreceivedbyaddress">listreceivedbyaddress</a>
   */
  List<ReceivedAddress> listReceivedByAddress(int minConf, boolean includeEmpty) throws GenericRpcException;

  /**
   * The listsinceblock RPC gets all transactions affecting the wallet which have occurred since a particular block, plus the header hash of a block at a particular depth.
   * 
   * @return An object containing an array of transactions and the lastblock field
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listsinceblock">listsinceblock</a>
   */
  TransactionsSinceBlock listSinceBlock() throws GenericRpcException;

  /**
   * The listsinceblock RPC gets all transactions affecting the wallet which have occurred since a particular block, plus the header hash of a block at a particular depth.
   * 
   * @param blockHash The hash of a block header encoded as hex in RPC byte order.
   * @return An object containing an array of transactions and the lastblock field
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listsinceblock">listsinceblock</a>
   */
  TransactionsSinceBlock listSinceBlock(String blockHash) throws GenericRpcException;

  /**
   * The listsinceblock RPC gets all transactions affecting the wallet which have occurred since a particular block, plus the header hash of a block at a particular depth.
   * 
   * @param blockHash The hash of a block header encoded as hex in RPC byte order.
   * @param targetConfirmations Sets the lastblock field of the results to the header hash of a block with this many confirmations.
   * 
   * @return An object containing an array of transactions and the lastblock field
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listsinceblock">listsinceblock</a>
   */
  TransactionsSinceBlock listSinceBlock(String blockHash, int targetConfirmations) throws GenericRpcException;

  /**
   * The listtransactions RPC returns the most recent transactions that affect the wallet.
   * 
   * @return An array containing objects, with each object describing a payment or internal accounting entry (not a transaction).
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listtransactions">listtransactions</a>
   */
  List<Transaction> listTransactions() throws GenericRpcException;

  /**
   * The listtransactions RPC returns the most recent transactions that affect the wallet.
   * 
   * @param account The name of an account to get transactinos from (deprecated).
   * 
   * @return An array containing objects, with each object describing a payment or internal accounting entry (not a transaction).
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listtransactions">listtransactions</a>
   */
  List<Transaction> listTransactions(String account) throws GenericRpcException;

  /**
   * The listtransactions RPC returns the most recent transactions that affect the wallet.
   * 
   * @param account The name of an account to get transactinos from (deprecated).
   * @param count The number of the most recent transactions to list.
   * 
   * @return An array containing objects, with each object describing a payment or internal accounting entry (not a transaction).
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listtransactions">listtransactions</a>
   */
  List<Transaction> listTransactions(String account, int count) throws GenericRpcException;

  /**
   * The listtransactions RPC returns the most recent transactions that affect the wallet.
   * 
   * @param account The name of an account to get transactinos from (deprecated).
   * @param count The number of the most recent transactions to list.
   * @param skip The number of the most recent transactions which should not be returned.
   * 
   * @return An array containing objects, with each object describing a payment or internal accounting entry (not a transaction).
   * 
   * @see <a href="https://bitcoin.org/en/developer-reference#listtransactions">listtransactions</a>
   */
  List<Transaction> listTransactions(String account, int count, int skip) throws GenericRpcException;

 /**
  * The listunspent RPC returns an array of unspent transaction outputs belonging to this wallet.
  * 
  * @return An array of objects each describing an unspent output.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#listunspent">listunspent</a>
  */
 List<Unspent> listUnspent() throws GenericRpcException;

 /**
  * The listunspent RPC returns an array of unspent transaction outputs belonging to this wallet.
  * 
  * @param minConf The minimum number of confirmations the transaction containing an output must have in order to be returned.
  * 
  * @return An array of objects each describing an unspent output.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#listunspent">listunspent</a>
  */
 List<Unspent> listUnspent(int minConf) throws GenericRpcException;

 /**
  * The listunspent RPC returns an array of unspent transaction outputs belonging to this wallet.
  * 
  * @param minConf The minimum number of confirmations the transaction containing an output must have in order to be returned.
  * @param maxConf The maximum number of confirmations the transaction containing an output may have in order to be returned.
  * 
  * @return An array of objects each describing an unspent output.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#listunspent">listunspent</a>
  */
 List<Unspent> listUnspent(int minConf, int maxConf) throws GenericRpcException;

 /**
  * The listunspent RPC returns an array of unspent transaction outputs belonging to this wallet.
  * 
  * @param minConf The minimum number of confirmations the transaction containing an output must have in order to be returned.
  * @param maxConf The maximum number of confirmations the transaction containing an output may have in order to be returned.
  * @param addresses Only outputs which pay an address in this array will be returned
  * 
  * @return An array of objects each describing an unspent output.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#listunspent">listunspent</a>
  */
 List<Unspent> listUnspent(int minConf, int maxConf, String... addresses) throws GenericRpcException;

 /**
  * The lockunspent RPC temporarily locks or unlocks specified transaction outputs. 
  * A locked transaction output will not be chosen by automatic coin selection when spending bitcoins.
  * 
  * @param unlock Set to false to lock the outputs specified in the following parameter. Set to true to unlock the outputs specified.
  * @param txid The TXID of the transaction containing the output to lock or unlock, encoded as hex.
  * @param vout The output index number (vout) of the output to lock or unlock. 
  * 
  * @return true if successful.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#lockunspent">lockunspent</a>
  */
 boolean lockUnspent(boolean unlock, String txid, int vout) throws GenericRpcException;

 /**
  * The move RPC moves a specified amount from one account in your wallet to another using an off-block-chain transaction.
  * 
  * @param fromAccount The name of the account to move the funds from
  * @param toAccount The name of the account to move the funds to
  * @param amount The amount of bitcoins to move
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#move">move</a>
  */
 @Deprecated
 boolean move(String fromAccount, String toAccount, BigDecimal amount) throws GenericRpcException;

 /**
  * The move RPC moves a specified amount from one account in your wallet to another using an off-block-chain transaction.
  * 
  * @param fromAccount The name of the account to move the funds from
  * @param toAccount The name of the account to move the funds to
  * @param amount The amount of bitcoins to move
  * @param comment A comment to assign to this move payment
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#move">move</a>
  */
 @Deprecated
 boolean move(String fromAccount, String toAccount, BigDecimal amount, String comment) throws GenericRpcException;

 /**
  * @see <a href="https://bitcoin.org/en/developer-reference#move">move</a>
  * @deprecated
  */
 boolean move(String fromAccount, String toAccount, BigDecimal amount, int minConf) throws GenericRpcException;

 /**
  * @see <a href="https://bitcoin.org/en/developer-reference#move">move</a>
  * @deprecated
  */
 boolean move(String fromAccount, String toAccount, BigDecimal amount, int minConf, String comment) throws GenericRpcException;


 /**
  * The sendfrom RPC spends an amount from a local account to a bitcoin address.
  * 
  * @param fromAccount The name of the account from which the bitcoins should be spent.
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spend in bitcoins.
  *  
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendfrom">sendfrom</a>
  */
 @Deprecated
 String sendFrom(String fromAccount, String toAddress, BigDecimal amount) throws GenericRpcException;

 /**
  * The sendfrom RPC spends an amount from a local account to a bitcoin address.
  * 
  * @param fromAccount The name of the account from which the bitcoins should be spent.
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spend in bitcoins.
  * @param minConf The minimum number of confirmations an incoming transaction must have for its outputs to be credited to this account’s balance. 
  *  
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendfrom">sendfrom</a>
  */
 @Deprecated
 String sendFrom(String fromAccount, String toAddress, BigDecimal amount, int minConf) throws GenericRpcException;

 /**
  * The sendfrom RPC spends an amount from a local account to a bitcoin address.
  * 
  * @param fromAccount The name of the account from which the bitcoins should be spent.
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spend in bitcoins.
  * @param minConf The minimum number of confirmations an incoming transaction must have for its outputs to be credited to this account’s balance. 
  * @param comment A locally-stored (not broadcast) comment assigned to this transaction.
  *  
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendfrom">sendfrom</a>
  */
 @Deprecated
 String sendFrom(String fromAccount, String toAddress, BigDecimal amount, int minConf, String comment) throws GenericRpcException;

 /**
  * The sendfrom RPC spends an amount from a local account to a bitcoin address.
  * 
  * @param fromAccount The name of the account from which the bitcoins should be spent.
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spend in bitcoins.
  * @param minConf The minimum number of confirmations an incoming transaction must have for its outputs to be credited to this account’s balance. 
  * @param comment A locally-stored (not broadcast) comment assigned to this transaction.
  * @param commentTo A locally-stored (not broadcast) comment assigned to this transaction
  *  
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendfrom">sendfrom</a>
  */
 @Deprecated
 String sendFrom(String fromAccount, String toAddress, BigDecimal amount, int minConf, String comment, String commentTo) throws GenericRpcException;

 /**
  * The sendtoaddress RPC spends an amount to a given address.
  * 
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spent in bitcoins
  * 
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendtoaddress">sendtoaddress</a>
  */
 String sendToAddress(String toAddress, BigDecimal amount) throws GenericRpcException;

 /**
  * The sendtoaddress RPC spends an amount to a given address.
  * 
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spent in bitcoins
  * @param comment A locally-stored (not broadcast) comment assigned to this transaction.
  * 
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendtoaddress">sendtoaddress</a>
  */
 String sendToAddress(String toAddress, BigDecimal amount, String comment) throws GenericRpcException;

 /**
  * The sendtoaddress RPC spends an amount to a given address.
  * 
  * @param toAddress A P2PKH or P2SH address to which the bitcoins should be sent
  * @param amount The amount to spent in bitcoins
  * @param comment A locally-stored (not broadcast) comment assigned to this transaction.
  * @param commentTo A locally-stored (not broadcast) comment assigned to this transaction
  * 
  * @return The TXID of the sent transaction, encoded as hex in RPC byte order
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#sendtoaddress">sendtoaddress</a>
  */
 String sendToAddress(String toAddress, BigDecimal amount, String comment, String commentTo) throws GenericRpcException;

 /**
  * The settxfee RPC sets the transaction fee per kilobyte paid by transactions created by this wallet.
  * 
  * @param amount The transaction fee to pay, in bitcoins, for each kilobyte of transaction data.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#settxfee">settxfee</a>
  */
 boolean setTxFee(BigDecimal amount);

 /**
  * The signmessage RPC signs a message with the private key of an address.
  * 
  * @param adress A P2PKH address whose private key belongs to this wallet
  * @param message The message to sign
  * 
  * @return The signature of the message, encoded in base64.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#signmessage">signmessage</a>
  */
 String signMessage(String adress, String message);

 /**
  * The walletpassphrase RPC stores the wallet decryption key in memory for the indicated number of seconds. 
  * Issuing the walletpassphrase command while the wallet is already unlocked will set a new unlock time that overrides the old one.
  * 
  * @param passPhrase The passphrase that unlocks the wallet
  * @param timeOut The number of seconds after which the decryption key will be automatically deleted from memory
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#walletpassphrase">walletpassphrase</a>
  */
 void walletPassPhrase(String passPhrase, long timeOut);

 /*
  * Zmq
  * 
  * getzmqnotifications 
  */

 /***************************************************************************************************************************/
 
 /**
  * The estimatefee RPC estimates the transaction fee per kilobyte that needs to be paid for a transaction to be included within a certain number of blocks.
  * 
  * @param nBlocks The maximum number of blocks a transaction should have to wait before it is predicted to be included in a block.
  * @return The estimated fee the transaction should pay in order to be included within the specified number of blocks.
  * 
  * @see <a href="https://bitcoin.org/en/developer-reference#estimatefee">estimatefee</a>
  */
 @Deprecated
 BigDecimal estimateFee(int nBlocks) throws GenericRpcException;

 /***************************************************************************************************************************/
  
 static interface Address extends MapWrapperType, Serializable {

   String address();

   String connected();
 }

 /**
  * the result returned by
  * {@link BitcoinJSONRPCClient#getAddressBalance(String)}
  * 
  * @author frankchen
  */
 static interface AddressBalance
 {
     long getBalance();
     long getReceived();
 }

 /**
  * the result return by {@link BitcoinJSONRPCClient#getAddressUtxo(String)}
  * @author frankchen
  */
 static interface AddressUtxo
 {
     String getAddress();
     String getTxid();
     int getOutputIndex();
     String getScript();
     long getSatoshis();
     long getHeight();
 }
 
 static interface AddressValidationResult extends MapWrapperType, Serializable {

   boolean isValid();

   String address();

   boolean isMine();

   boolean isScript();

   String pubKey();

   boolean isCompressed();

   String account();
 }

 @SuppressWarnings("serial")
 public static class BasicTxInput implements TxInput {

   public String txid;
   public Integer vout;
   public String scriptPubKey;
   public BigDecimal amount;

   public BasicTxInput(String txid, Integer vout) {
     this.txid = txid;
     this.vout = vout;
   }

   public BasicTxInput(String txid, Integer vout, String scriptPubKey) {
     this(txid, vout);
     this.scriptPubKey = scriptPubKey;
   }
   
   public BasicTxInput(String txid, Integer vout, String scriptPubKey, BigDecimal amount) {
     this(txid, vout, scriptPubKey);
     this.amount = amount;
   }

   @Override
   public String txid() {
     return txid;
   }

   @Override
   public Integer vout() {
     return vout;
   }

   @Override
   public String scriptPubKey() {
     return scriptPubKey;
   }

	@Override
	public BigDecimal amount() {
		return amount;
	}
 }

 @SuppressWarnings("serial")
 public static class BasicTxOutput implements TxOutput {

   public final String address;
   public final BigDecimal amount;
   public final byte[] data;

   public BasicTxOutput(String address, BigDecimal amount) {
     this(address, amount, null);
   }

   public BasicTxOutput(String address, BigDecimal amount, byte[] data) {
     this.address = address;
     this.amount = amount;
     this.data = data;
   }

   @Override
   public String address() {
     return address;
   }

   @Override
   public BigDecimal amount() {
     return amount;
   }

   @Override
   public byte[] data() {
     return data;
   }
 }

 static interface Block extends MapWrapperType, Serializable {

   String hash();

   int confirmations();

   int size();

   int height();

   int version();

   String merkleRoot();

   List<String> tx();

   Date time();

   long nonce();

   String bits();

   BigDecimal difficulty();

   String previousHash();

   String nextHash();

   String chainwork();

   Block previous() throws GenericRpcException;

   Block next() throws GenericRpcException;
 }

 static interface BlockChainInfo extends MapWrapperType, Serializable {

   String chain();

   int blocks();

   String bestBlockHash();

   BigDecimal difficulty();

   BigDecimal verificationProgress();

   String chainWork();
 }

 static interface DecodedScript extends MapWrapperType, Serializable {

   String asm();

   String hex();

   String type();

   int reqSigs();

   List<String> addresses();

   String p2sh();
 }

 	/**
	 * In addition to {@link BasicTxInput}, this also includes a
	 * {@link #redeemScript} and {@link #witnessScript}.
	 * <br>
	 * <br>
	 * 
	 * With the addition of these fields, the {@link ExtendedTxInput} can represent
	 * inputs for for P2SH, P2SH-P2WPKH, P2SH-P2WSH
	 * 
	 * @see <a href=
	 *      "https://bitcoincore.org/en/segwit_wallet_dev/#creation-of-p2sh-p2wsh-address">Bitcoin
	 *      Core documentation on P2SH and P2WSH addresses</a>
	 * @see <a href=
	 *      "https://bitcoincore.org/en/doc/0.18.0/rpc/rawtransactions/signrawtransactionwithkey/">Bitcoin
	 *      Core RPC documentation of signrawtransactionwithkey</a>
	 *      , where the
	 *      different scenarios for the extra fields of txIns (prevtxs) are
	 *      specified
	 */
 @SuppressWarnings("serial")
 public static class ExtendedTxInput extends BasicTxInput {

   private String redeemScript;
   private String witnessScript;

   public ExtendedTxInput(String txid, int vout, String scriptPubKey, BigDecimal amount, String redeemScript, String witnessScript) {
     super(txid, vout, scriptPubKey, amount);
     this.redeemScript = redeemScript;
     this.witnessScript = witnessScript;
   }

   public String redeemScript() {
     return redeemScript;
   }

   public String witnessScript() {
     return witnessScript;
   }

 }

  static interface LockedUnspent extends MapWrapperType, Serializable {
    
    String txId();
    
    int vout();
  }
  
  static interface MiningInfo extends MapWrapperType, Serializable {

    int blocks();

    int currentBlockSize();

    int currentBlockWeight();

    int currentBlockTx();

    BigDecimal difficulty();

    String errors();

    BigDecimal networkHashps();

    int pooledTx();

    boolean testNet();

    String chain();
  }

  static interface MultiSig extends MapWrapperType, Serializable {

    String address();

    String redeemScript();
  }

  static interface NetTotals extends MapWrapperType, Serializable {

    long totalBytesRecv();

    long totalBytesSent();

    long timeMillis();

    interface uploadTarget extends MapWrapperType, Serializable {

      long timeFrame();

      int target();

      boolean targetReached();

      boolean serveHistoricalBlocks();

      long bytesLeftInCycle();

      long timeLeftInCycle();
    }

    uploadTarget uploadTarget();
  }

  static interface NetworkInfo extends MapWrapperType, Serializable {

    long version();

    String subversion();

    long protocolVersion();

    String localServices();

    boolean localRelay();

    long timeOffset();

    long connections();

    List<Network> networks();

    BigDecimal relayFee();

    List<String> localAddresses();

    String warnings();
  }

  static interface Network extends MapWrapperType, Serializable {

    String name();

    boolean limited();

    boolean reachable();

    String proxy();

    boolean proxyRandomizeCredentials();
  }

  static interface NodeInfo extends MapWrapperType, Serializable {

    String addedNode();

    boolean connected();

    List<Address> addresses();

  }

  static interface PeerInfoResult extends MapWrapperType, Serializable {

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

    BigDecimal getPingTime();

    long getVersion();

    String getSubVer();

    boolean isInbound();

    int getStartingHeight();

    long getBanScore();

    int getSyncedHeaders();

    int getSyncedBlocks();

    boolean isWhiteListed();
  }

  interface RawTransaction extends MapWrapperType, Serializable {

    String hex();

    String txId();

    int version();

    long lockTime();

    long size();

    long vsize();

    String hash();

    /**
     * This method should be replaced someday
     *
     * @return the list of inputs
     */
    List<In> vIn(); 

    /**
     * This method should be replaced someday
     */
    List<Out> vOut(); // TODO : Create special interface instead of this

    String blockHash();

    /**
     * @return null if this tx has not been confirmed yet
     */
    Integer confirmations();

    /**
     * 
     * @return null if this tx has not been confirmed yet
     */
    Date time();

    /**
     * 
     * @return null if this tx has not been confirmed yet
     */
    Date blocktime();

    interface In extends TxInput, Serializable {

      Map<String, Object> scriptSig();

      long sequence();

      RawTransaction getTransaction();

      Out getTransactionOutput();
    }

    interface Out extends MapWrapperType, Serializable {

      BigDecimal value();

      int n();

      ScriptPubKey scriptPubKey();

      TxInput toInput();

      RawTransaction transaction();

      interface ScriptPubKey extends MapWrapperType, Serializable {

        String asm();

        String hex();

        int reqSigs();

        String type();

        List<String> addresses();
      }
    }
  }

  static interface ReceivedAddress extends MapWrapperType, Serializable {

    String address();

    String account();

    BigDecimal amount();

    int confirmations();
  }

  static interface SmartFeeResult extends MapWrapperType, Serializable {
    
    int blocks();

    BigDecimal feeRate();

    String errors();
  }

  static interface Transaction extends MapWrapperType, Serializable {

    String account();

    String address();

    String category();

    BigDecimal amount();

    BigDecimal fee();

    int confirmations();

    String blockHash();

    int blockIndex();

    Date blockTime();

    String txId();

    Date time();

    Date timeReceived();

    String comment();

    String commentTo();

    boolean generated();
    
    RawTransaction raw();
  }

  static interface TransactionsSinceBlock extends Serializable {

    List<Transaction> transactions();

    String lastBlock();
  }

  public static interface TxInput extends Serializable {

    public String txid();

    public Integer vout();

    public String scriptPubKey();
    
    public BigDecimal amount();
  }

  static interface TxOut extends MapWrapperType, Serializable {

    String bestBlock();

    long confirmations();

    BigDecimal value();

    String asm();

    String hex();

    long reqSigs();

    String type();

    List<String> addresses();

    long version();

    boolean coinBase();

  }

  static interface TxOutSetInfo extends MapWrapperType, Serializable {

    long height();

    String bestBlock();

    long transactions();

    long txouts();

    long bytesSerialized();

    String hashSerialized();

    BigDecimal totalAmount();
  }

  public static interface TxOutput extends Serializable {

    public String address();

    public BigDecimal amount();
    
    public byte[] data();
  }

  /**
   * @see <a href="https://bitcoin.org/en/developer-reference#listunspent">Bitcoin Core API documentation</a>
   */
  interface Unspent extends TxInput, TxOutput, Serializable {

	@Deprecated
    String account();

    int confirmations();
    
    /**
     * @return The redeemScript if scriptPubKey is P2SH
     */
    String redeemScript();
    
    /**
     * @return witnessScript, if the scriptPubKey is P2WSH or P2SH-P2WSH
     */
    String witnessScript();
  }

  static interface WalletInfo extends MapWrapperType, Serializable {

    long walletVersion();

    BigDecimal balance();

    BigDecimal unconfirmedBalance();

    BigDecimal immatureBalance();

    long txCount();

    long keyPoolOldest();

    long keyPoolSize();

    long unlockedUntil();

    BigDecimal payTxFee();

    String hdMasterKeyId();
  }
  
  /**
   * See return structure of <a href="https://bitcoincore.org/en/doc/0.18.0/rpc/rawtransactions/signrawtransactionwithkey/">signrawtransactionwithkey</a>
   */
  static interface SignedRawTransaction
  {
	  /**
	   * @return The hex-encoded raw transaction with signature(s)
	   */
	  String hex();
	  
	  /**
	   * @return If the transaction has a complete set of signatures
	   */
	  boolean complete();
	  
	  /**
	   * @return Script verification errors (if there are any)
	   */
	  List<RawTransactionSigningOrVerificationError> errors();
  }
  
  /**
   * See error array in return structure of <a href="https://bitcoincore.org/en/doc/0.18.0/rpc/rawtransactions/signrawtransactionwithkey/">signrawtransactionwithkey</a>
   */
  static interface RawTransactionSigningOrVerificationError
  {
	  /**
	   * @return The hash of the referenced, previous transaction
	   */
	  String txId();
	  
	  /**
	   * @return The index of the output to be spent and used as input
	   */
	  int vOut();
	  
	  /**
	   * @return The hex-encoded signature script
	   */
	  String scriptSig();
	  
	  /**
	   * @return Script sequence number
	   */
	  int n();
	  
	  /**
	   * @return Verification or signing error related to the input
	   */
	  String error();
  }
}

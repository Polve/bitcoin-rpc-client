/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wf.bitcoin.javabitcoindrpcclient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author azazar
 */
public class BitcoinRawTxBuilder {

  public final BitcoindRpcClient bitcoin;

  public BitcoinRawTxBuilder(BitcoindRpcClient bitcoin) {
    this.bitcoin = bitcoin;
  }
  public Set<BitcoindRpcClient.TxInput> inputs = new LinkedHashSet<>();
  public List<BitcoindRpcClient.TxOutput> outputs = new ArrayList<>();
  public List<String> privateKeys;

  @SuppressWarnings("serial")
  private class Input extends BitcoindRpcClient.BasicTxInput {

    public Input(String txid, Integer vout) {
      super(txid, vout);
    }

    public Input(BitcoindRpcClient.TxInput copy) {
      this(copy.txid(), copy.vout());
    }

    @Override
    public int hashCode() {
      return txid.hashCode() + vout;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (!(obj instanceof BitcoindRpcClient.TxInput))
        return false;
      BitcoindRpcClient.TxInput other = (BitcoindRpcClient.TxInput) obj;
      return vout == other.vout() && txid.equals(other.txid());
    }

  }

  public BitcoinRawTxBuilder in(BitcoindRpcClient.TxInput in) {
    inputs.add(new Input(in.txid(), in.vout()));
    return this;
  }

  public BitcoinRawTxBuilder in(String txid, int vout) {
    in(new BitcoindRpcClient.BasicTxInput(txid, vout));
    return this;
  }

  public BitcoinRawTxBuilder out(String address, BigDecimal amount) {
    return out(address, amount, null);
  }

  public BitcoinRawTxBuilder out(String address, BigDecimal amount, byte[] data) {
    outputs.add(new BitcoindRpcClient.BasicTxOutput(address, amount, data));
    return this;
  }

  public BitcoinRawTxBuilder in(BigDecimal value) throws GenericRpcException {
    return in(value, 6);
  }

  public BitcoinRawTxBuilder in(BigDecimal value, int minConf) throws GenericRpcException {
    List<BitcoindRpcClient.Unspent> unspent = bitcoin.listUnspent(minConf);
    BigDecimal v = value;
    for (BitcoindRpcClient.Unspent o : unspent) {
      if (!inputs.contains(new Input(o))) {
        in(o);
        v = v.subtract(o.amount());
      }
      if (v.compareTo(BigDecimal.ZERO) < 0)
        break;
    }
    if (BigDecimal.ZERO.compareTo(v) < 0)
      throw new GenericRpcException("Not enough bitcoins (" + v + "/" + value + ")");
    return this;
  }

  private HashMap<String, BitcoindRpcClient.RawTransaction> txCache = new HashMap<>();

  private BitcoindRpcClient.RawTransaction tx(String txId) throws GenericRpcException {
    BitcoindRpcClient.RawTransaction tx = txCache.get(txId);
    if (tx != null)
      return tx;
    tx = bitcoin.getRawTransaction(txId);
    txCache.put(txId, tx);
    return tx;
  }

  public BitcoinRawTxBuilder outChange(String address) throws GenericRpcException {
    return outChange(address, BigDecimal.ZERO);
  }

  public BitcoinRawTxBuilder outChange(String address, BigDecimal fee) throws GenericRpcException {
    BigDecimal is = BigDecimal.ZERO;
    for (BitcoindRpcClient.TxInput i : inputs)
      is = is.add(tx(i.txid()).vOut().get(i.vout()).value());
    BigDecimal os = fee;
    for (BitcoindRpcClient.TxOutput o : outputs)
      os = os.add(o.amount());
    if (os.compareTo(is) < 0)
      out(address, is.subtract(os));
    return this;
  }
  
  public BitcoinRawTxBuilder addPrivateKey(String privateKey)
  {
	  if ( privateKeys == null )
		  privateKeys = new ArrayList<String>();
	  privateKeys.add(privateKey);
	  return this;
  }

  public String create() throws GenericRpcException {
    return bitcoin.createRawTransaction(new ArrayList<>(inputs), outputs);
  }

  public String sign() throws GenericRpcException {
    return bitcoin.signRawTransaction(create(), null, privateKeys);
  }

  public String send() throws GenericRpcException {
    return bitcoin.sendRawTransaction(sign());
  }

}

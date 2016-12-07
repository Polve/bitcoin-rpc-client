/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wf.bitcoin.javabitcoindrpcclient;

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

  private class Input extends BitcoindRpcClient.BasicTxInput {

    public Input(String txid, int vout) {
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

  public BitcoinRawTxBuilder out(String address, double amount) {
    if (amount <= 0d)
      return this;
    outputs.add(new BitcoindRpcClient.BasicTxOutput(address, amount));
    return this;
  }

  public BitcoinRawTxBuilder in(double value) throws BitcoinRpcException {
    return in(value, 6);
  }

  public BitcoinRawTxBuilder in(double value, int minConf) throws BitcoinRpcException {
    List<BitcoindRpcClient.Unspent> unspent = bitcoin.listUnspent(minConf);
    double v = value;
    for (BitcoindRpcClient.Unspent o : unspent) {
      if (!inputs.contains(new Input(o))) {
        in(o);
        v = BitcoinUtil.normalizeAmount(v - o.amount());
      }
      if (v < 0)
        break;
    }
    if (v > 0)
      throw new BitcoinRpcException("Not enough bitcoins (" + v + "/" + value + ")");
    return this;
  }

  private HashMap<String, BitcoindRpcClient.RawTransaction> txCache = new HashMap<>();

  private BitcoindRpcClient.RawTransaction tx(String txId) throws BitcoinRpcException {
    BitcoindRpcClient.RawTransaction tx = txCache.get(txId);
    if (tx != null)
      return tx;
    tx = bitcoin.getRawTransaction(txId);
    txCache.put(txId, tx);
    return tx;
  }

  public BitcoinRawTxBuilder outChange(String address) throws BitcoinRpcException {
    return outChange(address, 0d);
  }

  public BitcoinRawTxBuilder outChange(String address, double fee) throws BitcoinRpcException {
    double is = 0d;
    for (BitcoindRpcClient.TxInput i : inputs)
      is = BitcoinUtil.normalizeAmount(is + tx(i.txid()).vOut().get(i.vout()).value());
    double os = fee;
    for (BitcoindRpcClient.TxOutput o : outputs)
      os = BitcoinUtil.normalizeAmount(os + o.amount());
    if (os < is)
      out(address, BitcoinUtil.normalizeAmount(is - os));
    return this;
  }

  public String create() throws BitcoinRpcException {
    return bitcoin.createRawTransaction(new ArrayList<>(inputs), outputs);
  }

  public String sign() throws BitcoinRpcException {
    return bitcoin.signRawTransaction(create(), null, null);
  }

  public String send() throws BitcoinRpcException {
    return bitcoin.sendRawTransaction(sign());
  }

}

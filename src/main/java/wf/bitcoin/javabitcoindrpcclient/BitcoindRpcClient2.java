package wf.bitcoin.javabitcoindrpcclient;

import java.util.List;
import java.util.Map;

/**
 * an extended client used to consume APIS, such as 'getaddressutxo',
 * which are provided by some btc nodes which supports 'addressindex'. <br/>
 * taking a look at <a href="https://github.com/satoshilabs/bitcoin">satoshilabs/bitcoin</a> 
 * 
 * @author frankchen
 * @create 2018年6月21日 上午10:38:17
 */
public interface BitcoindRpcClient2 extends BitcoindRpcClient
{
    /**
     * the result returned by
     * {@link BitcoinJSONRPCClient2#getAddressBalance(String)}
     * 
     * @author frankchen
     * @create 2018年6月21日 上午10:38:17
     */
    public static class AddressBalance extends MapWrapper
    {
        private long balance;
        private long received;

        public AddressBalance(Map<String, Object> r)
        {
            super(r);
            balance = ((Number) r.get("balance")).longValue();
            received = ((Number) r.get("received")).longValue();
        }

        /**
         * unit is in satoshis
         * 
         * eg, 5,3500,000 = 0.535 BTC
         */
        public long getBalance()
        {
            return balance;
        }

        public long getReceived()
        {
            return received;
        }
    }

    /**
     * the result return by {@link BitcoinJSONRPCClient2#getAddressUtxo(String)}
     * @author frankchen
     * @create 2018年6月21日 上午10:38:17
     */
    public class AddressUtxo
    {
        private String address;
        private String txid;
        private int    outputIndex;
        private String script;
        private long   satoshis;
        private long   height;

        public AddressUtxo(Map<String, Object> result)
        {
            address = result.getOrDefault("address", "").toString();
            txid = result.getOrDefault("txid", "").toString();
            outputIndex = ((Number) result.getOrDefault("outputIndex", "")).intValue();
            script = result.getOrDefault("script", "").toString();
            satoshis = ((Number) result.getOrDefault("satoshis", 0)).longValue();
            height = ((Number) result.getOrDefault("height", -1)).longValue();
        }

        public String getAddress()
        {
            return address;
        }

        /**
         * the txid of this uxto
         */
        public String getTxid()
        {
            return txid;
        }

        public int getOutputIndex()
        {
            return outputIndex;
        }

        public String getScript()
        {
            return script;
        }

        public long getSatoshis()
        {
            return satoshis;
        }

        /**
         * height of block in which this utxo is
         * @return
         */
        public long getHeight()
        {
            return height;
        }
    }
    
    public class AddressUtxoList extends ListMapWrapper<AddressUtxo>
    {
        @SuppressWarnings("rawtypes")
        public AddressUtxoList(List<Map> list)
        {
            super((List<Map>)list);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        protected AddressUtxo wrap(Map m)
        {
            return new AddressUtxo(m);
        }
    }
    

    /**
     * get the balance of specified address
     */
    AddressBalance getAddressBalance(String address);
    
    /**
     * get all the utxo list of a specified address
     */
    List<AddressUtxo> getAddressUtxo(String address);
    
    /**
     * check whether the node is running in a test net or not 
     */
    boolean isTestNet();
}

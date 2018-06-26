package wf.bitcoin.javabitcoindrpcclient;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * an encapsulation of APIs provided by BitPay
 * 
 * internal use only
 * 
 * @author frankchen
 * @create 2018年5月24日 下午3:00:04
 */
public class BitcoinJSONRPCClientExt extends BitcoinJSONRPCClient
{
    public BitcoinJSONRPCClientExt(URL url)
    {
        super(url);
    }

    public static class AddressBalance extends MapWrapper
    {
        private long balanceInSatoshis;
        private long received;
        
        public AddressBalance(Map<String, Object> r)
        {
            super(r);
            balanceInSatoshis = ((Number)r.get("balance")).longValue();
            received = ((Number)r.get("received")).longValue();
        }

        /**
         * unit is in satoshis
         * 
         * eg, 5,3500,000 = 0.535 BTC
         */
        public long getBalanceInSatoshis()
        {
            return balanceInSatoshis;
        }

        public long getReceived()
        {
            return received;
        }
    }

    /**
     * API provided by bitpay's bitcored
     * @param address
     * @return balance is in unit of satoshis
     */
    @SuppressWarnings("unchecked")
    public AddressBalance getAddressBalance(String address)
    {
        return new AddressBalance((Map<String, Object>)query("getaddressbalance", address));
    }

    //    "address": "mse5PYo8iCevsv2DyFFvna8msQFnB8b7Bd",
    //    "txid": "2278c94ca3b59ee2ab9b7c863d745002581a401efc26aed3963a120cccf8a4a7",
    //    "outputIndex": 1,
    //    "script": "76a91484fa57f18e3e7b40eb84097cce323b6f248857b388ac",
    //    "satoshis": 53500000,
    //    "height": 1298221
    public static class AddressUtxo
    {
        private String address;
        private String txid;
        private int outputIndex;
        private String script;
        private long satoshis;
        private long height;
        
        public AddressUtxo(Map<String, Object> result)
        {
            address = result.getOrDefault("address","").toString();
            txid = result.getOrDefault("txid","").toString();
            outputIndex = ((Number)result.getOrDefault("outputIndex","")).intValue();
            script = result.getOrDefault("script","").toString();
            satoshis = ((Number)result.getOrDefault("satoshis",0)).longValue();
            height = ((Number)result.getOrDefault("height", -1)).longValue();
        }
        
        public String getAddress()
        {
            return address;
        }

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

        public long getHeight()
        {
            return height;
        }
    }
    
    public static class AddressUtxoList extends ListMapWrapper<AddressUtxo>
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
     * @param address public key address in base58check encoding
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<AddressUtxo> getAddressUtxo(String address)
    {
        return new AddressUtxoList((List<Map>)query("getaddressutxos", address));
    }
    
    private Boolean isTestNet;
    public boolean isTestNet()
    {
        if ( isTestNet == null )
        {
            isTestNet = new Boolean(getInfo().testnet());
        }
        return isTestNet;
    }
}

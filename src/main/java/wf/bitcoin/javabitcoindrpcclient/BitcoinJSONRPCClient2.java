package wf.bitcoin.javabitcoindrpcclient;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author frankchen
 * @create 2018年5月24日 下午3:00:04
 */
public class BitcoinJSONRPCClient2 extends BitcoinJSONRPCClient implements BitcoindRpcClient2
{
    public BitcoinJSONRPCClient2(URL url)
    {
        super(url);
    }

    /**
     * @param address
     * @return balance is in unit of satoshis
     */
    @SuppressWarnings("unchecked")
    public AddressBalance getAddressBalance(String address)
    {
        return new AddressBalance((Map<String, Object>)query("getaddressbalance", address));
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

package wf.bitcoin.javabitcoindrpcclient.integration;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.MultiSig;

/**
 * Integration tests for the Wallet command group
 * 
 * @see <a href="https://bitcoincore.org/en/doc/0.18.0/rpc/">Bitcoin Core RPC documentation</a>
 */
public class WalletTests extends IntegrationTestBase
{
    @Test
    public void addMultiSigAddressTest()
    {
    	MultiSig multiSig = client.addMultiSigAddress(2,
    			Arrays.asList(
    					"2MtmEeTw22qnkZVnrPy5f1FzPYdZS4D9SJu",
    					"2N8EWC92UvXDNgqCtyWDmcvMQtdn4N9HBkD"));

    	assertEquals("2N2FUD9hyhV2TAShQ89kd8KjYTfgLmNQviA",
    			multiSig.address());
    	assertEquals("52210224f7816995d3b8ef24ae41ed790ec02be829f4074241a1827f838cdfbd9203852103318457f6ab23ada4cea00ea29f98b84cf3a531d4c40d7f46b8b25489f473bf1c52ae",
    			multiSig.redeemScript());
    }
}

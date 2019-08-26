package wf.bitcoin.javabitcoindrpcclient.integration;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.AddressValidationResult;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.MultiSig;

/**
 * Integration tests for the Wallet command group
 * 
 * @see <a href="https://bitcoincore.org/en/doc/0.18.0/rpc/">Bitcoin Core RPC documentation</a>
 */
public class WalletTest extends IntegrationTestBase
{
	@Test(expected = Test.None.class) // no exception expected
    public void addMultiSigAddressTest()
    {
		String addr1 = client.getNewAddress();
		String addr2 = client.getNewAddress();
		
    	MultiSig multiSig = client.addMultiSigAddress(2, Arrays.asList(addr1, addr2));
    	LOGGER.info("Created and added multiSig: " + multiSig);
    	
    	AddressValidationResult addrValidationResult = client.validateAddress(multiSig.address());

    	assertEquals(addrValidationResult.address(), multiSig.address());
    	assertEquals(addrValidationResult.isScript(), true);
    }
}

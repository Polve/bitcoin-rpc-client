package wf.bitcoin.javabitcoindrpcclient.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.AddressInfo;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.AddressValidationResult;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.MultiSig;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Unspent;

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
	
	@Test(expected = Test.None.class) // no exception expected
    public void getAddressInfoTest()
    {
		String address = client.getNewAddress();
		
		AddressInfo addressInfo = client.getAddressInfo(address);
		
		// Check if mandatory fields are there
		assertEquals(address, addressInfo.address());
		assertStringNotNullNorEmpty(addressInfo.scriptPubKey());
		assertNotNull(addressInfo.isMine());
		assertNotNull(addressInfo.isWatchOnly());
		assertNotNull(addressInfo.solvable());
		assertNotNull(addressInfo.isScript());
		assertNotNull(addressInfo.isChange());
		assertNotNull(addressInfo.isWitness());
		assertNotNull(addressInfo.label());
		assertNotNull(addressInfo.labels());
    }
	
	@Test(expected = Test.None.class) // no exception expected
    public void listUnspentTest()
    {
		// Create a new address
		String address = client.getNewAddress();
		
		// Generate a few UTXOs for that address
		// The first 100 blocks do not count, therefore generate 105 to get 5 utxos
		client.generateToAddress(105, address);
		
		// Check if they are retrieved by listUnspent
		List<Unspent> utxos = client.listUnspent(0, Integer.MAX_VALUE, address);
		
		// Check that the previously generated utxos are retrieved by this call
		assertEquals(5, utxos.size());
		
		// Check that mandatory fields are set
		for (Unspent utxo : utxos)
		{
			assertStringNotNullNorEmpty(utxo.txid());
			assertNotNull(utxo.vout());
			assertStringNotNullNorEmpty(utxo.address());
			assertNotNull(utxo.label());
			assertStringNotNullNorEmpty(utxo.scriptPubKey());
			assertNotNull(utxo.amount());
			assertNotNull(utxo.confirmations());
			assertNotNull(utxo.spendable());
			assertNotNull(utxo.solvable());
			assertNotNull(utxo.safe());
		}
    }
}

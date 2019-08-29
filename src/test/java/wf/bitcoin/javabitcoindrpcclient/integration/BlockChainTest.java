package wf.bitcoin.javabitcoindrpcclient.integration;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.BlockChainInfo;

/**
 * Integration tests for the Blockchain command group
 * 
 * @see <a href="https://bitcoincore.org/en/doc/0.18.0/rpc/">Bitcoin Core RPC documentation</a>
 */
public class BlockChainTest extends IntegrationTestBase
{
	@Test(expected = Test.None.class) // no exception expected
	public void getBlockChainInfoTest()
	{
		BlockChainInfo bci = client.getBlockChainInfo();
		
		// Check if mandatory fields are present
		assertNotNull(bci.chain());
		assertNotNull(bci.blocks());
		assertNotNull(bci.headers());
		assertNotNull(bci.bestBlockHash());
		assertNotNull(bci.difficulty());
		assertNotNull(bci.medianTime());
		assertNotNull(bci.verificationProgress());
		assertNotNull(bci.initialBlockDownload());
		assertNotNull(bci.chainWork());
		assertNotNull(bci.sizeOnDisk());
		assertNotNull(bci.pruned());
		assertNotNull(bci.warnings());
	}
}

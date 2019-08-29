package wf.bitcoin.javabitcoindrpcclient.integration;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
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
		
		// Check if mandatory fields are present and have valid values
		assertTrue(StringUtils.isNotBlank(bci.chain()));
		assertTrue(bci.blocks() >= 0);
		assertTrue(bci.headers() >= 0);
		assertTrue(StringUtils.isNotBlank(bci.bestBlockHash()));
		assertTrue(bci.difficulty().compareTo(BigDecimal.ZERO) >= 0);
		assertTrue(bci.medianTime() > 0);
		assertTrue(bci.verificationProgress().intValue() >= 0 && bci.verificationProgress().intValue() <= 1); // interval [0..1]
		assertFalse(bci.initialBlockDownload());
		assertTrue(StringUtils.isNotBlank(bci.chainWork()));
		assertTrue(bci.sizeOnDisk() > 0);
		assertFalse(bci.pruned()); // no pruning enabled for tests
		assertTrue(StringUtils.isBlank(bci.warnings()));
	}
}

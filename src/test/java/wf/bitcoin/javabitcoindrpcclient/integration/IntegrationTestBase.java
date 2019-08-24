package wf.bitcoin.javabitcoindrpcclient.integration;

import org.junit.BeforeClass;

import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.BlockChainInfo;

/**
 * Common framework for integration tests
 * 
 * In order to run these tests, make sure to first have the bitcoin core client running in regtest mode
 * 
 * These tests use the same RPC config, as the normal {@link BitcoinJSONRPCClient}
 */
public class IntegrationTestBase
{
    static BitcoindRpcClient client;
    
    @BeforeClass
    public static void setup() throws Exception
    {
    	client = new BitcoinJSONRPCClient();
    	
    	BlockChainInfo blockChainInfo = client.getBlockChainInfo();
    	
    	String expectedBlockChain = "regtest";
    	if (!blockChainInfo.chain().equals(expectedBlockChain))
    		throw new Exception("Integration tests expected to run on the " + expectedBlockChain + " blockchain, "
    				+ "but client is configured to use: " + blockChainInfo.chain());
    }
}

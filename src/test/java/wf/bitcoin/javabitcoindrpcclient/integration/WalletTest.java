package wf.bitcoin.javabitcoindrpcclient.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.AddressInfo;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.AddressValidationResult;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.MultiSig;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.Transaction;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.TransactionsSinceBlock;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient.TxInput;
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
	
	@Test(expected = Test.None.class) // no exception expected
	public void listSinceBlockTest()
	{
		// Call listSinceBlock to remember the current block (last unprocessed block)
		int blockHeight = client.getBlockCount();
		String blockHash = client.getBlockHash(blockHeight);
		TransactionsSinceBlock listSinceBlockBefore = client.listSinceBlock(blockHash, 1);
		String lastProcessedBlockHashBefore = listSinceBlockBefore.lastBlock();
		int lastProcessedBlockHeightBefore = client.getBlock(lastProcessedBlockHashBefore).height();
		
		// Create some transactions to known addresses
		String address1 = client.getNewAddress();
		String address2 = client.getNewAddress();
		String address3 = client.getNewAddress();
		
		// Generate a few UTXOs for address 1
		// The first 100 blocks do not count, therefore generate 105 to get 5 utxos
		client.generateToAddress(105, address1);
		
		List<Unspent> availableUtxos = client.listUnspent(0, Integer.MAX_VALUE, address1);
		LOGGER.info("Found " + availableUtxos.size() + " UTXOs (unspent transaction outputs) belonging to address1");

		TxInput selectedUtxoForTx1 = availableUtxos.get(0);
		LOGGER.info("Selected UTXO which will be used in tx1 (address1 -> address2) : " + selectedUtxoForTx1);
		
		// Fire off transaction 1 (address1 -> address2)
		String tx1ID = client.sendToAddress(address2, selectedUtxoForTx1.amount());
		LOGGER.info("UTXO sent to address2, tx1 ID: " + tx1ID);

		TxInput selectedUtxoForTx2 = availableUtxos.get(1);
		LOGGER.info("Selected UTXO which will be used in tx2 (address1 -> address3) : " + selectedUtxoForTx2);
		
		// Fire off transaction 2 (address1 -> address3)
		String tx2ID = client.sendToAddress(address3, selectedUtxoForTx2.amount());
		LOGGER.info("UTXO sent to address3, tx2 ID: " + tx2ID);
		
		// Mine some blocks, so that the transactions are added to the blockchain as part of a mined block
		client.generateToAddress(105, address1);
		
		// Call listSinceBlock again
		// This time we start at the block where the previous listSinceBlock left off
		TransactionsSinceBlock listSinceBlockAfter = client.listSinceBlock(lastProcessedBlockHashBefore, 1);
		String lastProcessedBlockHashAfter = listSinceBlockAfter.lastBlock();
		int lastProcessedBlockHeightAfter = client.getBlock(lastProcessedBlockHashAfter).height();
		
		// Check the number of processed blocks is correct
		int deltaBlockHeight = lastProcessedBlockHeightAfter - lastProcessedBlockHeightBefore;
		assertEquals(210, deltaBlockHeight); // 210 = 2 x 105 (we generated 105 blocks two times previously for address1)
		
		// Check that the received transactions are those expected
		List<Transaction> receivedTransactions = listSinceBlockAfter.transactions();
		
		// We collect the txIDs of the received transactions
		List<String> receivedTxIDs = receivedTransactions.stream()
				.map(tx -> tx.txId())
				.collect(Collectors.toList());
		
		// Check that the generated transactions are among the received ones
		assertTrue(receivedTxIDs.contains(tx1ID));
		assertTrue(receivedTxIDs.contains(tx2ID));
	}
	
	@Test(expected = Test.None.class) // no exception expected
	public void listSinceBlockTestIncludeWatchOnly()
	{
		// TODO Found no way to simulate the scenario needed for client.listSinceBlock(includeWatchOnly=true)
		// That requires that the blockchain has transactions where the from and to addresses are not known to the wallet
		// Moreover, these transactions have to be signed, and the signature has to cover previous utxos used as inputs in the tx itself
		// This means we cannot simulate this by hardcoding in the test a raw transaction to-from "watch-only" addresses
		
		// The way to manually validate this scenario (e.g. on testnet3) :
		// 1. Find a valid testnet address that received transactions
		// Use a blockchain explorer if needed ( e.g. https://live.blockcypher.com/btc-testnet/address/mxosQ4CvQR8ipfWdRktyB3u16tauEdamGc/ )
		// 2. Since the address is not known to the wallet, it can be added as a watch-only address
		// importaddress mxosQ4CvQR8ipfWdRktyB3u16tauEdamGc
		// 3. Find a block hash older than some transactions to this address (use blockchain explorer again if needed)
		// 3. Call listsinceblock with relevant args
		// listsinceblock "blockHash" 1 true
		// Result should include transactions to the watch-only address
	}
}

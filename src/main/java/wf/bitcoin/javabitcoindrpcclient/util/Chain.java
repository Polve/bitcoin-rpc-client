package wf.bitcoin.javabitcoindrpcclient.util;

/**
 * Represents the possible values of the chain we're running on.
 * <br><br>
 * Defined by the property "chain" of https://bitcoin.org/en/developer-reference#getblockchaininfo
 */
public enum Chain
{
	MAIN,
	TEST,
	REGTEST;
}

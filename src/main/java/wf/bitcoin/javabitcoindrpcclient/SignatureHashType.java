package wf.bitcoin.javabitcoindrpcclient;

/**
 * In case of an OPCODE of type OP_CHECKSIG in the pubkey script of a
 * transaction, this option determines what parts of the transactions the
 * signers want to sign (and therefore not allow others to modify afterwards)
 * <br><br>
 * 
 * Descriptions of the fields are courtesy of the <a href=
 * "https://bitcoin.org/en/transactions-guide#signature-hash-types">Bitcoin
 * Core Documentation</a>
 */
public enum SignatureHashType
{
	/**
	 * The default, signs all the inputs and outputs, protecting everything except
	 * the signature scripts against modification
	 */
	ALL,					// "ALL"
	
	/**
	 * Signs all of the inputs but none of the outputs, allowing anyone to change
	 * where the satoshis are going unless other signatures using other signature
	 * hash flags protect the outputs.
	 */
	NONE,					// "NONE"
	
	/**
	 * The only output signed is the one corresponding to this input (the output
	 * with the same output index number as this input), ensuring nobody can change
	 * your part of the transaction but allowing other signers to change their part
	 * of the transaction. The corresponding output must exist or the value “1” will
	 * be signed, breaking the security scheme. This input, as well as other inputs,
	 * are included in the signature. The sequence numbers of other inputs are not
	 * included in the signature, and can be updated.
	 */
	SINGLE,					// "SINGLE"
	
	/**
	 * Signs all of the outputs but only this one input, and it also allows anyone
	 * to add or remove other inputs, so anyone can contribute additional satoshis
	 * but they cannot change how many satoshis are sent nor where they go
	 */
	ALL_ANYONECANPAY,		// "ALL|ANYONECANPAY"
	
	/**
	 * Signs only this one input and allows anyone to add or remove other inputs or
	 * outputs, so anyone who gets a copy of this input can spend it however they’d
	 * like
	 */
	NONE_ANYONECANPAY,		// "NONE|ANYONECANPAY"
	
	/**
	 * Signs this one input and its corresponding output. Allows anyone to add or
	 * remove other inputs
	 */
	SINGLE_ANYONECANPAY;	// "SINGLE|ANYONECANPAY"
	
	@Override
	public String toString()
	{
		return super.toString().replace('_', '|');
	}
}

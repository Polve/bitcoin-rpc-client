/*
 * Bitcoin-JSON-RPC-Client License
 * 
 * Copyright (c) 2013, Mikhail Yevchenko.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 
 * Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package wf.bitcoin.javabitcoindrpcclient;

/**
 * error code returned from bitcoind
 * @author frankchen
 * @create 2018年7月9日 下午8:58:52
 */
public class BitcoinRPCErrorCode {
    public static final int RPC_MISC_ERROR                  = -1 ;  //!< std::exception thrown in command handling
    public static final int RPC_FORBIDDEN_BY_SAFE_MODE      = -2 ;  //!< Server is in safe mode, and command is not allowed in safe mode
    public static final int RPC_TYPE_ERROR                  = -3 ;  //!< Unexpected type was passed as parameter
    public static final int RPC_INVALID_ADDRESS_OR_KEY      = -5 ;  //!< Invalid address or key
    public static final int RPC_OUT_OF_MEMORY               = -7 ;  //!< Ran out of memory during operation
    public static final int RPC_INVALID_PARAMETER           = -8 ;  //!< Invalid, missing or duplicate parameter
    public static final int RPC_DATABASE_ERROR              = -20; //!< Database error
    public static final int RPC_DESERIALIZATION_ERROR       = -22; //!< Error parsing or validating structure in raw format
    public static final int RPC_VERIFY_ERROR                = -25; //!< General error during transaction or block submission
    public static final int RPC_VERIFY_REJECTED             = -26; //!< Transaction or block was rejected by network rules
    public static final int RPC_VERIFY_ALREADY_IN_CHAIN     = -27; //!< Transaction already in chain
    public static final int RPC_IN_WARMUP                   = -28; //!< Client still warming up

}

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

import java.util.Map;

import wf.bitcoin.krotjson.JSON;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com
 * @author Alessandro Polverini
 */
@SuppressWarnings("serial")
public class BitcoinRPCException extends GenericRpcException {
  
  private String rpcMethod;
  private String rpcParams;
  private int responseCode;
  private String responseMessage;
  private String response;
  private BitcoinRPCError rpcError;

  /**
   * Creates a new instance of <code>BitcoinRPCException</code> with response
   * detail.
   *
   * @param method the rpc method called
   * @param params the parameters sent
   * @param responseCode the HTTP code received
   * @param responseMessage the HTTP response message
   * @param response the error stream received
   */
  @SuppressWarnings("rawtypes")
  public BitcoinRPCException(String method, 
                             String params, 
                             int    responseCode, 
                             String responseMessage, 
                             String response) {
    super("RPC Query Failed (method: " + method + ", params: " + params + ", response code: " + responseCode + " responseMessage " + responseMessage + ", response: " + response);
    this.rpcMethod = method;
    this.rpcParams = params;
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.response = response;
    if ( responseCode == 500 ) { 
        // Bitcoind application error when handle the request
        // extract code/message for callers to handle
        Map error = (Map) ((Map)JSON.parse(response)).get("error");
        if ( error != null ) {
            rpcError = new BitcoinRPCError(error);
        }
    }
  }
  
  public BitcoinRPCException(String method, String params, Throwable cause) {
    super("RPC Query Failed (method: " + method + ", params: " + params + ")", cause);
    this.rpcMethod = method;
    this.rpcParams = params;
  }

  /**
   * Constructs an instance of <code>BitcoinRPCException</code> with the
   * specified detail message.
   *
   * @param msg the detail message.
   */
  public BitcoinRPCException(String msg) {
    super(msg);
  }

  public BitcoinRPCException(BitcoinRPCError error) {
      super(error.getMessage());
      this.rpcError  = error;
  }
  
  public BitcoinRPCException(String message, Throwable cause) {
    super(message, cause);
  }

  public int getResponseCode() {
    return responseCode;
  }

  public String getRpcMethod() {
    return rpcMethod;
  }

  public String getRpcParams() {
    return rpcParams;
  }

  /**
   * @return the HTTP response message
   */
  public String getResponseMessage() {
    return responseMessage;
  }

  /**
   * @return response message from bitcored
   */
  public String getResponse() {
      return this.response;
  }
  
  /**
   * @return response message from bitcored
   */
  public BitcoinRPCError getRPCError() {
      return this.rpcError;
  }
}

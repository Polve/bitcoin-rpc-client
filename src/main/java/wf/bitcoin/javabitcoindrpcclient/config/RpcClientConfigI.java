package wf.bitcoin.javabitcoindrpcclient.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

//See https://github.com/lviggiano/owner
@Sources({ "classpath:config.properties" })
public interface RpcClientConfigI extends Config
{
	/**
	 * Similar to the -datadir argument given to the bitcoin core binary
	 * <br><br>
	 * Using this will cause the RPC Client to look for bitcoin.conf in this path
	 * 
	 * @return Manually specified bitcoin-core data folder path
	 */
	@Key("core.dataFolder.path")
	String bitcoinCoreDataFolder();
}
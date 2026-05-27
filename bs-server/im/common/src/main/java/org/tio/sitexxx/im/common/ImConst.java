
/**
 * 
 */
package org.tio.sitexxx.im.common;

import org.tio.server.TioServerConfig;

/**
 * @author tanyaowu
 *
 */
public class ImConst {

	/**
	 * 达到这个尺寸就要gzip
	 */
	public static final short SIZE_FOR_COMPRESS = 300;//Short.MAX_VALUE;

	private static TioServerConfig tioServerConfigApp = null;
	
	private static TioServerConfig tioServerConfigWs = null;

	/**
	 * 
	 */
	public ImConst() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

	/**
	 * @return the tioServerConfigApp
	 */
	public static TioServerConfig getTioServerConfigApp() {
		return tioServerConfigApp;
	}

	/**
	 * @param tioServerConfigApp the tioServerConfigApp to set
	 */
	public static void setTioServerConfigApp(TioServerConfig tioServerConfigApp) {
		ImConst.tioServerConfigApp = tioServerConfigApp;
	}

	/**
	 * @return the tioServerConfigWs
	 */
	public static TioServerConfig getTioServerConfigWs() {
		return tioServerConfigWs;
	}

	/**
	 * @param tioServerConfigWs the tioServerConfigWs to set
	 */
	public static void setTioServerConfigWs(TioServerConfig tioServerConfigWs) {
		ImConst.tioServerConfigWs = tioServerConfigWs;
	}

}

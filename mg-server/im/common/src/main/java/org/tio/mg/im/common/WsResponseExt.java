
package org.tio.mg.im.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.websocket.common.WsResponse;

/**
 * @author tanyaowu 
 * 2016年9月14日 下午3:35:02
 */
public class WsResponseExt extends WsResponse {
	private static final long serialVersionUID = -509523038825749962L;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(WsResponseExt.class);

	private ImPacket initPacket;

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the initPacket
	 */
	public ImPacket getInitPacket() {
		return initPacket;
	}

	/**
	 * @param initPacket the initPacket to set
	 */
	public void setInitPacket(ImPacket initPacket) {
		this.initPacket = initPacket;
	}

	@Override
	public String toString() {
		return "WsResponseExt [initPacket=" + initPacket + "]";
	}
}

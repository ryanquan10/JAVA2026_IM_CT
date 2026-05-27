
package org.tio.sitexxx.im.server.handler;

import org.tio.core.ChannelContext;
import org.tio.sitexxx.im.common.ImPacket;

/**
 * 
 * @author tanyaowu 
 * 2016年9月7日 下午4:34:51
 */
public interface ImServerHandler {
	/**
	 * 
	 * @param packet
	 * @param channelContext
	 * @param isWebsocket
	 * @return
	 * @throws Exception
	 * @author: tanyaowu
	 */
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket) throws Exception;

}

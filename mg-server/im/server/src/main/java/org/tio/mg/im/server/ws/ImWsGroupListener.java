
package org.tio.mg.im.server.ws;

import org.tio.core.ChannelContext;
import org.tio.mg.im.server.TioSiteImGroupListener;

/**
 * @author tanyaowu
 * 2016年5月13日 下午10:38:36
 */
public class ImWsGroupListener extends TioSiteImGroupListener {
	public static final ImWsGroupListener me = new ImWsGroupListener();

	/**
	 *
	 * @author tanyaowu
	 */
	private ImWsGroupListener() {
	}

	/**
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void onAfterBind(ChannelContext channelContext, String group) throws Exception {
		super.onAfterBind(channelContext, group);
	}

	/**
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void onAfterUnbind(ChannelContext channelContext, String group) throws Exception {
		super.onAfterUnbind(channelContext, group);
	}
}

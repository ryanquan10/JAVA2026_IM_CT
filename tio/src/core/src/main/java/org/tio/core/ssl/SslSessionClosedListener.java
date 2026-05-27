/*
 * gimervpqybxksx本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动gqxtpdqeuwnbh
 */
package org.tio.core.ssl;

import org.tio.core.ChannelContext;
import org.tio.core.ChannelContext.CloseCode;
import org.tio.core.Tio;
import org.tio.core.ssl.facade.ISessionClosedListener;

public class SslSessionClosedListener implements ISessionClosedListener {
    private ChannelContext channelContext;

    public SslSessionClosedListener(ChannelContext channelContext) {
	this.channelContext = channelContext;
    }

    @Override
    public void onSessionClosed() {
	// log.info("{} onSessionClosed", channelContext);
	Tio.close(channelContext, "SSL SessionClosed", CloseCode.SSL_SESSION_CLOSED);
    }

}


package org.tio.sitexxx.service.tio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.TioConfig;
import org.tio.core.intf.Packet;
import org.tio.core.stat.IpStat;
import org.tio.core.stat.IpStatListener;
import org.tio.sitexxx.service.model.stat.TioIpStat;
import org.tio.utils.json.Json;

/**
 * @author tanyaowu 
 * 2016年9月25日 下午5:25:35
 */
public class TioSiteIpStatListener implements IpStatListener {
	private static Logger log = LoggerFactory.getLogger(TioSiteIpStatListener.class);

	/**
	 * web view服务器
	 */
	public final static TioSiteIpStatListener web_view = new TioSiteIpStatListener((short) 11);

	/**
	 * web api服务器
	 */
	public final static TioSiteIpStatListener web_api = new TioSiteIpStatListener((short) 12);

	/**
	 * websocket
	 */
	public final static TioSiteIpStatListener ws = new TioSiteIpStatListener((short) 1);

	/**
	 * app
	 */
	public final static TioSiteIpStatListener app = new TioSiteIpStatListener((short) 2);

	private short appType;

	/**
	 * 
	 * @author: tanyaowu
	 */
	private TioSiteIpStatListener(short appType) {
		this.appType = appType;
	}

	@Override
	public void onExpired(TioConfig tioConfig, IpStat ipStat) {
		TioIpStat tioIpStat = TioIpStat.from(tioConfig, ipStat, appType);
		if (tioIpStat != null) {
			tioIpStat.save();
		} else {
			log.error("{}, tioIpStat is null, ipStat is {}", tioConfig.getName(), Json.toJson(ipStat));
		}
	}

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the appType
	 */
	public short getAppType() {
		return appType;
	}

	/**
	 * @param appType the appType to set
	 */
	public void setAppType(short appType) {
		this.appType = appType;
	}

	@Override
	public void onDecodeError(ChannelContext channelContext, IpStat ipStat) {

	}

	@Override
	public void onAfterSent(ChannelContext channelContext, Packet packet, boolean isSentSuccess, IpStat ipStat) throws Exception {

	}

	@Override
	public void onAfterDecoded(ChannelContext channelContext, Packet packet, int packetSize, IpStat ipStat) throws Exception {

	}

	@Override
	public void onAfterHandled(ChannelContext channelContext, Packet packet, IpStat ipStat, long cost) throws Exception {

	}

	@Override
	public void onAfterConnected(ChannelContext channelContext, boolean isConnected, boolean isReconnect, IpStat ipStat) throws Exception {

	}

	@Override
	public void onAfterReceivedBytes(ChannelContext channelContext, int receivedBytes, IpStat ipStat) throws Exception {

	}
}

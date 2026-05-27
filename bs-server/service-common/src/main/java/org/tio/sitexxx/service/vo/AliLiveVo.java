
package org.tio.sitexxx.service.vo;

/**
 * @author tanyaowu
 * 2016年9月22日 上午10:14:49
 */
public class AliLiveVo {

	/**
	 * @param args
	 * @author: tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * 推流地址的前半段， 对应到OBS里面的URL
	 * rtmp://video-center.alivecdn.com/nb/
	 *
	 */
	private String appurl;

	/**
	 * 完整的推流地址
	 * rtmp://video-center.alivecdn.com/nb/12?vhost=live.t-io.org&auth_key=1506058365557-0-0-2edfda9e3425083960d8d3c7de18b94b
	 */
	private String publishurl;

	/**
	 * 推流地址，后半段，对应到OBS里面的流名字
	 * jkoknlkiEdsFagUfs?vhost=live.t-io.org&auth_key=1506079237-0-0-2eb91aa6ca9bcb01b618c1d451afd112
	 */
	private String streamid;

	/**
	 * rtmp的拉流地址
	 * rtmp://live.t-io.org/nb/12?auth_key=1506059050-0-0-dc7b674828231b6db6cfa7283f93c80b
	 */
	private String pullrtmp;

	/**
	 * flv的拉流地址
	 * http://live.t-io.org/nb/12.flv?auth_key=1506059050-0-0-710925642dde56f01740054181bcac91
	 */
	private String pullflv;

	/**
	 * m3u8的拉流地址
	 * http://live.t-io.org/nb/12.m3u8?auth_key=1506059050-0-0-7710084e689d5686041bf7740c05dad3
	 */
	private String pullm3u8;

	/**
	 *
	 * @author: tanyaowu
	 */
	public AliLiveVo() {
	}

	public String getAppurl() {
		return appurl;
	}

	public void setAppurl(String appurl) {
		this.appurl = appurl;
	}

	public String getPublishurl() {
		return publishurl;
	}

	public void setPublishurl(String publishurl) {
		this.publishurl = publishurl;
	}

	public String getStreamid() {
		return streamid;
	}

	public void setStreamid(String streamid) {
		this.streamid = streamid;
	}

	public String getPullrtmp() {
		return pullrtmp;
	}

	public void setPullrtmp(String pullrtmp) {
		this.pullrtmp = pullrtmp;
	}

	public String getPullflv() {
		return pullflv;
	}

	public void setPullflv(String pullflv) {
		this.pullflv = pullflv;
	}

	public String getPullm3u8() {
		return pullm3u8;
	}

	public void setPullm3u8(String pullm3u8) {
		this.pullm3u8 = pullm3u8;
	}

}

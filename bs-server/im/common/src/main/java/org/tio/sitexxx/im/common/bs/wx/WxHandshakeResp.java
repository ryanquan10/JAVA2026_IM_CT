

package org.tio.sitexxx.im.common.bs.wx;

import java.io.Serializable;

public class WxHandshakeResp implements Serializable {
	private static final long	serialVersionUID	= 3269628193908001549L;
	private String				cid;
	/**
	 * 握手ip
	 */
	private String				ip;

	/**
	 * @return the cid
	 */
	public String getCid() {
		return cid;
	}

	/**
	 * @param cid the cid to set
	 */
	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

}


package org.tio.sitexxx.im.common.bs;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.vo.MobileInfo;

public class HandshakeReq implements Serializable {
	private static final long	serialVersionUID	= -8074137966541427395L;
	@SuppressWarnings("unused")
	private static Logger		log					= LoggerFactory.getLogger(HandshakeReq.class);

	private String token; //客户端通过http登录后，服务器返回给客户端的token值，没登录则为空串

	private String sign; //签名

	private String jpushinfo;

	/**
	 * org.tio.sitexxx.service.vo.Devicetype的枚举值
	 */
	private Short		devicetype	= null;
	private MobileInfo	mobileInfo;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	/**
	 *
	 */
	public HandshakeReq() {

	}

	public Short getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Short devicetype) {
		this.devicetype = devicetype;
	}

	public MobileInfo getMobileInfo() {
		return mobileInfo;
	}

	public void setMobileInfo(MobileInfo mobileInfo) {
		this.mobileInfo = mobileInfo;
	}

	public String getJpushinfo() {
		return jpushinfo;
	}

	public void setJpushinfo(String jpushinfo) {
		this.jpushinfo = jpushinfo;
	}
}

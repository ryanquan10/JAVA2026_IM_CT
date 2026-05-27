
package org.tio.sitexxx.service.vo;

import cn.hutool.core.bean.BeanUtil;
import org.tio.sitexxx.service.model.main.WxWalletRedPacketRandom;
import org.tio.sitexxx.service.model.main.WxWalletRedPacketRandomLocal;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacket;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacketLocal;

import java.io.Serializable;
import java.util.Map;

/**
 * 发红包vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class GrabRedpacketLocalVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7132289661870632884L;

	/**
	 * uid-Y
	 */
	private Integer uid;

	/**
	 * 钱包id
	 */
	private String walletid;

	/**
	 * 红包流水
	 */
	private String serialnumber;

	/**
	 * 红包id
	 */
	private Integer rid;

	/**
	 * 会话信息
	 */
	private Long chatlinkid;

	/**
	 * 会话业务id
	 */
	private Long bizid;

	/**
	 * 会话模型
	 */
	private Short chatmode;

	/**
	 * ip-N
	 */
	private String ip;

	/**
	 * 设备信息
	 */
	private Short devicetype;

	/**
	 * app版本
	 */
	private String appversion;

	/**
	 * 随机的红包
	 */
	private WxWalletRedPacketRandomLocal random;

	/**
	 * 红包主体
	 */
	private WxWalletSendRedPacketLocal redPacket;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Short getDevicetype() {
		return devicetype;
	}

	public void setDevicetype(Short devicetype) {
		this.devicetype = devicetype;
	}

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	public WxWalletRedPacketRandomLocal getRandom() {
		return random;
	}

	public void setRandom(WxWalletRedPacketRandomLocal random) {
		this.random = random;
	}

	public WxWalletSendRedPacketLocal getRedPacket() {
		return redPacket;
	}

	public void setRedPacket(WxWalletSendRedPacketLocal redPacket) {
		this.redPacket = redPacket;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public Long getBizid() {
		return bizid;
	}

	public Integer getRid() {
		return rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public void setBizid(Long bizid) {
		this.bizid = bizid;
	}

	public Short getChatmode() {
		return chatmode;
	}

	public void setChatmode(Short chatmode) {
		this.chatmode = chatmode;
	}

	public String getWalletid() {
		return walletid;
	}

	public void setWalletid(String walletid) {
		this.walletid = walletid;
	}

	public String getSerialnumber() {
		return serialnumber;
	}

	public void setSerialnumber(String serialnumber) {
		this.serialnumber = serialnumber;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:24
	 */
	public Map<String, Object> toMap() {
		return BeanUtil.beanToMap(this);
	}

	/**
	 * @param userVo
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:23
	 */
	public static GrabRedpacketLocalVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new GrabRedpacketLocalVo(), true);
	}
}

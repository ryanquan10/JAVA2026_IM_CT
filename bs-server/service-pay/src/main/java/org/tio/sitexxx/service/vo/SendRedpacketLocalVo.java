
package org.tio.sitexxx.service.vo;

import cn.hutool.core.bean.BeanUtil;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacket;
import org.tio.sitexxx.service.model.main.WxWalletSendRedPacketLocal;

import java.io.Serializable;
import java.util.Map;

/**
 * 发红包vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class SendRedpacketLocalVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4464819506130930255L;

	/**
	 * uid-Y
	 */
	private Integer uid;

	/**
	 * 钱包id
	 */
	private String walletid;

	/**
	 * 附属钱包
	 */
	private String subwalletid;

	/**
	 * 
	 * 普通红包:1;手气红包:2
	 */
	private Short mode;

	/**
	 * 单笔金额:以分为单位普通红包、一对一红包必传此值
	 */
	private String singlecny;

	/**
	 * 总金额：以分为单位拼手气红包必传此值
	 */
	private String cny;

	/**
	 * 支付协议号
	 */
	private String agrno;

	/**
	 * 红包数量:一对一红包数量为 1，普通群红包和拼手气红包 数量最大 100 个
	 */
	private Short num;

	/**
	 * 通知地址
	 */
	private String notifyUrl;

	/**
	 * 快捷支付超时时间
	 */
	private Short paytimeout;

	/**
	 * 快捷支付确认短信
	 */
	private String smscode;

	/**
	 * 快捷支付确认订单
	 */
	private String merorderid;

	/**
	 * 支付密码
	 */
	private String paypwd;

	/**
	 * 支付类型
	 */
	private Short paytype;

	/**
	 * 红包id
	 */
	private Integer rid;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 会话id
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
	 * 币种
	 */
	private String currency = "CNY";

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
	 * ipinfo
	 */
	private IpInfo ipInfo;

	private WxWalletSendRedPacketLocal sendRed;

	//老版本兼容
	/**
	 * 
	 * GROUP_NORMAL 普通群红包:每个最大金额为 200 元
	 * ONE_TO_ONE 一对一:最大金额为 200 元
	 * GROUP_LUCK 拼手气红包:总金额=人数*200 元
	 * 
	 * 普通红包:1;手气红包:2
	 */
	@SuppressWarnings("unused")
	private Short packetType;

	/**
	 * 单笔金额:以分为单位普通红包、一对一红包必传此值
	 */
	@SuppressWarnings("unused")
	private String singleAmount;

	/**
	 * 总金额：以分为单位拼手气红包必传此值
	 */
	@SuppressWarnings("unused")
	private String amount;

	/**
	 * 红包数量:一对一红包数量为 1，普通群红包和拼手气红包 数量最大 100 个
	 */
	@SuppressWarnings("unused")
	private Short packetCount;

	public void setPacketType(Short packetType) {
		this.packetType = packetType;
		this.mode = packetType;
	}

	public void setSingleAmount(String singleAmount) {
		this.singleAmount = singleAmount;
		this.singlecny = singleAmount;
	}

	public void setAmount(String amount) {
		this.cny = amount;
		this.amount = amount;
	}

	public void setPacketCount(Short packetCount) {
		this.num = packetCount;
		this.packetCount = packetCount;
	}

	public IpInfo getIpInfo() {
		return ipInfo;
	}

	public void setIpInfo(IpInfo ipInfo) {
		this.ipInfo = ipInfo;
	}

	public WxWalletSendRedPacketLocal getSendRed() {
		return sendRed;
	}

	public void setSendRed(WxWalletSendRedPacketLocal sendRed) {
		this.sendRed = sendRed;
	}

	public String getSubwalletid() {
		return subwalletid;
	}

	public void setSubwalletid(String subwalletid) {
		this.subwalletid = subwalletid;
	}

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

	public String getAgrno() {
		return agrno;
	}

	public void setAgrno(String agrno) {
		this.agrno = agrno;
	}

	public String getSmscode() {
		return smscode;
	}

	public void setSmscode(String smscode) {
		this.smscode = smscode;
	}

	public String getMerorderid() {
		return merorderid;
	}

	public void setMerorderid(String merorderid) {
		this.merorderid = merorderid;
	}

	public String getPaypwd() {
		return paypwd;
	}

	public void setPaypwd(String paypwd) {
		this.paypwd = paypwd;
	}

	public Short getPaytype() {
		return paytype;
	}

	public void setPaytype(Short paytype) {
		this.paytype = paytype;
	}

	public Integer getRid() {
		return rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
	}

	public Short getPaytimeout() {
		return paytimeout;
	}

	public void setPaytimeout(Short paytimeout) {
		this.paytimeout = paytimeout;
	}

	public Short getChatmode() {
		return chatmode;
	}

	public void setChatmode(Short chatmode) {
		this.chatmode = chatmode;
	}

	public Long getBizid() {
		return bizid;
	}

	public void setBizid(Long bizid) {
		this.bizid = bizid;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getWalletid() {
		return walletid;
	}

	public void setWalletid(String walletid) {
		this.walletid = walletid;
	}

	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Short getMode() {
		return mode;
	}

	public void setMode(Short mode) {
		this.mode = mode;
	}

	public String getSinglecny() {
		return singlecny;
	}

	public void setSinglecny(String singlecny) {
		this.singlecny = singlecny;
	}

	public String getCny() {
		return cny;
	}

	public void setCny(String cny) {
		this.cny = cny;
	}

	public Short getNum() {
		return num;
	}

	public void setNum(Short num) {
		this.num = num;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
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
	public static SendRedpacketLocalVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new SendRedpacketLocalVo(), true);
	}
}

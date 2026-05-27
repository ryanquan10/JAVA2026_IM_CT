
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 充值vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
/**
 * 
 * @author lixinji
 * 2021年3月11日 上午11:26:43
 */
public class RechargeVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6366423991922966177L;

	/**
	 * uid-Y
	 */
	private Integer uid;

	/**
	 * 钱包id
	 */
	private String walletid;

	/**
	 * 通知地址
	 */
	private String notifyUrl;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 金额
	 */
	private String amount;

	/**
	 * 绑定的银行卡协议号-Y
	 */
	private String agrno;

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
	 * 到账账号
	 */
	private String towalletid;

	/**
	 * 订单超时时长
	 */
	private Short timeout;

	/**
	 *  支付类型，1 微信支付 2 支付宝支付 3 u链 4 银行卡
	 */
	private Integer type;

	private String paypwd;

	public String getPaypwd() {
		return paypwd;
	}

	public void setPaypwd(String paypwd) {
		this.paypwd = paypwd;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	public Short getTimeout() {
		return timeout;
	}

	public void setTimeout(Short timeout) {
		this.timeout = timeout;
	}

	public String getTowalletid() {
		return towalletid;
	}

	public void setTowalletid(String towalletid) {
		this.towalletid = towalletid;
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getAgrno() {
		return agrno;
	}

	public void setAgrno(String agrno) {
		this.agrno = agrno;
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
	public static RechargeVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new RechargeVo(), true);
	}
}

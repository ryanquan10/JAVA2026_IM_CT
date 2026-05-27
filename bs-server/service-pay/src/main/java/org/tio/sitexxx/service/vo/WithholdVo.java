
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;

import cn.hutool.core.bean.BeanUtil;

/**
 * 提现vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class WithholdVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1672752228173101766L;

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
	 * 支付密码
	 */
	private String paypwd;

	private Integer type;

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
	 * 手续费-N
	 */
	private Integer bizfee;

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

	/**
	 * 
	 */
	private User user;

	/**
	 * 币种
	 */
	private String currency = "CNY";

//	private String ulink;
//
//	private String ulinkimg;

//	public String getUlink() {
//		return ulink;
//	}
//
//	public void setUlink(String ulink) {
//		this.ulink = ulink;
//	}
//
//	public String getUlinkimg() {
//		return ulinkimg;
//	}
//
//	public void setUlinkimg(String ulinkimg) {
//		this.ulinkimg = ulinkimg;
//	}

	/**
	 * 订单超时时长
	 */
	private Short timeout;

	public Short getTimeout() {
		return timeout;
	}

	public void setTimeout(Short timeout) {
		this.timeout = timeout;
	}

	public String getPaypwd() {
		return paypwd;
	}

	public void setPaypwd(String paypwd) {
		this.paypwd = paypwd;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	public Short getDevicetype() {
		return devicetype;
	}

	public Integer getBizfee() {
		return bizfee;
	}

	public void setBizfee(Integer bizfee) {
		this.bizfee = bizfee;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setDevicetype(Short devicetype) {
		this.devicetype = devicetype;
	}

	public IpInfo getIpInfo() {
		return ipInfo;
	}

	public void setIpInfo(IpInfo ipInfo) {
		this.ipInfo = ipInfo;
	}

	public String getAgrno() {
		return agrno;
	}

	public void setAgrno(String agrno) {
		this.agrno = agrno;
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

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:24
	 */
	public Map<String, Object> toMap() {
		return BeanUtil.beanToMap(this);
	}

	/**
	 * @param map
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:23
	 */
	public static WithholdVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new WithholdVo(), true);
	}
}

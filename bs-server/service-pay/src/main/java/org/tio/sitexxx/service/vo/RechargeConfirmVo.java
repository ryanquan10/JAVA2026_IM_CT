
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.WxWalletRechargeItem;

import cn.hutool.core.bean.BeanUtil;

/**
 * 充值确认vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class RechargeConfirmVo implements Serializable {

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
	 * 商户订单号
	 */
	private String merorderid;

	/**
	 * 充值id；
	 */
	private Integer rid;

	/**
	 * 短信-未使用
	 */
	private String smscode;

	/**
	 * ip-N
	 */
	private String ip;

	/**
	 * 设备信息
	 */
	private Short devicetype;

	/**
	 * ipinfo
	 */
	private IpInfo ipInfo;

	/**
	 * 
	 */
	private WxWalletRechargeItem order;

	public WxWalletRechargeItem getOrder() {
		return order;
	}

	public void setOrder(WxWalletRechargeItem order) {
		this.order = order;
	}

	public Integer getRid() {
		return rid;
	}

	public void setRid(Integer rid) {
		this.rid = rid;
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

	public String getMerorderid() {
		return merorderid;
	}

	public void setMerorderid(String merorderid) {
		this.merorderid = merorderid;
	}

	public String getSmscode() {
		return smscode;
	}

	public void setSmscode(String smscode) {
		this.smscode = smscode;
	}

	public Short getDevicetype() {
		return devicetype;
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
	public static RechargeConfirmVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new RechargeConfirmVo(), true);
	}
}

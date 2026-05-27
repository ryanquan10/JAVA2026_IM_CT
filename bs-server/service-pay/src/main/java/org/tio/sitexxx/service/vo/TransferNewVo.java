
package org.tio.sitexxx.service.vo;

import cn.hutool.core.bean.BeanUtil;
import org.tio.sitexxx.service.model.main.IpInfo;
import org.tio.sitexxx.service.model.main.User;

import java.io.Serializable;
import java.util.Map;

/**
 * 提现vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class TransferNewVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1672752228173101766L;

	/**
	 * uid-Y
	 */
	private Integer fUid;

	private Integer tUid;

	/**
	 * 支付密码
	 */
	private String paypwd;


	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 金额
	 */
	private String amount;


	/**
	 * 钱包id
	 */
	private String walletid;

	/**
	 * ipinfo
	 */
	private IpInfo ipInfo;

	/**
	 * app版本
	 */
	private String appversion;

	/**
	 * 设备信息
	 */
	private Short devicetype;


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

	public String getWalletid() {
		return walletid;
	}

	public void setWalletid(String walletid) {
		this.walletid = walletid;
	}


	public Integer getfUid() {
		return fUid;
	}

	public void setfUid(Integer fUid) {
		this.fUid = fUid;
	}

	public Integer gettUid() {
		return tUid;
	}

	public void settUid(Integer tUid) {
		this.tUid = tUid;
	}

	public String getPaypwd() {
		return paypwd;
	}

	public void setPaypwd(String paypwd) {
		this.paypwd = paypwd;
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

	public String getAppversion() {
		return appversion;
	}

	public void setAppversion(String appversion) {
		this.appversion = appversion;
	}

	/**
	 * @param map
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:23
	 */
	public static TransferNewVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new TransferNewVo(), true);
	}
}

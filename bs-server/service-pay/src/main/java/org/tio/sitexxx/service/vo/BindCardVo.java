
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 绑卡请求Vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class BindCardVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4990253834557702763L;

	/**
	 * uid
	 */
	private Integer uid;

	/**
	 * 姓名-Y
	 */
	private String name;

	public String getBankname() {
		return bankname;
	}

	public void setBankname(String bankname) {
		this.bankname = bankname;
	}

	private String bankname;


	/**
	 * 银行卡code
	 */
	private String bankcardno;

	/**
	 * 证件号码-Y
	 */
	private String cardno;

	/**
	 * 手机号-Y
	 */
	private String mobile;

	/**
	 * 信用卡ccv2
	 */
	private String cvv2;

	/**
	 * 信用卡有效期
	 */
	private String availabledate;

	/**
	 * ip-N
	 */
	private String ip;

	/**
	 * 钱包id-N
	 */
	private String walletid;

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getCvv2() {
		return cvv2;
	}

	public void setCvv2(String cvv2) {
		this.cvv2 = cvv2;
	}

	public String getAvailabledate() {
		return availabledate;
	}

	public void setAvailabledate(String availabledate) {
		this.availabledate = availabledate;
	}

	public String getBankcardno() {
		return bankcardno;
	}

	public void setBankcardno(String bankcardno) {
		this.bankcardno = bankcardno;
	}

	public String getWalletid() {
		return walletid;
	}

	public void setWalletid(String walletid) {
		this.walletid = walletid;
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
	public static BindCardVo toBean(Map<String, Object> bindVo) {
		return BeanUtil.fillBeanWithMap(bindVo, new BindCardVo(), true);
	}
}


package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 开户请求Vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class OpenUserVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4990253834557702763L;

	/**
	 * uid-Y
	 */
	private Integer uid;

	/**
	 * 姓名-Y
	 */
	private String name;

	/**
	 * 证件号码-Y
	 */
	private String cardno;

	/**
	 * 手机号-Y
	 */
	private String mobile;

	/**
	 * 实名信息id
	 */
	private Integer infoid;

	/**
	 * mac地址-S
	 */
	private String mac;

	/**
	 * nickName-S
	 */
	private String nickName;

	/**
	 * 职业-N
	 */
	private String profession = "A";

	/**
	 * 证件类型:默认-IDCARD-N
	 */
	private String cardtype = "IDCARD";

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

	private String paypwd;

	public String getPaypwd() {
		return paypwd;
	}

	public void setPaypwd(String paypwd) {
		this.paypwd = paypwd;
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

	public Integer getInfoid() {
		return infoid;
	}

	public void setInfoid(Integer infoid) {
		this.infoid = infoid;
	}

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

	public String getCardtype() {
		return cardtype;
	}

	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
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

	public String getProfession() {
		return profession;
	}

	public void setProfession(String profession) {
		this.profession = profession;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return StrUtil.isBlank(mac) ? "0.0.0.0" : mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
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
	public static OpenUserVo toBean(Map<String, Object> userVo) {
		return BeanUtil.fillBeanWithMap(userVo, new OpenUserVo(), true);
	}
}

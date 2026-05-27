
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import org.tio.sitexxx.service.model.main.WxWalletBankCards;

import cn.hutool.core.bean.BeanUtil;

/**
 * 绑卡确认请求Vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class BindCardConfirmVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4990253834557702763L;

	/**
	 * 商户订单号
	 */
	private String merorderid;

	/**
	 * 银行卡id
	 */
	private Integer bankcardid;

	/**
	 * 短信验证码
	 */
	private String smscode;

	/**
	 * 
	 */
	private Integer uid;

	/**
	 * ip-N
	 */
	private String ip;

	/**
	 * 初始化银行卡
	 */
	private WxWalletBankCards initCards;

	public WxWalletBankCards getInitCards() {
		return initCards;
	}

	public void setInitCards(WxWalletBankCards initCards) {
		this.initCards = initCards;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}

	public Integer getBankcardid() {
		return bankcardid;
	}

	public void setBankcardid(Integer bankcardid) {
		this.bankcardid = bankcardid;
	}

	public String getMerorderid() {
		return merorderid;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
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

	/**
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:24
	 */
	public Map<String, Object> toMap() {
		return BeanUtil.beanToMap(this);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @param userVo
	 * @return
	 * @author lixinji
	 * 2020年11月3日 下午4:58:23
	 */
	public static BindCardConfirmVo toBean(Map<String, Object> bindVo) {
		return BeanUtil.fillBeanWithMap(bindVo, new BindCardConfirmVo(), true);
	}
}

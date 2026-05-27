
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import org.tio.sitexxx.service.model.main.WxWalletBankCards;

import cn.hutool.core.bean.BeanUtil;

/**
 * 解绑请求Vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class UnBindCardVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4990253834557702763L;

	/**
	 * 绑定的银行卡协议号-Y
	 */
	private String agrno;

	/**
	 * 
	 */
	private String bankcardid;

	/**
	 * 
	 */
	private Integer uid;

	private String paypwd;

	/**
	 * 钱包id-N
	 */
	private String walletid;

	private WxWalletBankCards removeCard;

	public String getPaypwd() {
		return paypwd;
	}

	public void setPaypwd(String paypwd) {
		this.paypwd = paypwd;
	}

	public WxWalletBankCards getRemoveCard() {
		return removeCard;
	}

	public void setRemoveCard(WxWalletBankCards removeCard) {
		this.removeCard = removeCard;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}

	public String getAgrno() {
		return agrno;
	}

	public void setAgrno(String agrno) {
		this.agrno = agrno;
	}

	public String getWalletid() {
		return walletid;
	}

	public void setWalletid(String walletid) {
		this.walletid = walletid;
	}

	public Integer getUid() {
		return uid;
	}

	public String getBankcardid() {
		return bankcardid;
	}

	public void setBankcardid(String bankcardid) {
		this.bankcardid = bankcardid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
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
	public static UnBindCardVo toBean(Map<String, Object> bindVo) {
		return BeanUtil.fillBeanWithMap(bindVo, new UnBindCardVo(), true);
	}
}

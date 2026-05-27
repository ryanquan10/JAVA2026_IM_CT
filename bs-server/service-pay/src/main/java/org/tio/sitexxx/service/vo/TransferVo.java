
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 转账vo
 * 
 * @author lixinji
 * 2021年3月9日 下午1:45:08
 */
public class TransferVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5065642665564253002L;

	/**
	 * uid-Y
	 */
	private Integer uid;

	/**
	 * 钱包id
	 */
	private String walletid;

	/**
	 * to转账
	 */
	private String towalletid;

	/**
	 * 金额
	 */
	private String cny;

	/**
	 * 订单超时时长
	 */
	private Integer timeout;

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
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

	public String getTowalletid() {
		return towalletid;
	}

	public void setTowalletid(String towalletid) {
		this.towalletid = towalletid;
	}

	public String getCny() {
		return cny;
	}

	public void setCny(String cny) {
		this.cny = cny;
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
	public static TransferVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new TransferVo(), true);
	}
}

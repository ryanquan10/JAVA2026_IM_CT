
package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 钱包信息vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
public class WalletVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1528676211597035126L;

	/**
	 * uid-Y
	 */
	private Integer uid;

	/**
	 * 钱包id
	 */
	private String walletid;

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
	public static WalletVo toBean(Map<String, Object> walletMap) {
		return BeanUtil.fillBeanWithMap(walletMap, new WalletVo(), true);
	}
}


package org.tio.sitexxx.service.vo;

import java.io.Serializable;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;

/**
 * 提现查询 vo
 * @author lixinji
 * 2020年11月2日 下午7:07:44
 */
/**
 * 
 * @author lixinji
 * 2020年11月25日 上午10:52:42
 */
public class WithholdQueryVo implements Serializable {

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
	 * 序列号
	 */
	private String serialnumber;

	/**
	 * 请求id
	 */
	private String reqid;

	/**
	 * 提现id
	 */
	private Integer wid;

	public Integer getWid() {
		return wid;
	}

	public void setWid(Integer wid) {
		this.wid = wid;
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

	public String getSerialnumber() {
		return serialnumber;
	}

	public void setSerialnumber(String serialnumber) {
		this.serialnumber = serialnumber;
	}

	public String getReqid() {
		return reqid;
	}

	public void setReqid(String reqid) {
		this.reqid = reqid;
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
	public static WithholdQueryVo toBean(Map<String, Object> map) {
		return BeanUtil.fillBeanWithMap(map, new WithholdQueryVo(), true);
	}
}

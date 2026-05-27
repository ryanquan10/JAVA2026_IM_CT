
/**
 *
 */
package org.tio.mg.im.common.bs;

import java.io.Serializable;
import java.util.Date;



/**
 * 
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class P2PAlreadyReadNtf implements Serializable {

	private static final long serialVersionUID = -5146467582178818159L;

	/**
	 * @param touid
	 * @author tanyaowu
	 */
	public P2PAlreadyReadNtf(Integer touid, Date readtime) {
		super();
		this.touid = touid;
		this.readtime = readtime;
	}

	/**
	 * 私聊的to userid
	 */
	private Integer	touid;
	/**
	 * 阅读时间
	 */
	private Date	readtime;

	/**
	 * @return the touid
	 */
	public Integer getTouid() {
		return touid;
	}

	/**
	 * @param touid the touid to set
	 */
	public void setTouid(Integer touid) {
		this.touid = touid;
	}

	public Date getReadtime() {
		return readtime;
	}

	public void setReadtime(Date readtime) {
		this.readtime = readtime;
	}

}

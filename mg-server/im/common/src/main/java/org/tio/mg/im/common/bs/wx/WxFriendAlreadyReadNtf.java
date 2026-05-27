
/**
 *
 */
package org.tio.mg.im.common.bs.wx;

import java.io.Serializable;
import java.util.Date;



/**
 * 
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxFriendAlreadyReadNtf implements Serializable {

	private static final long serialVersionUID = -5146467582178818159L;

	/**
	 * @param uid
	 * @author tanyaowu
	 */
	public WxFriendAlreadyReadNtf(Integer uid, Date readtime) {
		super();
		this.setUid(uid);
		this.readtime = readtime;
	}

	/**
	 * 聊天对方的userid
	 */
	private Integer	uid;
	/**
	 * 阅读时间
	 */
	private Date	readtime;

	public Date getReadtime() {
		return readtime;
	}

	public void setReadtime(Date readtime) {
		this.readtime = readtime;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

}

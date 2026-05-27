
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 消息名片对象
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxMsgCardVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500652530550301909L;

	/**
	 * 名片类型
	 */
	private Short cardtype;

	/**
	 * 名片的业务id
	 */
	private Long bizid;

	/**
	 * 名片的名称
	 */
	private String bizname;

	/**
	 * 名片的头像
	 */
	private String bizavatar;

	/**
	 * 分享的用户
	 */
	private Integer shareFromUid;

	/**
	 * 分享到的群/用户
	 */
	private Long shareToBizid;

	public Short getCardtype() {
		return cardtype;
	}

	public void setCardtype(Short cardtype) {
		this.cardtype = cardtype;
	}

	public Long getBizid() {
		return bizid;
	}

	public void setBizid(Long bizid) {
		this.bizid = bizid;
	}

	public String getBizname() {
		return bizname;
	}

	public void setBizname(String bizname) {
		this.bizname = bizname;
	}

	public String getBizavatar() {
		return bizavatar;
	}

	public void setBizavatar(String bizavatar) {
		this.bizavatar = bizavatar;
	}

	public Integer getShareFromUid() {
		return shareFromUid;
	}

	public void setShareFromUid(Integer shareFromUid) {
		this.shareFromUid = shareFromUid;
	}

	public Long getShareToBizid() {
		return shareToBizid;
	}

	public void setShareToBizid(Long shareToBizid) {
		this.shareToBizid = shareToBizid;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}

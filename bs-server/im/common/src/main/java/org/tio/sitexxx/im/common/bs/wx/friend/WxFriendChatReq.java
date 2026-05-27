
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.friend;

import java.io.Serializable;

/**
 * 新版朋友间的聊天请求-- Client-->Server
 * 
 * @author lixinji 2020年2月13日 下午6:28:03
 */
public class WxFriendChatReq implements Serializable {
	private static final long	serialVersionUID	= 7365945567713702051L;
	private String				c;											// 聊天内容
	//	TODO:lixinji-删除to
	private Integer	to;			// 消息接收者的userid
	private Long	chatlinkid;	// 用户聊天会话

	private Long	cardid;		//名片id
	private Short	cardtype;	//名片类型
	
	private String mapjson;

	private String quotemsgcontent;
	private String quotemid;
	private String quotemsgtype;
	private String quotesrcnick;

	public String getQuotemsgcontent() {
		return quotemsgcontent;
	}

	public void setQuotemsgcontent(String quotemsgcontent) {
		this.quotemsgcontent = quotemsgcontent;
	}

	public String getQuotemid() {
		return quotemid;
	}

	public void setQuotemid(String quotemid) {
		this.quotemid = quotemid;
	}

	public String getQuotemsgtype() {
		return quotemsgtype;
	}

	public void setQuotemsgtype(String quotemsgtype) {
		this.quotemsgtype = quotemsgtype;
	}

	public String getQuotesrcnick() {
		return quotesrcnick;
	}

	public void setQuotesrcnick(String quotesrcnick) {
		this.quotesrcnick = quotesrcnick;
	}

	public String getMapjson() {
		return mapjson;
	}

	public void setMapjson(String mapjson) {
		this.mapjson = mapjson;
	}

	public String getC() {
		return c;
	}

	public Long getCardid() {
		return cardid;
	}

	public void setCardid(Long cardid) {
		this.cardid = cardid;
	}

	public Short getCardtype() {
		return cardtype;
	}

	public void setCardtype(Short cardtype) {
		this.cardtype = cardtype;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Integer getTo() {
		return to;
	}

	public void setTo(Integer to) {
		this.to = to;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}
}

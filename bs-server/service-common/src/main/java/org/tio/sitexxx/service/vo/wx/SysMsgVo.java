
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 系统消息vo
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class SysMsgVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9054873871350615650L;

	public SysMsgVo(String opernick, String msgbody, String tonicks, String key) {
		this.opernick = opernick;
		this.msgbody = msgbody;
		this.tonicks = tonicks;
		this.msgkey = key;
	}

	/**
	 * 操作码
	 */
	private Short opercode;
	
	/**
	 * 原始消息
	 */
	private String srctext;
	
	/**
	 * 原始消息类型
	 */
	private Short srcmsgtype;
	
	/**
	 * 操作昵称
	 */
	private String opernick;

	/**
	 * 消息体
	 */
	private String msgbody;

	/**
	 * 消息key
	 */
	private String msgkey;

	/**
	 * 操作者转义字符
	 */
	public static String OPERESCAPE = "%%%";

	/**
	 * 接收者转义字符
	 */
	public static String TOESCAPE = "###";

	/**
	 * 接收者的分割符
	 */
	public static char splitchar = '、';

	/**
	 * 被操作者
	 */
	private String tonicks;
	
	public Short getSrcmsgtype() {
		return srcmsgtype;
	}

	public void setSrcmsgtype(Short srcmsgtype) {
		this.srcmsgtype = srcmsgtype;
	}

	public String getSrctext() {
		return srctext;
	}

	public void setSrctext(String srctext) {
		this.srctext = srctext;
	}

	public Short getOpercode() {
		return opercode;
	}

	public void setOpercode(Short opercode) {
		this.opercode = opercode;
	}

	public String getOpernick() {
		return opernick;
	}

	public void setOpernick(String opernick) {
		this.opernick = opernick;
	}

	public String getMsgbody() {
		return msgbody;
	}

	public void setMsgbody(String msgbody) {
		this.msgbody = msgbody;
	}

	public String getTonicks() {
		return tonicks;
	}

	public void setTonicks(String tonicks) {
		this.tonicks = tonicks;
	}

	public String toText() {
		return msgbody.replaceAll(TOESCAPE, "\"" + tonicks + "\"").replaceAll(OPERESCAPE, "\"" + opernick + "\"");
	}

	public String getToEscape() {
		return TOESCAPE;
	}

	public String getOperEscape() {
		return OPERESCAPE;
	}

	public char getSplitchar() {
		return splitchar;
	}

	public String getMsgkey() {
		return msgkey;
	}

	public void setMsgkey(String msgkey) {
		this.msgkey = msgkey;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}

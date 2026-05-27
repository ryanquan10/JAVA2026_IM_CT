
package org.tio.mg.im.common.bs.base;

import java.io.Serializable;

/**
 * 基础响应
 * @author xufei
 * 2020年3月13日 上午10:36:27
 */
public abstract class BaseResp implements Serializable {

	private static final long serialVersionUID = 3817788009675426796L;
	
	/**
	 * 响应结果
	 */
	private boolean ok;
	
	/**
	 * 返回消息
	 */
	private String msg;
	
	/**
	 * 返回code
	 */
	private Integer code;
	
	/**
	 * 会话id
	 */
	private Long chatlinkid;
	
	/**
	 * 返回数据
	 */
	private Object data;

	public Object getData() {
		return data;
	}
	
	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}
	
	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	protected void setData(Object data) {
		this.data = data;
	}

	/**
	 * 
	 * @author xufei
	 * 2020年3月13日 上午10:32:56
	 */
	public abstract void returnData(Object object);
}

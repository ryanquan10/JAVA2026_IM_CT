
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//**
     * 
     */
package org.tio.sitexxx.service.pay.exception;

import org.apache.commons.lang3.StringUtils;

/**
 * NCountDemoException
 */
public class NCountException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7121642511651992156L;

	/**
	 * 模块名，抛出异常的类
	 */
	@SuppressWarnings("rawtypes")
	protected Class errClass = null;

	/**
	 * 错误码
	 */
	protected String errCode = null;

	/**
	 * 错误描述信息
	 */
	protected String errMsg = null;

	public NCountException() {
		super();
	}

	public NCountException(String errMsg) {
		super("no errMsg");
		this.errMsg = errMsg;
	}

	public NCountException(String errMsg, Exception e) {
		super("no errMsg", e);
		this.errMsg = errMsg;
	}

	public NCountException(String errCode, String errMsg) {
		super(errMsg);
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public NCountException(String errCode, String errMsg, Exception e) {
		super(errMsg, e);
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	@SuppressWarnings("rawtypes")
	public NCountException(String errCode, Class errClass) {
		super("no errMsg");
		this.errCode = errCode;
		this.errClass = errClass;
	}

	@SuppressWarnings("rawtypes")
	public NCountException(String errCode, String errMsg, Class errClass) {
		super(errMsg);
		this.errCode = errCode;
		this.errMsg = errMsg;
		this.errClass = errClass;
	}

	@SuppressWarnings("rawtypes")
	public NCountException(String errCode, String errMsg, Class errClass, Exception e) {
		super(errMsg, e);
		this.errClass = errClass;
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public String printErrMsg() {
		StringBuffer msg = new StringBuffer("");
		if (StringUtils.isNotBlank(errCode)) {
			msg.append("Exception errCode is [").append(this.errCode).append("];\n");
		}

		if (this.errClass != null) {
			msg.append("Exception Class   is [").append(this.errClass.getName()).append("];\n");
		}

		msg.append("Exception Message is [").append(this.errMsg).append("];\n");

		// 打印错误异常堆栈
		if (getCause() != null) {
			getCause().printStackTrace();
		}

		return msg.toString();
	}

	@SuppressWarnings("rawtypes")
	public Class getErrClass() {
		return errClass;
	}

	public String getErrCode() {
		return errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}
}

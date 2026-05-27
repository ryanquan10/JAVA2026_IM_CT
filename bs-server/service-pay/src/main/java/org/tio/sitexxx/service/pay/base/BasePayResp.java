
package org.tio.sitexxx.service.pay.base;

import java.util.Map;

import cn.hutool.core.util.StrUtil;

/**
 * 请求
 * @param <Req>
 * @author lixinji
 * 2020年11月2日 下午6:53:23
 */
public class BasePayResp {

	/**
	 * 响应数据
	 */
	private Map<String, Object> resp;

	/**
	 * 成功与否
	 */
	private boolean ok;

	/**
	 * 返回状态
	 */
	private String status;

	/**
	 * code码
	 */
	private int code;

	/**
	 * 商户返回code
	 */
	private String merCode;

	/**
	 * 商户返回业务code
	 */
	private String merBizCode;

	/**
	 * 消息
	 */
	private String msg;

	public String getMerBizCode() {
		return merBizCode;
	}

	public void setMerBizCode(String merBizCode) {
		this.merBizCode = merBizCode;
	}

	public Map<String, Object> getResp() {
		return resp;
	}

	public void setResp(Map<String, Object> resp) {
		this.resp = resp;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMerCode() {
		return merCode;
	}

	public void setMerCode(String merCode) {
		this.merCode = merCode;
	}

	public boolean isBizOk() {
		if (!this.ok) {
			return false;
		}
		if (StrUtil.isNotBlank(merBizCode)) {
			return false;
		}
		return true;

	}

	/**
	 * 业务错误信息
	 * @return
	 * @author lixinji
	 * 2021年3月17日 下午5:27:23
	 */
	public String getMerMsg() {
		if (StrUtil.isBlank(this.merBizCode)) {
			return "";
		}
		return this.merBizCode + ":" + this.msg;
	}

	public static BasePayResp fail(String msg, String merCode) {
		BasePayResp payResp = new BasePayResp();
		payResp.setOk(false);
		payResp.setMerCode(merCode);
		payResp.setMsg(msg);
		return payResp;
	}
}


package org.tio.sitexxx.service.pay.base;

import java.util.Map;

import org.tio.http.common.HttpRequest;

/**
 * 请求
 * @author lixinji
 * 2020年11月2日 下午6:53:23
 */
public class BasePayReq {

	/**
	 * @param request
	 */
	public BasePayReq(HttpRequest request) {
		this.request = request;
	}

	/**
	 * 
	 */
	private Map<String, Object> params;

	/**
	 * 
	 */
	private String singleParam;

	/**
	 * 请求的request
	 */
	private HttpRequest request;

	public Map<String, Object> getParams() {
		return params;
	}

	public String getSingleParam() {
		return singleParam;
	}

	public void setSingleParam(String singleParam) {
		this.singleParam = singleParam;
	}

	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}

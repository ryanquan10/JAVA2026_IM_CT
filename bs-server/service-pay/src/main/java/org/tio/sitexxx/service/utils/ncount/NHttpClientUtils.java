
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//*
     * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
     * www.hnapay.com
     */

package org.tio.sitexxx.service.utils.ncount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang3.StringUtils;
import org.tio.sitexxx.service.pay.exception.NCountException;

/**
 * HttpClient工具类
 * 
 */
public class NHttpClientUtils {
	private String	sendEncoding	= "UTF-8";
	private int		retryConnTimes	= 1;
	private int		timeout			= 5 * 60 * 1000;

	/**
	 * HTTP请求
	 *
	 * @param obj
	 * @return
	 * @throws CommunicationException
	 */
	/*  String url=this.notifyBtJumpPath+"?contractId="+contractId+"&inTxnCd="+IntTxnCd._00203.value;*/
	public String submit(Object obj, String url) throws NCountException {
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryConnTimes, false));
		client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(timeout);

		if (StringUtils.isNotEmpty(this.sendEncoding)) {
			method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, sendEncoding);
		}
		method.setRequestHeader("User-Agent", "Rich Powered/1.0");

		if (obj != null) {
			if (obj instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> paraMap = (Map<String, String>) obj;
				for (Iterator<String> iter = paraMap.keySet().iterator(); iter.hasNext();) {
					String key = iter.next();
					String value = "";
					if (paraMap.get(key) != null) {
						value = paraMap.get(key);
					}

					method.addParameter(key, value);
				}
			} else if (obj instanceof byte[]) {
				method.setRequestEntity(new ByteArrayRequestEntity((byte[]) obj));
			} else {
				throw new IllegalArgumentException("submit(Object obj): obj should be Map or byte[]");
			}
		}

		int statusCode = 0;
		byte[] responseBody = null;
		String result = "";
		try {
			statusCode = client.executeMethod(method);
			responseBody = method.getResponseBody();
			result = new String(responseBody, sendEncoding);
		} catch (HttpException e) {
		} catch (IOException e) {
		} finally {
			method.releaseConnection();
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		if (statusCode != HttpStatus.SC_OK) {
			throw new NCountException("", "HTTP状态：" + String.valueOf(statusCode));
		}

		return result;
	}

	public String query(Object obj, String url) throws NCountException {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(url);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(retryConnTimes, false));
		client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
		client.getHttpConnectionManager().getParams().setSoTimeout(timeout);

		if (StringUtils.isNotEmpty(this.sendEncoding)) {
			method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, sendEncoding);
		}
		method.setRequestHeader("User-Agent", "Rich Powered/1.0");
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (obj != null) {
			if (obj instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, String> paraMap = (Map<String, String>) obj;
				for (Iterator<String> iter = paraMap.keySet().iterator(); iter.hasNext();) {
					String key = iter.next();
					String value = "";
					if (paraMap.get(key) != null) {
						value = paraMap.get(key);
					}
					params.add(new NameValuePair(key, value));
				}
			} else if (obj instanceof byte[]) {

			} else {
				throw new IllegalArgumentException("submit(Object obj): obj should be Map or byte[]");
			}
		}

		int statusCode = 0;
		byte[] responseBody = null;
		String result = "";
		try {
			NameValuePair[] temp = new NameValuePair[params.size()];
			method.setQueryString(params.toArray(temp));
			statusCode = client.executeMethod(method);
			responseBody = method.getResponseBody();
			result = new String(responseBody, sendEncoding);
		} catch (HttpException e) {
		} catch (IOException e) {
		} finally {
			method.releaseConnection();
			client.getHttpConnectionManager().closeIdleConnections(0);
		}
		if (statusCode != HttpStatus.SC_OK) {
			throw new NCountException("", "HTTP状态：" + String.valueOf(statusCode));
		}

		return result;
	}

}

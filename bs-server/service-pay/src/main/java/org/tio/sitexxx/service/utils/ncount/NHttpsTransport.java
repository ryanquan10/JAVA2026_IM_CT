
/*
 * 使用本软件请从杭州钛特云有限公司获取授权，其它途径获取本软件的行为皆为侵权行为
 * 黄庆辉-4412********4310
 *//*
     * Copyright (c) 2017. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
     * www.hnapay.com
     */

package org.tio.sitexxx.service.utils.ncount;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.CommunicationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * 使用httpClient形式发送报文
 * 
 */
public class NHttpsTransport {

	private String	url				= null;
	private int		timeout			= 5 * 60 * 1000;
	private int		retryConnTimes	= 5;
	private String	sendEncoding	= null;

	public NHttpsTransport() {
	}

	@SuppressWarnings("rawtypes")
	public Object submit(Object obj) throws CommunicationException {
		CloseableHttpClient httpClient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(retryConnTimes, false)).build();
		HttpPost method = new HttpPost(url);

		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).build();

		if (StringUtils.isNotEmpty(this.sendEncoding)) {
			method.setHeader(HTTP.CONTENT_ENCODING, sendEncoding);
		}
		method.setHeader(HTTP.USER_AGENT, "Rich Powered/1.0");

		method.setConfig(requestConfig);

		if (obj instanceof Map) {
			Map paraMap = (Map) obj;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Iterator iter = paraMap.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				nvps.add(new BasicNameValuePair(key, (String) paraMap.get(key)));
			}

			try {
				method.setEntity(new UrlEncodedFormEntity(nvps, this.sendEncoding));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else if (obj instanceof byte[]) {
			method.setEntity(new ByteArrayEntity((byte[]) obj));
		} else if (obj instanceof String) {
			try {
				StringEntity entity = new StringEntity(obj.toString(), this.sendEncoding);
				if (this.sendEncoding != null) {
					entity.setContentEncoding(this.sendEncoding);
				}
				entity.setContentType("text/xml");
				method.setEntity(entity);
			} catch (Exception e) {
			}
		} else {
			throw new IllegalArgumentException("submit(Object obj): obj should be Map ,String,or byte[]");
		}

		int statusCode = 0;
		String result = "";
		CloseableHttpResponse response = null;
		try {

			response = httpClient.execute(method);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();
			result = EntityUtils.toString(responseEntity, sendEncoding);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (statusCode != HttpStatus.SC_OK) {
			throw new CommunicationException(String.valueOf(statusCode));
		}
		return result;
	}

	@SuppressWarnings("rawtypes")
	public Object query(Object obj) throws CommunicationException {
		CloseableHttpClient httpClient = HttpClients.custom().setRetryHandler(new DefaultHttpRequestRetryHandler(retryConnTimes, false)).build();
		HttpGet method = null;
		if (obj instanceof Map) {
			Map paraMap = (Map) obj;
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			for (Iterator iter = paraMap.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				nvps.add(new BasicNameValuePair(key, (String) paraMap.get(key)));
			}

			try {
				//method.setEntity(new UrlEncodedFormEntity(nvps, this.sendEncoding));
				String str = EntityUtils.toString(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
				method = new HttpGet(url + "?" + str);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (obj instanceof byte[]) {

		} else if (obj instanceof String) {
			method = new HttpGet(url + "?" + obj.toString());
		} else {
			throw new IllegalArgumentException("submit(Object obj): obj should be Map ,String,or byte[]");
		}
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).build();

		if (StringUtils.isNotEmpty(this.sendEncoding)) {
			method.setHeader(HTTP.CONTENT_ENCODING, sendEncoding);
		}
		method.setHeader(HTTP.USER_AGENT, "Rich Powered/1.0");

		method.setConfig(requestConfig);

		int statusCode = 0;
		String result = "";
		CloseableHttpResponse response = null;
		try {

			response = httpClient.execute(method);
			statusCode = response.getStatusLine().getStatusCode();
			HttpEntity responseEntity = response.getEntity();
			result = EntityUtils.toString(responseEntity, sendEncoding);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (statusCode != HttpStatus.SC_OK) {
			throw new CommunicationException(String.valueOf(statusCode));
		}
		return result;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setRetryConnTimes(int retryConnTimes) {
		this.retryConnTimes = retryConnTimes;
	}

	public void setSendEncoding(String sendEncoding) {
		this.sendEncoding = sendEncoding;
	}

}

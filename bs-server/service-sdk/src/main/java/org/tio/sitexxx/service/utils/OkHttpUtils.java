
package org.tio.sitexxx.service.utils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.json.Json;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtils {

	private static Logger log = LoggerFactory.getLogger(OkHttpUtils.class);

	private static MediaType jsonMedia = MediaType.parse("application/json; charset=utf-8");

	/**
	 * 
	 * @param url
	 * @param paramsJson
	 * @return
	 * @throws Exception
	 */
	public static Response postJson(String url, String paramsJson) throws Exception {
		URL urlObject = new URL(url);
		URI uri = urlObject.toURI();
		if("https".equals(uri.getScheme())) {
			return postJsonHttps(url,paramsJson);
		} else {
			return postJsonHttp(url,paramsJson);
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param paramsJson
	 * @return
	 * @throws Exception
	 * 
	 */
	@SuppressWarnings("deprecation")
	public static Response postJsonHttp(String url, String paramsJson) throws Exception {
		try {
			OkHttpClient client = new OkHttpClient();
			RequestBody body = RequestBody.create(jsonMedia, paramsJson);
			Request request = new Request.Builder().url(url).post(body).build();
			Response response = client.newCall(request).execute();
			return response;
		} catch (IOException e) {
			log.error(e.toString(), e);
			throw e;
		}
	}
	
	/**
	 * 
	 * @param url
	 * @param paramsJson
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static Response postJsonHttps(String url, String paramsJson) throws Exception {
		try {
			OkHttpClient client = buildOKHttpClient()
                    .build();
			RequestBody body = RequestBody.create(jsonMedia, paramsJson);
			Request request = new Request.Builder().url(url).post(body).build();
			Response response = client.newCall(request).execute();
			return response;
		} catch (IOException e) {
			log.error(e.toString(), e);
			throw e;
		}
	}

	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static Response get(String url) throws Exception {
		URL urlObject = new URL(url);
		URI uri = urlObject.toURI();
		if("https".equals(uri.getScheme())) {
			return getHttps(url);
		} else {
			return getHttp(url);
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 * 
	 */
	public static Response getHttp(String url) throws Exception {
		try {
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url).build();
			Response response = client.newCall(request).execute();
			return response;
		} catch (IOException e) {
			log.error(e.toString(), e);
			throw e;
		}
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static Response getHttps(String url) throws Exception {
		try {
			OkHttpClient client = buildOKHttpClient()
                    .build();
			Request request = new Request.Builder().url(url).build();
			Response response = client.newCall(request).execute();
			return response;
		} catch (IOException e) {
			log.error(e.toString(), e);
			throw e;
		}
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws Exception
	 * 
	 */
	public static Response post(String url, Map<String, Object> params) throws Exception {
		return postJson(url, Json.toJson(params));
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @param calssName
	 * @return
	 * @throws Exception
	 * 
	 */
	public static <T> T post(String url, String params, Class<T> calssName) throws Exception {
		Response response = postJson(url, params);
		T object = Json.toBean(response.body().toString(), calssName);
		return object;
	}

	/**
	 * 
	 * @param url
	 * @param params
	 * @param calssName
	 * @return
	 * @throws Exception
	 * 
	 */
	public static <T> T post(String url, Map<String, Object> params, Class<T> calssName) throws Exception {
		Response response = post(url, params);
		T object = Json.toBean(response.body().toString(), calssName);
		return object;
	}

	/**
	 * 
	 * @param url
	 * @param calssName
	 * @return
	 * @throws Exception
	 * 
	 */
	public static <T> T get(String url, Class<T> calssName) throws Exception {
		Response response = get(url);
		T object = Json.toBean(response.body().toString(), calssName);
		return object;
	}
	
	/**
	 * @return
	 * @author lixinji
	 * 2022年5月23日 上午10:35:04
	 */
	public static OkHttpClient.Builder buildOKHttpClient() {
		try {
			TrustManager[] trustAllCerts = buildTrustManagers();
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
			builder.hostnameVerifier((hostname, session) -> true);
			return builder;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			e.printStackTrace();
			return new OkHttpClient.Builder();
		}
	}
	
	/**
	 * @return
	 * @author lixinji
	 * 2022年5月23日 上午10:35:06
	 */
	private static TrustManager[] buildTrustManagers() {
		return new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
		} };
	}
	
	public static void main(String[] args) {
	}

}

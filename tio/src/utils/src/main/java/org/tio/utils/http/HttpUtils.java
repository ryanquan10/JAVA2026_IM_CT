/*
 * imphri本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ddzcoe
 */
/**
 * 
 */
package org.tio.utils.http;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.tio.utils.hutool.StrUtil;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author tanyaowu
 */
public class HttpUtils {

    public static final MediaType MEDIATYPE_JSON_UTF8 = MediaType.parse("application/json; charset=utf-8");

    public static final long DEFAULT_READTIMEOUT_IN_SECONDS = 10L;

    /**
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public static Response get(String url) throws Exception {
	return get(url, null);
    }

    // public static final OkHttpClient client = new OkHttpClient();

    public static Response get(String url, Map<String, String> headerMap) throws Exception {
	return get(url, headerMap, DEFAULT_READTIMEOUT_IN_SECONDS);
    }

    /**
     * 
     * @param url
     * @param headerMap
     * @param timeoutInSecond
     * @return
     * @throws Exception
     */
    public static Response get(String url, Map<String, String> headerMap, long timeoutInSecond) throws Exception {
	Builder builder = new Request.Builder().url(url);
	if (headerMap != null) {
	    Headers headers = Headers.of(headerMap);
	    builder.headers(headers);
	}
	builder.get();

	Request request = builder.build();

	OkHttpClient client = new OkHttpClient.Builder().connectTimeout(timeoutInSecond, TimeUnit.SECONDS)
		.readTimeout(timeoutInSecond, TimeUnit.SECONDS).build();

	Response response = client.newCall(request).execute();
	return response;
    }

    /**
     * 
     * @param url
     * @return
     * @throws Exception
     */
    public static Response post(String url) throws Exception {
	return post(url, null);
    }

    /**
     * 
     * @param url
     * @param headerMap
     * @return
     * @throws Exception
     */
    public static Response post(String url, Map<String, String> headerMap) throws Exception {
	return post(url, headerMap, (MediaType) null, null, null, null, null);
    }

    /**
     * 
     * @param url
     * @param headerMap
     * @param paramNames
     * @param paramValues
     * @return
     * @throws Exception
     */
    public static Response post(String url, Map<String, String> headerMap, List<String> paramNames,
	    List<String> paramValues) throws Exception {
	return post(url, headerMap, (MediaType) null, null, null, paramNames, paramValues);
    }

    /**
     * 
     * @param url
     * @param headerMap
     * @param paramMap
     * @return
     * @throws Exception
     */
    public static Response post(String url, Map<String, String> headerMap, Map<String, String> paramMap)
	    throws Exception {
	return post(url, headerMap, (MediaType) null, null, paramMap, null, null);
    }

    private static Response post(String url, Map<String, String> headerMap, MediaType mediaType, String bodyString,
	    Map<String, String> paramMap, List<String> paramNames, List<String> paramValues) throws Exception {
	return post(url, headerMap, mediaType, bodyString, paramMap, paramNames, paramValues,
		DEFAULT_READTIMEOUT_IN_SECONDS);
    }

    /**
     * 
     * @param url
     * @param headerMap
     * @param mediaType
     * @param bodyString
     * @param paramMap
     * @param paramNames
     * @param paramValues
     * @return
     * @throws Exception
     */
    private static Response post(String url, Map<String, String> headerMap, MediaType mediaType, String bodyString,
	    Map<String, String> paramMap, List<String> paramNames, List<String> paramValues, long timeoutInSecond)
	    throws Exception {
	Request.Builder builder = new Request.Builder().url(url);
	if (headerMap != null) {
	    Headers headers = Headers.of(headerMap);
	    builder.headers(headers);
	}

	if (false == StrUtil.isBlank(bodyString)) { // 提交bodyString
	    if (mediaType == null) {
		mediaType = MEDIATYPE_JSON_UTF8;
	    }
	    RequestBody body = RequestBody.create(bodyString, mediaType);// .create(mediaType, bodyString);
	    builder.post(body);
	} else { // 提交form表单
	    FormBody.Builder formBodyBuilder = new FormBody.Builder();
	    if (paramMap != null && paramMap.size() > 0) {
		Set<Entry<String, String>> set = paramMap.entrySet();
		for (Entry<String, String> entry : set) {
		    formBodyBuilder.add(entry.getKey(), entry.getValue());
		}
	    } else if (paramNames != null) {
		int xx = paramNames.size();
		if (xx > 0) {
		    for (int i = 0; i < xx; i++) {
			formBodyBuilder.add(paramNames.get(i), paramValues.get(i));
		    }
		}
	    }
	    RequestBody formBody = formBodyBuilder.build();
	    builder.post(formBody);
	}
	Request request = builder.build();
	OkHttpClient client = new OkHttpClient.Builder().connectTimeout(timeoutInSecond, TimeUnit.SECONDS)
		.readTimeout(timeoutInSecond, TimeUnit.SECONDS).build();

	Response response = client.newCall(request).execute();
	return response;
    }

    /**
     * 
     * @param url
     * @param headerMap
     * @param bodyString
     * @return
     * @throws Exception
     */
    public static Response post(String url, Map<String, String> headerMap, String bodyString) throws Exception {
	return post(url, headerMap, (MediaType) null, bodyString, null, null, null);
    }

    /**
     * 
     */
    public HttpUtils() {

    }

}

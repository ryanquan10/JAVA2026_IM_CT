package org.tio.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FastByteArrayOutputStream;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.StreamProgress;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;

/**
 * 
 */
public class Requests {

    public static final int timeout = 5000;

    public static final Pattern CHARSET_PATTERN = Pattern.compile("charset\\s*=\\s*([a-z0-9-]*)",
	    Pattern.CASE_INSENSITIVE);

    public static final Pattern META_CHARSET_PATTERN = Pattern.compile("<meta[^>]*?charset\\s*=\\s*['\"]?([a-z0-9-]*)",
	    Pattern.CASE_INSENSITIVE);

    public static boolean isHttps(String url) {
	return url.toLowerCase().startsWith("https:");
    }

    public static boolean isHttp(String url) {
	return url.toLowerCase().startsWith("http:");
    }

    public static HttpRequest createRequest(Method method, String url) {
	return new HttpRequest(url).method(method);
    }

    public static HttpRequest createGet(String url) {
	return HttpRequest.get(url);
    }

    public static HttpRequest createPost(String url) {
	return HttpRequest.post(url);
    }

    public static String get(String urlString, Charset customCharset) {
	return HttpRequest.get(urlString).charset(customCharset).execute().body();
    }

    public static String get(String urlString) {
	return get(urlString, timeout);
    }

    public static String get(String urlString, int timeout) {
	return HttpRequest.get(urlString).timeout(timeout).execute().body();
    }

    public static String get(String urlString, Map<String, Object> paramMap) {
	return HttpRequest.get(urlString).form(paramMap).execute().body();
    }

    public static String get(String urlString, Map<String, Object> paramMap, int timeout) {
	return HttpRequest.get(urlString).form(paramMap).timeout(timeout).execute().body();
    }

    public static String post(String urlString, Map<String, Object> paramMap) {
	return post(urlString, paramMap, timeout);
    }

    public static String post(String urlString, Map<String, Object> paramMap, int timeout) {
	return HttpRequest.post(urlString).form(paramMap).timeout(timeout).execute().body();
    }

    public static String post(String urlString, String body) {
	return post(urlString, body, timeout);
    }

    public static String post(String urlString, String body, int timeout) {
	return HttpRequest.post(urlString).timeout(timeout).body(body).execute().body();
    }

    // ----------------------------------------------------------------------------------------
    // download

    public static String downloadString(String url, String customCharsetName) {
	return downloadString(url, CharsetUtil.charset(customCharsetName), null);
    }

    public static String downloadString(String url, Charset customCharset) {
	return downloadString(url, customCharset, null);
    }

    public static String downloadString(String url, Charset customCharset, StreamProgress streamPress) {
	if (StrUtil.isBlank(url)) {
	    throw new NullPointerException("[url] is null!");
	}

	FastByteArrayOutputStream out = new FastByteArrayOutputStream();
	download(url, out, true, streamPress);
	return null == customCharset ? out.toString() : out.toString(customCharset);
    }

    public static long downloadFile(String url, String dest) {
	return downloadFile(url, FileUtil.file(dest));
    }

    public static long downloadFile(String url, File destFile) {
	return downloadFile(url, destFile, null);
    }

    public static long downloadFile(String url, File destFile, int timeout) {
	return downloadFile(url, destFile, timeout, null);
    }

    public static long downloadFile(String url, File destFile, StreamProgress streamProgress) {
	return downloadFile(url, destFile, -1, streamProgress);
    }

    public static long downloadFile(String url, File destFile, int timeout, StreamProgress streamProgress) {
	return requestDownloadFile(url, destFile, timeout).writeBody(destFile, streamProgress);
    }

    public static File downloadFileFromUrl(String url, String dest) {
	return downloadFileFromUrl(url, FileUtil.file(dest));
    }

    public static File downloadFileFromUrl(String url, File destFile) {
	return downloadFileFromUrl(url, destFile, null);
    }

    public static File downloadFileFromUrl(String url, File destFile, int timeout) {
	return downloadFileFromUrl(url, destFile, timeout, null);
    }

    public static File downloadFileFromUrl(String url, File destFile, StreamProgress streamProgress) {
	return downloadFileFromUrl(url, destFile, -1, streamProgress);
    }

    @SuppressWarnings("unused")
    public static File downloadFileFromUrl(String url, File destFile, int timeout, StreamProgress streamProgress) {
	HttpResponse response = requestDownloadFile(url, destFile, timeout);

	final File outFile = response.completeFileNameFromHeader(destFile);
	long writeBytes = response.writeBody(outFile, streamProgress);
	return outFile;
    }

    private static HttpResponse requestDownloadFile(String url, File destFile, int timeout) {
	Assert.notBlank(url, "[url] is blank !");
	Assert.notNull(destFile, "[destFile] is null !");

	final HttpResponse response = HttpRequest.get(url).timeout(timeout).executeAsync();
	if (response.isOk()) {
	    return response;
	}

	throw new RuntimeException("Server response error with status code: [{}]");
    }

    public static long download(String url, OutputStream out, boolean isCloseOut) {
	return download(url, out, isCloseOut, null);
    }

    public static long download(String url, OutputStream out, boolean isCloseOut, StreamProgress streamProgress) {
	if (StrUtil.isBlank(url)) {
	    throw new NullPointerException("[url] is null!");
	}
	if (null == out) {
	    throw new NullPointerException("[out] is null!");
	}

	final HttpResponse response = HttpRequest.get(url).executeAsync();
	if (!response.isOk()) {
	    throw new RuntimeException("Server response error with status code: [{}]");
	}
	return response.writeBody(out, isCloseOut, streamProgress);
    }

    public static byte[] downloadBytes(String url) {
	if (StrUtil.isBlank(url)) {
	    throw new NullPointerException("[url] is null!");
	}

	final HttpResponse response = HttpRequest.get(url).setFollowRedirects(true).executeAsync();
	if (!response.isOk()) {
	    throw new RuntimeException("Server response error with status code: [{}]");
	}
	return response.bodyBytes();
    }

    public static String toParams(Map<String, ?> paramMap) {
	return toParams(paramMap, CharsetUtil.CHARSET_UTF_8);
    }

    public static String toParams(Map<String, Object> paramMap, String charsetName) {
	return toParams(paramMap, CharsetUtil.charset(charsetName));
    }

    public static String toParams(Map<String, ?> paramMap, Charset charset) {
	return URLUtil.buildQuery(paramMap, charset);
    }

    public static String encodeParams(String urlWithParams, Charset charset) {
	if (StrUtil.isBlank(urlWithParams)) {
	    return StrUtil.EMPTY;
	}

	String urlPart = null; // url部分，不包括问号
	String paramPart; // 参数部分
	final int pathEndPos = urlWithParams.indexOf('?');
	if (pathEndPos > -1) {
	    // url + 参数
	    urlPart = StrUtil.subPre(urlWithParams, pathEndPos);
	    paramPart = StrUtil.subSuf(urlWithParams, pathEndPos + 1);
	    if (StrUtil.isBlank(paramPart)) {
		// 无参数，返回url
		return urlPart;
	    }
	} else if (false == StrUtil.contains(urlWithParams, '=')) {
	    // 无参数的URL
	    return urlWithParams;
	} else {
	    // 无URL的参数
	    paramPart = urlWithParams;
	}

	paramPart = normalizeParams(paramPart, charset);

	return StrUtil.isBlank(urlPart) ? paramPart : urlPart + "?" + paramPart;
    }

    public static String normalizeParams(String paramPart, Charset charset) {
	final StrBuilder builder = StrBuilder.create(paramPart.length() + 16);
	final int len = paramPart.length();
	String name = null;
	int pos = 0; // 未处理字符开始位置
	char c; // 当前字符
	int i; // 当前字符位置
	for (i = 0; i < len; i++) {
	    c = paramPart.charAt(i);
	    if (c == '=') { // 键值对的分界点
		if (null == name) {
		    // 只有=前未定义name时被当作键值分界符，否则做为普通字符
		    name = (pos == i) ? StrUtil.EMPTY : paramPart.substring(pos, i);
		    pos = i + 1;
		}
	    } else if (c == '&') { // 参数对的分界点
		if (pos != i) {
		    if (null == name) {
			// 对于像&a&这类无参数值的字符串，我们将name为a的值设为""
			name = paramPart.substring(pos, i);
			builder.append(URLUtil.encodeQuery(name, charset)).append('=');
		    } else {
			builder.append(URLUtil.encodeQuery(name, charset)).append('=')
				.append(URLUtil.encodeQuery(paramPart.substring(pos, i), charset)).append('&');
		    }
		    name = null;
		}
		pos = i + 1;
	    }
	}

	// 结尾处理
	if (null != name) {
	    builder.append(URLUtil.encodeQuery(name, charset)).append('=');
	}
	if (pos != i) {
	    if (null == name && pos > 0) {
		builder.append('=');
	    }
	    builder.append(URLUtil.encodeQuery(paramPart.substring(pos, i), charset));
	}

	// 以&结尾则去除之
	int lastIndex = builder.length() - 1;
	if ('&' == builder.charAt(lastIndex)) {
	    builder.delTo(lastIndex);
	}
	return builder.toString();
    }

    @Deprecated
    public static Map<String, String> decodeParamMap(String paramsStr, String charset) {
	return decodeParamMap(paramsStr, CharsetUtil.charset(charset));
    }

    public static Map<String, String> decodeParamMap(String paramsStr, Charset charset) {
	final Map<CharSequence, CharSequence> queryMap = UrlQuery.of(paramsStr, charset).getQueryMap();
	if (MapUtil.isEmpty(queryMap)) {
	    return MapUtil.empty();
	}
	return Convert.toMap(String.class, String.class, queryMap);
    }

    public static Map<String, List<String>> decodeParams(String paramsStr, String charset) {
	return decodeParams(paramsStr, CharsetUtil.charset(charset));
    }

    public static Map<String, List<String>> decodeParams(String paramsStr, Charset charset) {
	final Map<CharSequence, CharSequence> queryMap = UrlQuery.of(paramsStr, charset).getQueryMap();
	if (MapUtil.isEmpty(queryMap)) {
	    return MapUtil.empty();
	}

	final Map<String, List<String>> params = new LinkedHashMap<>();
	queryMap.forEach((key, value) -> {
	    final List<String> values = params.computeIfAbsent(StrUtil.str(key), k -> new ArrayList<>(1));
	    // 一般是一个参数
	    values.add(StrUtil.str(value));
	});
	return params;
    }

    public static String urlWithForm(String url, Map<String, Object> form, Charset charset, boolean isEncodeParams) {
	if (isEncodeParams && StrUtil.contains(url, '?')) {
	    // 在需要编码的情况下，如果url中已经有部分参数，则编码之
	    url = encodeParams(url, charset);
	}

	// url和参数是分别编码的
	return urlWithForm(url, toParams(form, charset), charset, false);
    }

    public static String urlWithForm(String url, String queryString, Charset charset, boolean isEncode) {
	if (StrUtil.isBlank(queryString)) {
	    // 无额外参数
	    if (StrUtil.contains(url, '?')) {
		// url中包含参数
		return isEncode ? encodeParams(url, charset) : url;
	    }
	    return url;
	}

	// 始终有参数
	final StrBuilder urlBuilder = StrBuilder.create(url.length() + queryString.length() + 16);
	int qmIndex = url.indexOf('?');
	if (qmIndex > 0) {
	    // 原URL带参数，则对这部分参数单独编码（如果选项为进行编码）
	    urlBuilder.append(isEncode ? encodeParams(url, charset) : url);
	    if (false == StrUtil.endWith(url, '&')) {
		// 已经带参数的情况下追加参数
		urlBuilder.append('&');
	    }
	} else {
	    // 原url无参数，则不做编码
	    urlBuilder.append(url);
	    if (qmIndex < 0) {
		// 无 '?' 追加之
		urlBuilder.append('?');
	    }
	}
	urlBuilder.append(isEncode ? encodeParams(queryString, charset) : queryString);
	return urlBuilder.toString();
    }

    public static String getCharset(HttpURLConnection conn) {
	if (conn == null) {
	    return null;
	}
	return getCharset(conn.getContentType());
    }

    public static String getCharset(String contentType) {
	if (StrUtil.isBlank(contentType)) {
	    return null;
	}
	return ReUtil.get(CHARSET_PATTERN, contentType, 1);
    }

    public static String getString(InputStream in, Charset charset, boolean isGetCharsetFromContent) {
	final byte[] contentBytes = IoUtil.readBytes(in);
	return getString(contentBytes, charset, isGetCharsetFromContent);
    }

    public static String getString(byte[] contentBytes, Charset charset, boolean isGetCharsetFromContent) {
	if (null == contentBytes) {
	    return null;
	}

	if (null == charset) {
	    charset = CharsetUtil.CHARSET_UTF_8;
	}
	String content = new String(contentBytes, charset);
	if (isGetCharsetFromContent) {
	    final String charsetInContentStr = ReUtil.get(META_CHARSET_PATTERN, content, 1);
	    if (StrUtil.isNotBlank(charsetInContentStr)) {
		Charset charsetInContent = null;
		try {
		    charsetInContent = Charset.forName(charsetInContentStr);
		} catch (Exception e) {
		    if (StrUtil.containsIgnoreCase(charsetInContentStr, "utf-8")
			    || StrUtil.containsIgnoreCase(charsetInContentStr, "utf8")) {
			charsetInContent = CharsetUtil.CHARSET_UTF_8;
		    } else if (StrUtil.containsIgnoreCase(charsetInContentStr, "gbk")) {
			charsetInContent = CharsetUtil.CHARSET_GBK;
		    }
		    // ignore
		}
		if (null != charsetInContent && false == charset.equals(charsetInContent)) {
		    content = new String(contentBytes, charsetInContent);
		}
	    }
	}
	return content;
    }

    public static String getMimeType(String filePath, String defaultValue) {
	return ObjectUtil.defaultIfNull(getMimeType(filePath), defaultValue);
    }

    public static String getMimeType(String filePath) {
	return FileUtil.getMimeType(filePath);
    }

    public static String buildBasicAuth(String username, String password, Charset charset) {
	final String data = username.concat(":").concat(password);
	return "Basic " + Base64.encode(data, charset);
    }
}

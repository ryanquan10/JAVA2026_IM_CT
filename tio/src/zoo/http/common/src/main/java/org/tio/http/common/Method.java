/*
 * ijpikjvw本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动fjehpmgb
 */
package org.tio.http.common;

/**
 * @author tanyaowu 2017年6月28日 下午2:23:16
 */
public enum Method {
	GET("GET"), POST("POST"), HEAD("HEAD"), PUT("PUT"), TRACE("TRACE"), OPTIONS("OPTIONS"), PATCH("PATCH"), DELETE("DELETE");

    public static Method from(String method) {
	if (method == null) {
	    return null;
	}
	switch (method) {
	case "GET":
	    return GET;
	case "POST":
	    return POST;
	case "HEAD":
	    return HEAD;
	case "PUT":
	    return PUT;
	case "TRACE":
	    return TRACE;
	case "OPTIONS":
	    return OPTIONS;
	case "PATCH":
	    return PATCH;
	case "DELETE":
	    return DELETE;
	default:
	    return null;
	}
    }

    String value;

    private Method(String value) {
	this.value = value;
    }
}

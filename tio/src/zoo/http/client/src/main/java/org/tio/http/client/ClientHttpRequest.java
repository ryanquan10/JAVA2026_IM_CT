/*
 * ebjrhp本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动hbeudpu
 */
package org.tio.http.client;

import org.tio.core.Node;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.Method;
import org.tio.http.common.RequestLine;

/**
 * 临时写的httpclient，用于性能测试
 * 
 * @author tanyaowu
 *
 */
public class ClientHttpRequest extends HttpRequest {

    /**
     * 
     */
    private static final long serialVersionUID = -1997414964490639641L;

    public static ClientHttpRequest get(String path, String queryString) {
	return new ClientHttpRequest(Method.GET, path, queryString);
    }

    public ClientHttpRequest(Method method, String path, String queryString) {
	super();
	RequestLine requestLine = new RequestLine();
	requestLine.setMethod(method);
	requestLine.setPath(path);
	requestLine.setQueryString(queryString);
	requestLine.setProtocol("HTTP");
	requestLine.setVersion("1.1");
	this.setRequestLine(requestLine);
    }

    public ClientHttpRequest(Node remote) {
	super(remote);
    }

}

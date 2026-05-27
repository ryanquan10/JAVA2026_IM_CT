/*
 * gzkwfclesmgyw本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xomarywccnaerl
 */
package org.tio.websocket.common;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tanyaowu 2017年7月30日 上午10:09:59
 */
public class WsResponse extends WsPacket {
    private static Logger log = LoggerFactory.getLogger(WsResponse.class);

    private static final long serialVersionUID = 963847148301021559L;

    public static WsResponse fromBytes(byte[] bytes) {
	WsResponse wsResponse = new WsResponse();
	wsResponse.setBody(bytes);
	wsResponse.setWsOpcode(Opcode.BINARY);
	return wsResponse;
    }

    public static WsResponse fromText(String text, String charset) {
	WsResponse wsResponse = new WsResponse();
	try {
	    wsResponse.setBody(text.getBytes(charset));
	} catch (UnsupportedEncodingException e) {
	    log.error("", e);
	}
	wsResponse.setWsOpcode(Opcode.TEXT);
	return wsResponse;
    }
}

/*
 * vmupmfg本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pvdnqmnbahds
 */
package org.tio.websocket.common;

import java.io.UnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tanyaowu 2017年7月30日 上午10:09:46
 */
public class WsRequest extends WsPacket {
    private static final Logger log = LoggerFactory.getLogger(WsRequest.class);

    private static final long serialVersionUID = -3361865570708714596L;

    public static WsRequest fromBytes(byte[] bytes) {
	WsRequest wsRequest = new WsRequest();
	wsRequest.setBody(bytes);
	wsRequest.setWsEof(true);
	wsRequest.setWsOpcode(Opcode.BINARY);
	return wsRequest;
    }

    public static WsRequest fromText(String text, String charset) {
	WsRequest wsRequest = new WsRequest();
	try {
	    wsRequest.setBody(text.getBytes(charset));
	} catch (UnsupportedEncodingException e) {
	    log.error("", e);
	}
	wsRequest.setWsEof(true);
	wsRequest.setWsOpcode(Opcode.TEXT);
	return wsRequest;
    }
}

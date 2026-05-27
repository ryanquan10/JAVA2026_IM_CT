/*
 * bvyzawgrr本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rayeuisqiy
 */
package org.tio.websocket.client.event;

// ref: https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
public class CloseEvent implements WsEvent {
    public final int code;
    public final String reason;
    public final boolean wasClean;

    public CloseEvent(int code, String reason, boolean wasClean) {
	this.code = code;
	this.reason = reason;
	this.wasClean = wasClean;
    }
}

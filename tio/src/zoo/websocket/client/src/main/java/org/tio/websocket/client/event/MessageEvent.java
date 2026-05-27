/*
 * yabaihhbv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动cucyzskvj
 */
package org.tio.websocket.client.event;

import org.tio.websocket.common.WsPacket;

public class MessageEvent implements WsEvent {
    public final WsPacket data;

    public MessageEvent(WsPacket data) {
	this.data = data;
    }
}

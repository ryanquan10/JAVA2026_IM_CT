/*
 * drvxniccy本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jpttfmwohhxo
 */
package org.tio.websocket.client.config;

import java.util.function.Consumer;

import org.tio.utils.hutool.StrUtil;
import org.tio.websocket.client.event.CloseEvent;
import org.tio.websocket.client.event.ErrorEvent;
import org.tio.websocket.client.event.MessageEvent;
import org.tio.websocket.client.event.OpenEvent;

public class WsClientConfig {
    private String charset = "UTF-8";
    private Consumer<CloseEvent> onClose;
    private Consumer<ErrorEvent> onError;
    private Consumer<MessageEvent> onMessage;
    private Consumer<OpenEvent> onOpen;
    private Consumer<Throwable> onThrows;

    public WsClientConfig() {
    }

    public WsClientConfig(Consumer<MessageEvent> onMessage) {
	this.onMessage = onMessage;
    }

    public WsClientConfig(Consumer<MessageEvent> onMessage, Consumer<ErrorEvent> onError) {
	this.onError = onError;
	this.onMessage = onMessage;
    }

    public WsClientConfig(Consumer<MessageEvent> onMessage, Consumer<ErrorEvent> onError,
	    Consumer<CloseEvent> onClose) {
	this.onClose = onClose;
	this.onError = onError;
	this.onMessage = onMessage;
    }

    public WsClientConfig(Consumer<MessageEvent> onMessage, Consumer<ErrorEvent> onError, Consumer<CloseEvent> onClose,
	    Consumer<Throwable> onThrows) {
	this.onError = onError;
	this.onMessage = onMessage;
	this.onClose = onClose;
	this.onThrows = onThrows;
    }

    public WsClientConfig(Consumer<OpenEvent> onOpen, Consumer<MessageEvent> onMessage, Consumer<CloseEvent> onClose,
	    Consumer<ErrorEvent> onError, Consumer<Throwable> onThrows) {
	this.charset = StrUtil.isBlank(charset) ? "UTF-8" : charset;
	this.onClose = onClose;
	this.onError = onError;
	this.onMessage = onMessage;
	this.onOpen = onOpen;
	this.onThrows = onThrows;
    }

    public WsClientConfig(String charset) {
	this.charset = charset;
    }

    public String getCharset() {
	return charset;
    }

    public Consumer<CloseEvent> getOnClose() {
	return onClose;
    }

    public Consumer<ErrorEvent> getOnError() {
	return onError;
    }

    // public void setCharset(String charset) {
    // this.charset = charset;
    // }

    public Consumer<MessageEvent> getOnMessage() {
	return onMessage;
    }

    public Consumer<OpenEvent> getOnOpen() {
	return onOpen;
    }

    public Consumer<Throwable> getOnThrows() {
	return onThrows;
    }

    public void setOnClose(Consumer<CloseEvent> onClose) {
	this.onClose = onClose;
    }

    public void setOnError(Consumer<ErrorEvent> onError) {
	this.onError = onError;
    }

    public void setOnMessage(Consumer<MessageEvent> onMessage) {
	this.onMessage = onMessage;
    }

    public void setOnOpen(Consumer<OpenEvent> onOpen) {
	this.onOpen = onOpen;
    }

    public void setOnThrows(Consumer<Throwable> onThrows) {
	this.onThrows = onThrows;
    }
}

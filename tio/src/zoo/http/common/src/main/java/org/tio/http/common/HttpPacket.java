/*
 * bbfkrxbxqec本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动sjqxbocttihiv
 */
package org.tio.http.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.tio.core.intf.Packet;
import org.tio.utils.SysConst;

/**
 *
 * @author tanyaowu
 *
 */
public class HttpPacket extends Packet {
    private static final long serialVersionUID = 3903186670675671956L;
    private static final AtomicLong ID_ATOMICLONG = new AtomicLong();
    private Map<String, Serializable> props = new HashMap<>();
    protected byte[] body;
    private String headerString = SysConst.BLANK;
    private Long id = ID_ATOMICLONG.incrementAndGet();

    public HttpPacket() {

    }

    /**
     * 获取属性
     * 
     * @param key
     * @return
     * @author tanyaowu
     */
    public Object getAttribute(String key) {
	return props.get(key);
    }

    /**
     * 
     * @param key
     * @param defaultValue
     * @return
     * @author tanyaowu
     */
    public Object getAttribute(String key, Serializable defaultValue) {
	Serializable ret = props.get(key);
	if (ret == null) {
	    return defaultValue;
	}
	return ret;
    }

    /**
     * @return the body
     */
    public byte[] getBody() {
	return body;
    }

    public String getHeaderString() {
	return headerString;
    }

    /**
     * @return the id
     */
    public Long getId() {
	return id;
    }

    /**
     * 
     * @param key
     * @author tanyaowu
     */
    public void removeAttribute(String key) {
	props.remove(key);
    }

    /**
     * 设置属性
     * 
     * @param key
     * @param value
     * @author tanyaowu
     */
    public void setAttribute(String key, Serializable value) {
	props.put(key, value);
    }

    public void setBody(byte[] body) {
	this.body = body;
    }

    public void setHeaderString(String headerString) {
	this.headerString = headerString;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
	this.id = id;
    }
    
}

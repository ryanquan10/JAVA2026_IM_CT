/*
 * acsornmv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动kaudqjmngs
 */
package org.tio.core;

import java.util.Objects;

import org.tio.utils.hutool.StrUtil;

/**
 * 
 * @author tanyaowu 2017年10月19日 上午9:40:07
 */
public class Node implements Comparable<Node>, java.io.Serializable {
    private static final long serialVersionUID = 1866316757333121660L;
    public static void main(String[] args) {
	String ip = "12.12.12.12";
	int port = 90;
	Node n1 = new Node(ip, port);
	Node n2 = new Node(ip, port);

	System.out.println(Objects.equals(n1, n2));
    }
    private String ip;
    private int port;
    /**
     * 是否使用了ssl，1：使用了；2：没有使用
     */
    private Byte ssl = 1;
    /**
     * 心跳超时时间，单位：毫秒
     */
    private Long timeout = null;

    private String protocol = null;

    public Node() {
    }

    public Node(String ip, int port) {
	this();
	if (StrUtil.isBlank(ip)) {
	    ip = "0.0.0.0";
	}

	this.setIp(ip);
	this.setPort(port);
    }

    public Node(String ip, int port, boolean useSsl) {
	this(ip, port);
	this.setSsl(useSsl ? (byte) 1 : (byte) 0);
    }

    @Override
    public int compareTo(Node other) {
	if (other == null) {
	    return -1;
	}
	// RemoteNode other = (RemoteNode) obj;

	if (Objects.equals(ip, other.getIp()) && Objects.equals(port, other.getPort())) {
	    return 0;
	} else {
	    return this.toString().compareTo(other.toString());
	}
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	Node other = (Node) obj;
	return ip.equals(other.getIp()) && port == other.getPort();
    }

    public String getIp() {
	return ip;
    }

    public int getPort() {
	return port;
    }

    public String getProtocol() {
	return protocol;
    }

    /**
     * @return the ssl
     */
    public Byte getSsl() {
	return ssl;
    }

    @Override
    public int hashCode() {
	return (ip + ":" + port).hashCode();
    }

    public void setIp(String ip) {
	this.ip = ip;
    }

    public void setPort(int port) {
	this.port = port;
    }

    public void setProtocol(String protocol) {
	this.protocol = protocol;
    }

    /**
     * @param ssl the ssl to set
     */
    public void setSsl(Byte ssl) {
	this.ssl = ssl;
    }

    @Override
    public String toString() {
	StringBuilder builder = new StringBuilder();
	builder.append(ip).append(":").append(port);
	return builder.toString();
    }

    /**
     * @return the timeout
     */
    public Long getTimeout() {
	return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(Long timeout) {
	this.timeout = timeout;
    }
}

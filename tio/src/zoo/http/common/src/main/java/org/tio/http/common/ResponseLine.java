/*
 * yhwpjcubbgp本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动sulhx
 */
/**
 * 
 */
package org.tio.http.common;

/**
 * @author tanyaowu
 *
 */
public class ResponseLine {
    public String protocol;
    public String version;
    public Integer status;
    public String desc;
    // public byte[] bytes;

    // public static ResponseLine

    public ResponseLine(String protocol, String version, Integer status, String desc) {
	super();
	this.protocol = protocol;
	this.version = version;
	this.status = status;
	this.desc = desc;

	// StringBuilder sb = new StringBuilder(32);
	// sb.append(protocol);
	// sb.append("/");
	// sb.append(version);
	// sb.append(SysConst.SPACE);
	// sb.append(status);
	// sb.append(SysConst.SPACE);
	// sb.append(desc);
	// this.bytes = sb.toString().getBytes();
    }

}

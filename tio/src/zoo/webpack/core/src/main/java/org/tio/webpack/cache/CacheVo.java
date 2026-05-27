/*
 * belhunehidb本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动txrakcskcd
 */
package org.tio.webpack.cache;

import java.io.Serializable;

/**
 * @author tanyaowu 2017年11月20日 上午11:00:24
 */
public class CacheVo implements Serializable {
    private static final long serialVersionUID = -1693751347296834323L;

    private String path;

    private byte[] initBytes;

    private byte[] compressedBytes;

    /**
     * 
     * @author tanyaowu
     */
    public CacheVo() {
    }

    // private String initStr;

    // private String compressedStr;

    public byte[] getCompressedBytes() {
	return compressedBytes;
    }

    public byte[] getInitBytes() {
	return initBytes;
    }

    public String getPath() {
	return path;
    }

    public void setCompressedBytes(byte[] compressedBytes) {
	this.compressedBytes = compressedBytes;
    }

    public void setInitBytes(byte[] initBytes) {
	this.initBytes = initBytes;
    }

    public void setPath(String path) {
	this.path = path;
    }

    // /**
    // * @return the initStr
    // */
    // public String getInitStr() {
    // return initStr;
    // }
    //
    // /**
    // * @param initStr the initStr to set
    // */
    // public void setInitStr(String initStr) {
    // this.initStr = initStr;
    // }

    // /**
    // * @return the compressedStr
    // */
    // public String getCompressedStr() {
    // return compressedStr;
    // }
    //
    // /**
    // * @param compressedStr the compressedStr to set
    // */
    // public void setCompressedStr(String compressedStr) {
    // this.compressedStr = compressedStr;
    // }

}

/*
 * iksdddu本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动bylud
 */
package org.tio.webpack.compress;

import org.tio.utils.SysConst;

/**
 * @author tanyaowu 2017年11月20日 上午11:03:45
 */
public interface ResCompressor {

    String DOC = "\r\n1、t-io提供压缩能力" + "\r\n2、不仅仅是百万级网络编程框架 ： https://www.tiocloud.com" + SysConst.CRLF;

    /**
     * 
     * @param filePath
     * @param initStr  原内容
     * @return 压缩后的内容
     * @author tanyaowu
     */
    public String compress(String filePath, String initStr);
}

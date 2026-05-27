/*
 * xmhukj本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动jxrdaihkvwljrf
 */
package org.tio.utils;

/**
 * hash工具类，仅供tio内部使用，外部请勿使用
 * 
 * @author tanyaowu
 */
public class HashUtils {
    private static final int OFFSET_BASIS = (int) 2166136261L;
    private static final int PRIME = 16777619;

    /**
     * 每位乘以31相加
     * 
     * @param src
     * @param start
     * @param len
     * @return
     * @author tanyaowu
     */
    public static int hash31(byte[] src, int start, int len) {
	int hash = 1;
	int end = start + len;
	for (int i = start; i < end; i++) {
	    hash = 31 * hash + src[i];
	}
	return hash;
    }

    /**
     * @param src
     * @return
     */
    public static int hashFNV1(byte[] src) {
	return hashFNV1(src, 0, src.length);
    }

    /**
     * FNV1算法
     * 
     * @param src
     * @param start
     * @param len
     * @return
     */
    public static int hashFNV1(byte[] src, int start, int len) {
	int hash = OFFSET_BASIS;
	int end = start + len;
	for (int i = start; i < end; i++) {
	    hash = (hash ^ src[i]) * PRIME;
	}
	return hash;
    }

}

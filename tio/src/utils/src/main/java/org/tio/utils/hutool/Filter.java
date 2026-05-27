/*
 * xuoumwfol本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动hdzah
 */
package org.tio.utils.hutool;

/**
 * 过滤器接口
 * 
 * @author Looly
 *
 */
public interface Filter<T> {
    /**
     * 是否接受对象
     * 
     * @param t 检查的对象
     * @return 是否接受对象
     */
    boolean accept(T t);
}
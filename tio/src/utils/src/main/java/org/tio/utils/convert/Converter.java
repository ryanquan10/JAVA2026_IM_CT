/*
 * cdmpgmp本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vetriklziipkf
 */
/**
 * 
 */
package org.tio.utils.convert;

/**
 * @author tanyaowu 从F类型转到T类型
 */
public interface Converter<T> {
    /**
     * 
     * @param value
     * @return
     */
    public T convert(Object value);

}

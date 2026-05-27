/*
 * xdfzqywrw本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动hblfheawtbmaxh
 */
package org.tio.core.exception;

/**
 * 
 * @author tanyaowu 2017年4月1日 上午9:33:24
 */
public class LengthOverflowException extends java.lang.Throwable {

    private static final long serialVersionUID = 5231789012657669073L;

    /**
     * 
     *
     * @author: tanyaowu
     * 
     */
    public LengthOverflowException() {
    }

    /**
     * @param message
     *
     * @author: tanyaowu
     * 
     */
    public LengthOverflowException(String message) {
	super(message);

    }

    /**
     * @param message
     * @param cause
     *
     * @author: tanyaowu
     * 
     */
    public LengthOverflowException(String message, Throwable cause) {
	super(message, cause);

    }

    /**
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     *
     * @author: tanyaowu
     * 
     */
    public LengthOverflowException(String message, Throwable cause, boolean enableSuppression,
	    boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);

    }

    /**
     * @param cause
     *
     * @author: tanyaowu
     * 
     */
    public LengthOverflowException(Throwable cause) {
	super(cause);

    }

}

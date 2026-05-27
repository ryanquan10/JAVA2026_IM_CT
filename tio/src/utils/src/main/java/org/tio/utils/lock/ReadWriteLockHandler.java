/*
 * qjpwl本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动bxvlrfyoi
 */
package org.tio.utils.lock;

/**
 * @author tanyaowu
 */
public interface ReadWriteLockHandler {

    // public static class ReadWriteRet {
    // public Object readRet;
    // public Object writeRet;
    // /**
    // * 是不是运行了read方法
    // */
    // public boolean isReadRunned = false;
    // /**
    // * 是不是运行了write方法
    // */
    // public boolean isWriteRunned = false;
    // }

    // /**
    // *
    // * @return
    // */
    // public void read() throws Exception;

    /**
     * 
     * @return
     */
    public void write() throws Exception;
}

/*
 * zzhkkwkfmds本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vfzgnga
 */
package org.tio.core.intf;

import java.util.concurrent.CountDownLatch;

/**
 * @author tanyaowu 2020年9月2日 下午2:10:46
 */
public class PacketMeta implements java.io.Serializable {

    private static final long serialVersionUID = 7095735340598265148L;
    private Boolean isSentSuccess = false;
    private CountDownLatch countDownLatch = null;

    public CountDownLatch getCountDownLatch() {
	return countDownLatch;
    }

    public Boolean getIsSentSuccess() {
	return isSentSuccess;
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
	this.countDownLatch = countDownLatch;
    }

    public void setIsSentSuccess(Boolean isSentSuccess) {
	this.isSentSuccess = isSentSuccess;
    }

}
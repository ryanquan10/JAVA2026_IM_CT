/*
 * poiucaflqda本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动chtnirrqjb
 */
package org.tio.utils.thread.pool;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.utils.queue.FullWaitQueue;

/**
 *
 * @author tanyaowu 2017年4月4日 上午9:23:12
 */
public abstract class AbstractQueueRunnable<T> extends AbstractSynRunnable {
    private static final Logger log = LoggerFactory.getLogger(AbstractQueueRunnable.class);

    public AbstractQueueRunnable(Executor executor) {
	super(executor);
    }

    /**
     * @return
     *
     */
    public boolean addMsg(T t) {
	if (this.isCanceled()) {
	    log.error("任务已经取消");
	    return false;
	}

	return getMsgQueue().add(t);
    }

    /**
     * 清空处理的队列消息
     */
    public void clearMsgQueue() {
	if (getMsgQueue() != null) {
	    getMsgQueue().clear();
	}
    }

    /**
     * 获取消息队列
     * 
     * @return
     */
    public abstract FullWaitQueue<T> getMsgQueue();

    @Override
    public boolean isNeededExecute() {
	return (getMsgQueue() != null && !getMsgQueue().isEmpty()) && !this.isCanceled();
    }
}

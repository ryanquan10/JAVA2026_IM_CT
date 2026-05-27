/*
 * bhrurewwhtcfe本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ebdmvvxnnr
 */
package org.tio.utils.queue;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 暂时用ConcurrentLinkedQueue代替
 * 
 * @author tanyaowu 2019年9月30日 上午9:22:00
 */
public class TioFullWaitQueue<T> implements FullWaitQueue<T> {

    private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<>();

    /**
     * 
     * @param capacity
     * @param useSingleProducer
     * @author tanyaowu
     */
    public TioFullWaitQueue(Integer capacity, boolean useSingleProducer) {
    }

    @Override
    public boolean add(T e) {
	return queue.add(e);
    }

    @Override
    public void clear() {
	queue.clear();
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public boolean isEmpty() {
	return queue.isEmpty();
    }

    @Override
    public T poll() {
	return queue.poll();
    }

    @Override
    public int size() {
	return queue.size();
    }

}

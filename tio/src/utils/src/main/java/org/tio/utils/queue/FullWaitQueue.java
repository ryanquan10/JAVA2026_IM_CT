/*
 * kapqvisoq本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xbvpj
 */
package org.tio.utils.queue;

/**
 * 满员等待队列
 * 
 * @author tanyaowu 2019年9月28日 上午9:36:45
 */
public interface FullWaitQueue<T> {
    /**
     * write 向队列尾添加一个元素，如果队列已经满了，则等待一段时间
     * 
     * @param t
     * @return
     * @author tanyaowu
     */
    public boolean add(T t);

    public void clear();

    public boolean isEmpty();

    /**
     * read Retrieves and removes the head of this queue, or returns {@code null} if
     * this queue is empty.
     *
     * @return the head of this queue, or {@code null} if this queue is empty
     */
    public T poll();

    public int size();
}

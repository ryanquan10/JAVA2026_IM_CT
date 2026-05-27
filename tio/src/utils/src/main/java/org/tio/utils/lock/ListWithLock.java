/*
 * tiarhsjrnt本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ftowo
 */
package org.tio.utils.lock;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author tanyaowu 2017年5月14日 上午9:55:37
 */
public class ListWithLock<T> extends CollectionWithLock<List<T>, T> {
    private static final long serialVersionUID = 8549668315606224029L;

    /**
     * @param durationList
     * @author tanyaowu
     */
    public ListWithLock(List<T> list) {
	super(list);
    }

    /**
     * @param durationList
     * @param lock
     * @author tanyaowu
     */
    public ListWithLock(List<T> list, ReentrantReadWriteLock lock) {
	super(list, lock);
    }

}

/*
 * srfibhmyoqed本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动rcmwok
 */
package org.tio.utils.lock;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * MapWithLock<K, SetWithLock<V>>
 * 
 * @author tanyaowu 2020年8月19日 下午5:31:05
 */
public class MapSetWithLock<K, V> extends MapCollectionWithLock<K, V, Set<V>, SetWithLock<V>>
/* ObjWithLock<MapWithLock<K, SetWithLock<V>>> */ {

    private static final long serialVersionUID = 3450500044148462074L;

    public MapSetWithLock(MapWithLock<K, SetWithLock<V>> map) {
	super(map);
    }

    public MapSetWithLock(MapWithLock<K, SetWithLock<V>> map, ReentrantReadWriteLock lock) {
	super(map, lock);
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public SetWithLock<V> newCollection() {
	return new SetWithLock<>(new HashSet<>());
    }

}

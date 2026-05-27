/*
 * fegckk本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动gwsqxfzs
 */
package org.tio.utils.lock;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * MapWithLock<K, ListWithLock<V>>
 * 
 * @author tanyaowu 2020年8月19日 下午5:31:05
 */
public class MapListWithLock<K, V> extends MapCollectionWithLock<K, V, List<V>, ListWithLock<V>>
/* ObjWithLock<MapWithLock<K, ListWithLock<V>>> */ {

    private static final long serialVersionUID = 3450500044148462074L;

    public MapListWithLock(MapWithLock<K, ListWithLock<V>> map) {
	super(map);
    }

    public MapListWithLock(MapWithLock<K, ListWithLock<V>> map, ReentrantReadWriteLock lock) {
	super(map, lock);
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public ListWithLock<V> newCollection() {
	return new ListWithLock<>(new ArrayList<>());
    }

}

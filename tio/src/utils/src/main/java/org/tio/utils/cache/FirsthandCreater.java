/*
 * jahgmstmrkloxd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动hqifozaeohp
 */
/**
 * 
 */
package org.tio.utils.cache;

import java.io.Serializable;

/**
 * 一手对象（即非缓存对象）创建者
 * 
 * @author tanyaowu
 *
 */
public interface FirsthandCreater<T extends Serializable> {

    /**
     * 
     * @return
     * @author tanyaowu
     * @throws Exception
     */
    public T create() throws Exception;

}

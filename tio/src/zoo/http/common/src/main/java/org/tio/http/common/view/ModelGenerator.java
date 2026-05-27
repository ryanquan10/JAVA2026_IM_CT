/*
 * vrsvilthipzui本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xqvdokjicmpqn
 */
package org.tio.http.common.view;

import org.tio.http.common.HttpRequest;

/**
 * 模板引擎model创建者
 * 
 * @author tanyaowu 2017年11月15日 下午1:12:39
 */
public interface ModelGenerator {

    /**
     * 
     * @param request
     * @return
     * @author tanyaowu
     * @throws Exception
     */
    Object generate(HttpRequest request) throws Exception;

}

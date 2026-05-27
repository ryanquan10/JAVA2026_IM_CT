/*
 * qmudbd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zrrhv
 */
/**
 * 
 */
package org.tio.http.server.stat;

import org.tio.http.common.HttpRequest;
import org.tio.http.common.HttpResponse;

/**
 * @author tanyaowu
 *
 */
public class DefaultStatPathFilter implements StatPathFilter {

    public static final DefaultStatPathFilter me = new DefaultStatPathFilter();

    /**
     * 
     */
    public DefaultStatPathFilter() {
    }

    @Override
    public boolean filter(String path, HttpRequest request, HttpResponse response) {
	return true;
    }

}

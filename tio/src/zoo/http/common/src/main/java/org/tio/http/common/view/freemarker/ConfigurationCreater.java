/*
 * uicigjr本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qaxceixwioe
 */
/**
 * 
 */
package org.tio.http.common.view.freemarker;

import java.io.IOException;

import org.tio.http.common.HttpConfig;

import freemarker.template.Configuration;

/**
 * @author tanyaowu
 *
 */
public interface ConfigurationCreater {
    /**
     * 
     * @param httpConfig
     * @param root
     * @return
     * @throws IOException
     */
    public Configuration createConfiguration(HttpConfig httpConfig, String root) throws IOException;

}

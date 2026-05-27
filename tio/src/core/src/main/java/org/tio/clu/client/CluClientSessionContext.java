/*
 * fbofmxaonp本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xzblojhyv
 */
package org.tio.clu.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.common.CluSessionContext;

/**
 * @author tanyaowu 2020年8月26日 下午1:47:25
 */
public class CluClientSessionContext extends CluSessionContext {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(CluClientSessionContext.class);
    private String cgId = null;

    public void clean() {
	setHandshaked(false);
    }

    /**
     * @return the cgId
     */
    public String getCgId() {
	return cgId;
    }

    /**
     * @param cgId the cgId to set
     */
    public void setCgId(String cgId) {
	this.cgId = cgId;
    }

}

/*
 * buhuwicddqy本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动dbynm
 */
package org.tio.http.common.session.id.impl;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpConfig;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.session.id.ISessionIdGenerator;

/**
 * @author tanyaowu 2017年8月15日 上午10:53:39
 */
public class UUIDSessionIdGenerator implements ISessionIdGenerator {
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(UUIDSessionIdGenerator.class);

    public final static UUIDSessionIdGenerator instance = new UUIDSessionIdGenerator();

    /**
     *
     * @author tanyaowu
     */
    private UUIDSessionIdGenerator() {
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public String sessionId(HttpConfig httpConfig, HttpRequest request) {
	return UUID.randomUUID().toString().replace("-", "");
    }
}

/*
 * jqgwymavsyqh本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pciinpaovjiv
 */
package org.tio.utils.thread.pool;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tanyaowu
 */
public class TioCallerRunsPolicy extends CallerRunsPolicy {
    private static Logger log = LoggerFactory.getLogger(TioCallerRunsPolicy.class);

    public TioCallerRunsPolicy() {
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
	log.error(r.getClass().getSimpleName());
	super.rejectedExecution(r, e);
    }

}

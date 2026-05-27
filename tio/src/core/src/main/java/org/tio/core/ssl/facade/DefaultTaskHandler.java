/*
 * ftpbgpgc本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动dcxogsuz
 */
package org.tio.core.ssl.facade;

import javax.net.ssl.SSLException;

public class DefaultTaskHandler implements ITaskHandler {
    @Override
    public void process(ITasks tasks) throws SSLException {
	Runnable task;
	while ((task = tasks.next()) != null) {
	    task.run();
	}

	/*
	 * Must be called to signal to the SSLFacade that all tasks have been completed
	 * and that the handshake process should resume
	 */
	tasks.done();
    }
}

/*
 * rphrzjpavqv本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动uuglkrbyy
 */
package org.tio.core.ssl.facade;

import javax.net.ssl.SSLException;

public class Tasks implements ITasks {
    private final Worker _worker;
    private final Handshaker _hs;

    public Tasks(Worker worker, Handshaker hs) {
	_worker = worker;
	_hs = hs;
    }

    @Override
    public void done() throws SSLException {
	_hs.carryOn();
    }

    @Override
    public Runnable next() {
	return _worker.getDelegatedTask();
    }
}

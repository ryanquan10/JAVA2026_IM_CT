/*
 * qxrmqelmaqu本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ryhhdyfsfgtgw
 */
package org.tio.webpack.model;

public class Root {
    private boolean debug;

    private Console console;

    private boolean dev;

    private Compress compress;

    public Compress getCompress() {
	return this.compress;
    }

    public Console getConsole() {
	return this.console;
    }

    public boolean getDebug() {
	return this.debug;
    }

    public boolean getDev() {
	return this.dev;
    }

    public void setCompress(Compress compress) {
	this.compress = compress;
    }

    public void setConsole(Console console) {
	this.console = console;
    }

    public void setDebug(boolean debug) {
	this.debug = debug;
    }

    public void setDev(boolean dev) {
	this.dev = dev;
    }

}
/*
 * bwqeucdspfkd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vfxmmfcoj
 */
/**
 * 
 */
package org.tio.http.common;

import java.io.File;
import java.io.InputStream;

/**
 * @author tanyaowu
 *
 */
public class HttpResource {

    private String path = null;
    private InputStream inputStream = null;
    private File file = null;

    /**
     * 
     */
    public HttpResource() {
    }

    public HttpResource(String path, InputStream inputStream, File file) {
	super();
	this.path = path;
	this.inputStream = inputStream;
	this.file = file;
    }

    public File getFile() {
	return file;
    }

    public InputStream getInputStream() {
	return inputStream;
    }

    public String getPath() {
	return path;
    }

    public void setFile(File file) {
	this.file = file;
    }

    public void setInputStream(InputStream inputStream) {
	this.inputStream = inputStream;
    }

    public void setPath(String path) {
	this.path = path;
    }

}

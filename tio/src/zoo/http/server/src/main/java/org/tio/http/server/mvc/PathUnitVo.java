/*
 * djonhpjnvjefm本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动lrlnv
 */
/**
 * 
 */
package org.tio.http.server.mvc;

/**
 * @author tanyaowu
 *
 */
public class PathUnitVo {

    /**
     * 是否是变量，true: 是变量
     */
    private boolean isVar = false;

    /**
     * 对于/user/{userid}来说，此值是userid
     */
    private String path = null;

    /**
     * 
     */
    public PathUnitVo() {
    }

    public PathUnitVo(boolean isVar, String path) {
	super();
	this.isVar = isVar;
	this.path = path;
    }

    /**
     * 对于/user/{userid}来说，此值是userid
     */
    public String getPath() {
	return path;
    }

    public boolean isVar() {
	return isVar;
    }

    /**
     * 对于/user/{userid}来说，此值是userid
     */
    public void setPath(String path) {
	this.path = path;
    }

    public void setVar(boolean isVar) {
	this.isVar = isVar;
    }

}

/*
 * xdejxtpmqe本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ebdgoq
 */
/**
 * 
 */
package org.tio.http.server.mvc;

import java.lang.reflect.Method;

/**
 * @author tanyaowu
 *
 */
public class VariablePathVo {

    /**
     * 对于/user/{userid}，就是["user", "userid"]
     */
    private PathUnitVo[] pathUnits = null;

    /**
     * 原path，形如/user/{userid}
     */
    private String path = null;

    private Method method = null;

    /**
     * 
     */
    public VariablePathVo() {

    }

    /**
     * @param path      原path，形如/user/{userid}
     * @param method
     * @param pathUnits 对于/user/{userid}，就是["user", "userid"]
     */
    public VariablePathVo(String path, Method method, PathUnitVo[] pathUnits) {
	super();
	this.method = method;
	this.pathUnits = pathUnits;
	this.path = path;
    }

    public Method getMethod() {
	return method;
    }

    /**
     * 原path，形如/user/{userid}
     */
    public String getPath() {
	return path;
    }

    /**
     * 对于/user/{userid}，就是["user", "userid"]
     */
    public PathUnitVo[] getPathUnits() {
	return pathUnits;
    }

    public void setMethod(Method method) {
	this.method = method;
    }

    /**
     * 原path，形如/user/{userid}
     */
    public void setPath(String path) {
	this.path = path;
    }

    /**
     * 对于/user/{userid}，就是["user", "userid"]
     */
    public void setPathUnits(PathUnitVo[] pathUnits) {
	this.pathUnits = pathUnits;
    }

}

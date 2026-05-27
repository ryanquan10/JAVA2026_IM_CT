
package org.tio.sitexxx.service.service.atom;

import org.slf4j.Logger;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.IAtom;
import org.tio.sitexxx.service.utils.RetUtils;

public abstract class AbsAtom implements IAtom {

	/**
	 * 返回信息
	 */
	protected String msg;

	/**
	 * 返回编码
	 */
	protected Short result;

	/**
	 * 返回对象集合
	 */
	protected Ret retObj;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setErrMsg(String msg, Logger log) {
		this.msg = msg;
		log.error(msg);
	}

	public Short getResult() {
		return result;
	}

	public void setResult(Short result) {
		this.result = result;
	}

	public Ret getRetObj() {
		return retObj;
	}

	public void setRetObj(Ret retObj) {
		this.retObj = retObj;
	}

	public boolean failRet(Ret ret) {
		this.msg = RetUtils.getRetMsg(ret);
		retObj = ret;
		return false;
	}

	public boolean failRet(String msg) {
		this.msg = msg;
		retObj = RetUtils.failMsg(msg);
		return false;
	}

	public boolean okRet(Object data) {
		retObj = RetUtils.okData(data);
		return true;
	}

	public boolean okRet(Ret ret) {
		retObj = ret;
		return true;
	}
}

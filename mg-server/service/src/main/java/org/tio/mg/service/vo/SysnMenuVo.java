
package org.tio.mg.service.vo;

import java.io.Serializable;
import java.util.ArrayList;

import org.tio.mg.service.model.mg.MgAuth;

/**
 * 
 * @author xufei
 * 2020年8月3日 上午11:39:25
 */
public class SysnMenuVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4300910750401270673L;

	private ArrayList<MgAuth> data;
	
	private boolean ok;

	
	public ArrayList<MgAuth> getData() {
		return data;
	}

	public void setData(ArrayList<MgAuth> data) {
		this.data = data;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}
	
	
}


/**
 * 
 */
package org.tio.sitexxx.service.vo;

import java.io.Serializable;

/**
 * 群组统计
 * @author tanyaowu
 *
 */
public class GroupStat implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8563172443011041452L;

	public GroupStat() {
	}

	/**
	 * 
	 */

	/**
	 * 真实在线峰值
	 */
	private int maxRealOnline = 0;

	/**
	 * 经过计算出来的在线峰值
	 */
	private int maxCalcOnline = 0;

	/**
	 * pc在线人数（真实）
	 */
	private int pcOnline = 0;

	/**
	 * 安卓在线人数（真实）
	 */
	private int	androidOnline	= 0;
	/**
	 * ios在线人数（真实）
	 */
	private int	iosOnline		= 0;
	/**
	 * 当前所有客户端（含PC， 安卓，IOS，）的在线人数（计算出来的数值）
	 */
	private int	calcOnline		= 0;

	public int getMaxRealOnline() {
		return maxRealOnline;
	}

	public void setMaxRealOnline(int maxRealOnline) {
		this.maxRealOnline = maxRealOnline;
	}

	public int getMaxCalcOnline() {
		return maxCalcOnline;
	}

	public void setMaxCalcOnline(int maxCalcOnline) {
		this.maxCalcOnline = maxCalcOnline;
	}

	public int getPcOnline() {
		return pcOnline;
	}

	public void setPcOnline(int pcOnline) {
		this.pcOnline = pcOnline;
	}

	public int getAndroidOnline() {
		return androidOnline;
	}

	public void setAndroidOnline(int androidOnline) {
		this.androidOnline = androidOnline;
	}

	public int getIosOnline() {
		return iosOnline;
	}

	public void setIosOnline(int iosOnline) {
		this.iosOnline = iosOnline;
	}

	public int getCalcOnline() {
		return calcOnline;
	}

	public void setCalcOnline(int calcOnline) {
		this.calcOnline = calcOnline;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}

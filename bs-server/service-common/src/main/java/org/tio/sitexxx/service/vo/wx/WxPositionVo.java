
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 位置消息对象
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxPositionVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500652530550301909L;

	/**
	 * 纬度
	 */
	private double latitude;

	/**
	 * 经度
	 */
	private double longitude;

	/**
	 * 定位名称
	 */
	private String name;

	/**
	 * 定位地址
	 */
	private String address;

	/**
	 * 定位图片封面
	 */
	private String mapSnapshot;
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMapSnapshot() {
		return mapSnapshot;
	}

	public void setMapSnapshot(String mapSnapshot) {
		this.mapSnapshot = mapSnapshot;
	}

	/**
	 * @param args
	 * @author lixinji 2020年2月10日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}

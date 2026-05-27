

package org.tio.sitexxx.im.common.bs.wx.chatitem;

import java.io.Serializable;

/**
 * 位置消息请求
 * @author tanyaowu 

 */
public class WxPositionReq implements Serializable {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8838786116225726932L;
	/**
	 * 会话id
	 */
	private Long				chatlinkid;

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

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

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
}
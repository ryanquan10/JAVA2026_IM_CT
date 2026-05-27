
/**
 * 
 */
package org.tio.mg.im.common.bs;

import java.io.Serializable;

/**
 * 分页获取在线观众请求
 * @author tanyaowu
 */
public class PageOnlineReq implements Serializable {

	/**
	 * type字段的枚举值
	 * @author tanyaowu
	 */
	public static interface Type {
		int	PC		= 1;
		int	ANDROID	= 2;
		int	IOS		= 3;
		int	ALL		= 99;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -875940565815372237L;

	/**
	 * 群组id，此处是群主的userid
	 */
	private String g;

	/**
	 * 哪种客户端的在线列表，1：pc，2：安卓，3：IOS，null：所有端
	 */
	private Integer type;

	/**
	 * 每页显示多少条数据
	 */
	private Integer pageSize;

	/**
	 * 第几页，从1开始计数，第一页传1
	 */
	private Integer pageNumber;

	public String getG() {
		return g;
	}

	public void setG(String g) {
		this.g = g;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

}

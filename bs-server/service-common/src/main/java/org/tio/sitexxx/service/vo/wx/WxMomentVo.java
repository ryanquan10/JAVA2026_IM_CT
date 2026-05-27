
package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

/**
 * 位置消息对象
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxMomentVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500652530550301909L;

	/**
	 * 朋友圈id
	 */
	private Integer mid;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 视频url
	 */
	private String videoUrl;

	/**
	 * 图片url
	 */
	private String imgUrl;

	public Integer getMid() {
		return mid;
	}

	public void setMid(Integer mid) {
		this.mid = mid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	/**
	 * @param args
	 * @author xinji 2023年12月7日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}


package org.tio.sitexxx.service.vo.wx;

import java.io.Serializable;

import cn.hutool.core.util.StrUtil;

/**
 * 模板对象vo
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxTemplateMsgVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5320818170151506451L;

	/**
	 * 主标题
	 */
	private String title;

	/**
	 * 副标题
	 */
	private String subtitle;

	/**
	 * url
	 */
	private String url;

	/**
	 * 图片
	 */
	private String img;

	/**
	 * 图片简介
	 */
	private String imgalt;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 缩略内容
	 */
	private String resume;

	/**
	 * 时间
	 */
	private String time;

	/**
	 * 作者
	 */
	private String author;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public String getImgalt() {
		return imgalt;
	}

	public void setImgalt(String imgalt) {
		this.imgalt = imgalt;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年12月29日 下午4:24:27
	 */
	public boolean checkIsNull() {
		if (StrUtil.isBlank(title) && StrUtil.isBlank(subtitle) && StrUtil.isBlank(resume) && StrUtil.isBlank(img) && StrUtil.isBlank(content)) {
			return true;
		}
		return false;
	}
}


/**
 * 
 */
package org.tio.sitexxx.service.vo.im;

import java.io.Serializable;

import org.tio.utils.json.Json;

/**
 * @author tanyaowu
 * 超链接卡片消息
 */
public class CardLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1796945022986497182L;

	/**
	 * 内容类型
	 * @author tanyaowu
	 *
	 */
	public static enum ContentType {

		blog("博客", 1), news("新闻", 2), soft("软件", 3);

		private String name;

		private int value;

		private ContentType(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public static String getName(int value) {
			for (ContentType c : ContentType.values()) {
				if (c.getValue() == value) {
					return c.name;
				}
			}
			return null;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

	}

	public CardLink(String link, String title, String sum, ContentType contentType) {
		this();
		this.link = link;
		this.title = title;
		this.sum = sum;
		this.contentType = contentType;
	}

	public CardLink() {
	}

	/**
	 * 链接地址
	 */
	private String link;

	/**
	 * 标题
	 */
	private String title;

	/**
	 * 内容摘要
	 */
	private String sum;

	/**
	 *  内容类型
	 */
	private ContentType contentType = ContentType.news;

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSum() {
		return sum;
	}

	public void setSum(String sum) {
		this.sum = sum;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(Json.toJson(new CardLink()));
	}

}

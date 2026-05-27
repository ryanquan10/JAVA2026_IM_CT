
package org.tio.sitexxx.service.vo;

import org.tio.sitexxx.service.model.main.*;

import java.io.Serializable;
import java.util.Date;

/**
 * 位置消息对象
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class CircleArticleMsgVo implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5500652530550301909L;

	private CircleMsg circleMsg;
	private CircleArticle article;
	private CircleArticleLike articleLike;
	private CircleArticleComment comment;
	private Short likeCancel;
	private Short commentCancel;
	private String content;
	private String avatar;
	private String fromNickname;
	private Date time;
	private Short delArticle;
	private String videoUrl;
	private String imgUrl;
	private String articleContent;

	public String getArticleContent() {
		return articleContent;
	}

	public void setArticleContent(String articleContent) {
		this.articleContent = articleContent;
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

	public Short getDelArticle() {
		return delArticle;
	}

	public void setDelArticle(Short delArticle) {
		this.delArticle = delArticle;
	}


	public CircleMsg getCircleMsg() {
		return circleMsg;
	}

	public void setCircleMsg(CircleMsg circleMsg) {
		this.circleMsg = circleMsg;
	}

	public CircleArticle getArticle() {
		return article;
	}

	public void setArticle(CircleArticle article) {
		this.article = article;
	}

	public CircleArticleLike getArticleLike() {
		return articleLike;
	}

	public void setArticleLike(CircleArticleLike articleLike) {
		this.articleLike = articleLike;
	}

	public CircleArticleComment getComment() {
		return comment;
	}

	public void setComment(CircleArticleComment comment) {
		this.comment = comment;
	}

	public Short getLikeCancel() {
		return likeCancel;
	}

	public void setLikeCancel(Short likeCancel) {
		this.likeCancel = likeCancel;
	}

	public Short getCommentCancel() {
		return commentCancel;
	}

	public void setCommentCancel(Short commentCancel) {
		this.commentCancel = commentCancel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getFromNickname() {
		return fromNickname;
	}

	public void setFromNickname(String fromNickname) {
		this.fromNickname = fromNickname;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	/**
	 * @param args
	 * @author xinji 2023年12月9日 下午3:12:59
	 */
	public static void main(String[] args) {

	}
}

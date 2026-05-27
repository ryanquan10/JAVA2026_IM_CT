
package org.tio.sitexxx.service.vo;

import org.tio.sitexxx.service.model.main.Moments;
import org.tio.sitexxx.service.model.main.MomentsComments;
import org.tio.sitexxx.service.model.main.MomentsLikes;
import org.tio.sitexxx.service.model.main.WxFriendMsg;

import java.io.Serializable;
import java.util.Date;

/**
 * 位置消息对象
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class WxMomentMsgVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500652530550301909L;

	WxFriendMsg wxFriendMsg;
	Moments moments;
	MomentsLikes momentsLikes;
	Short likeCancel;
	Short commentCancel;
	String content;
	String avatar;
	String fromNickname;

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

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Short getCommentCancel() {
		return commentCancel;
	}

	public void setCommentCancel(Short commentCancel) {
		this.commentCancel = commentCancel;
	}

	MomentsComments momentsComments;
	Date time;

	public Short getLikeCancel() {
		return likeCancel;
	}

	public void setLikeCancel(Short likeCancel) {
		this.likeCancel = likeCancel;
	}

	public WxMomentMsgVo() {
	}

	public WxMomentMsgVo(WxFriendMsg wxFriendMsg, Moments moments, MomentsLikes momentsLikes, MomentsComments momentsComments, Date time) {
		this.wxFriendMsg = wxFriendMsg;
		this.moments = moments;
		this.momentsLikes = momentsLikes;
		this.momentsComments = momentsComments;
		this.time = time;
	}

	public WxFriendMsg getWxFriendMsg() {
		return wxFriendMsg;
	}

	public void setWxFriendMsg(WxFriendMsg wxFriendMsg) {
		this.wxFriendMsg = wxFriendMsg;
	}

	public Moments getMoments() {
		return moments;
	}

	public void setMoments(Moments moments) {
		this.moments = moments;
	}

	public MomentsLikes getMomentsLikes() {
		return momentsLikes;
	}

	public void setMomentsLikes(MomentsLikes momentsLikes) {
		this.momentsLikes = momentsLikes;
	}

	public MomentsComments getMomentsComments() {
		return momentsComments;
	}

	public void setMomentsComments(MomentsComments momentsComments) {
		this.momentsComments = momentsComments;
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

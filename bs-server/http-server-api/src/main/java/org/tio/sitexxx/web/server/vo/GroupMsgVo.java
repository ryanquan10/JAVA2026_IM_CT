
package org.tio.sitexxx.web.server.vo;



import org.tio.http.common.UploadFile;

import java.io.Serializable;

/**
 * 群发消息Vo
 * 
 * @author lixinji 2020年2月10日 下午3:12:31
 */
public class GroupMsgVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4279416613620223037L;
	private Integer id;

	/**
	 * 用户id
	 */
	private Integer uid;

	/**
	 * 群发uid，使用 "," 隔开
	 */
	private String uids;

	/**
	 * 群发groupid，使用 "," 隔开
	 */
	private String groupids;

	/**
	 * 消息
	 */
	private String msg;

	/**
	 * 内容类型，1、普通文本消息，3、文件，4、音频，5、视频，6、图片
	 */
	private Integer contenttype;

	/**
	 * 文件
	 */
	private UploadFile file;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public String getUids() {
		return uids;
	}

	public void setUids(String uids) {
		this.uids = uids;
	}

	public String getGroupids() {
		return groupids;
	}

	public void setGroupids(String groupids) {
		this.groupids = groupids;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getContenttype() {
		return contenttype;
	}

	public void setContenttype(Integer contenttype) {
		this.contenttype = contenttype;
	}

	public UploadFile getFile() {
		return file;
	}

	public void setFile(UploadFile file) {
		this.file = file;
	}

	public static void main(String[] args) {

	}
}

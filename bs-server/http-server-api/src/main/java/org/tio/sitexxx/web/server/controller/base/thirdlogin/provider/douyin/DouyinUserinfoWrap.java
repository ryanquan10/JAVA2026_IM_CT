
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.douyin;

/**
 * @author tanyaowu
 */
public class DouyinUserinfoWrap {

	public DouyinUserinfoWrap() {
	}

	private String			message;
	private DouyinUserinfo	data;

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setData(DouyinUserinfo douyinUserinfo) {
		this.data = douyinUserinfo;
	}

	public DouyinUserinfo getData() {
		return data;
	}

	public static class DouyinUserinfo {

		private int		error_code;
		private String	description;
		private String	open_id;
		private String	union_id;
		private String	nickname;
		private String	avatar;

		public void setError_code(int error_code) {
			this.error_code = error_code;
		}

		public int getError_code() {
			return error_code;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public void setOpen_id(String open_id) {
			this.open_id = open_id;
		}

		public String getOpen_id() {
			return open_id;
		}

		public void setUnion_id(String union_id) {
			this.union_id = union_id;
		}

		public String getUnion_id() {
			return union_id;
		}

		public void setNickname(String nickname) {
			this.nickname = nickname;
		}

		public String getNickname() {
			return nickname;
		}

		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}

		public String getAvatar() {
			return avatar;
		}

	}

}


package org.tio.mg.web.server.controller.base.thirdlogin.auth2;

import java.io.Serializable;

/**
 * @author tanyaowu
 */
public class AccessTokenResp implements Serializable {

	private static final long	serialVersionUID	= 7920132287341057785L;
	private String				message;
	private Data				data;

	/**
	 * 
	 */
	public AccessTokenResp() {

	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

	public static class Data implements Serializable {
		private static final long serialVersionUID = 5367177515645004107L;

		private String	access_token;
		private String	captcha;
		private String	description;
		private int		error_code;
		private long	expires_in;
		private String	open_id;
		private String	refresh_token;
		private String	scope;

		public String getAccess_token() {
			return access_token;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public String getCaptcha() {
			return captcha;
		}

		public void setCaptcha(String captcha) {
			this.captcha = captcha;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public int getError_code() {
			return error_code;
		}

		public void setError_code(int error_code) {
			this.error_code = error_code;
		}

		public long getExpires_in() {
			return expires_in;
		}

		public void setExpires_in(long expires_in) {
			this.expires_in = expires_in;
		}

		public String getOpen_id() {
			return open_id;
		}

		public void setOpen_id(String open_id) {
			this.open_id = open_id;
		}

		public String getRefresh_token() {
			return refresh_token;
		}

		public void setRefresh_token(String refresh_token) {
			this.refresh_token = refresh_token;
		}

		public String getScope() {
			return scope;
		}

		public void setScope(String scope) {
			this.scope = scope;
		}

	}

}


package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.osc;

/**
 * @author tanyaowu 
 * 2019年4月6日 下午11:07:43
 */
public class OscAccessTokenResp {
	/**
	 * {
		    "access_token": "8447ff97-9b8c-4224-9cec-63b97d34ba65", 
			"refresh_token": "8447ff97-9b8c-4224-9cec", 
		    "token_type": "bearer", 
		    "expires_in": 43199,
			"uid": 12
		}
	 */
	private String access_token;
	private String refresh_token;
	private String token_type;
	private Long expires_in;
	private Long uid;

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the access_token
	 */
	public String getAccess_token() {
		return access_token;
	}

	/**
	 * @param access_token the access_token to set
	 */
	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	/**
	 * @return the refresh_token
	 */
	public String getRefresh_token() {
		return refresh_token;
	}

	/**
	 * @param refresh_token the refresh_token to set
	 */
	public void setRefresh_token(String refresh_token) {
		this.refresh_token = refresh_token;
	}

	/**
	 * @return the token_type
	 */
	public String getToken_type() {
		return token_type;
	}

	/**
	 * @param token_type the token_type to set
	 */
	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	/**
	 * @return the expires_in
	 */
	public Long getExpires_in() {
		return expires_in;
	}

	/**
	 * @param expires_in the expires_in to set
	 */
	public void setExpires_in(Long expires_in) {
		this.expires_in = expires_in;
	}

	/**
	 * @return the uid
	 */
	public Long getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(Long uid) {
		this.uid = uid;
	}
}

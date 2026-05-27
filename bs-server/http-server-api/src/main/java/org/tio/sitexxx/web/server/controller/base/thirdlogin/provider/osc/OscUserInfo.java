
package org.tio.sitexxx.web.server.controller.base.thirdlogin.provider.osc;

/**
 * @author tanyaowu 
 * 2019年4月6日 下午10:01:21
 */
public class OscUserInfo {
	//	{
	//	    id: 899**,
	//	    email: "****@gmail.com",
	//	    name: "彭博",
	//	    gender: "male",
	//	    avatar: "http://www.oschina.net/uploads/user/****",
	//	    location: "广东 深圳",
	//	    url: "http://home.oschina.net/****"
	//	}

	private Long	id;
	private String	email;
	private String	name;
	private String	gender;
	private String	avatar;
	private String	location;
	private String	url;

	/**
	 * 
	 * @author tanyaowu
	 */
	public OscUserInfo() {
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * @param gender the gender to set
	 */
	public void setGender(String gender) {
		this.gender = gender;
	}

	/**
	 * @return the avatar
	 */
	public String getAvatar() {
		return avatar;
	}

	/**
	 * @param avatar the avatar to set
	 */
	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}


package org.tio.sitexxx.service.vo.nd;

import java.io.Serializable;

/**
 * 南大先腾外部创建群vo
 * 部分群信息可根据wxgroup进行添加
 * @author lixinji
 * 2021年4月15日 下午3:17:16
 */
public class NdGroupVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6456964274459261304L;

	/**
	 * 群成员外部用户code列表，以','分割
	 */
	private String usercodes;

	/**
	 * 创建群外部用户code
	 */
	private String createcode;

	/**
	 * 群简介
	 */
	private String intro;

	/**
	 * 群名称
	 */
	private String name;

	public String getUsercodes() {
		return usercodes;
	}

	public void setUsercodes(String usercodes) {
		this.usercodes = usercodes;
	}

	public String getCreatecode() {
		return createcode;
	}

	public void setCreatecode(String createcode) {
		this.createcode = createcode;
	}

	public String getIntro() {
		return intro;
	}

	public void setIntro(String intro) {
		this.intro = intro;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}

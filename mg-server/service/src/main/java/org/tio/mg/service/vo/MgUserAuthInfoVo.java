
package org.tio.mg.service.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class MgUserAuthInfoVo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8968129151401225094L;
	
	public MgUserAuthInfoVo(Integer mguid) {
		this.mguid = mguid;
		menuList = new ArrayList<MgMenuVo>();
		allAuth = new HashMap<String, MgAuthVo>();
		pageAuth = new HashMap<String, MgAuthVo>();
		operAuth = new HashMap<String, MgAuthVo>();
		menuAuth = new HashMap<String, MgAuthVo>();
	}

	private Integer mguid;
	
	/**
	 * 菜单列表
	 */
	private ArrayList<MgMenuVo> menuList;
	
	/**
	 * 所有权限
	 */
	private HashMap<String, MgAuthVo> allAuth;
	
	/**
	 * 操作权限
	 * key:operurl
	 * value:MgAuth
	 */
	private HashMap<String, MgAuthVo> operAuth;
	
	/**
	 * 菜单权限
	 * key:operurl
	 * value:MgAuth
	 */
	private HashMap<String, MgAuthVo> menuAuth;

	
	/**
	 * 页面权限
	 */
	private HashMap<String, MgAuthVo> pageAuth;
	
	public Integer getMguid() {
		return mguid;
	}

	public void setMguid(Integer mguid) {
		this.mguid = mguid;
	}

	public ArrayList<MgMenuVo> getMenuList() {
		return menuList;
	}

	public void setMenuList(ArrayList<MgMenuVo> menuList) {
		this.menuList = menuList;
	}

	public HashMap<String, MgAuthVo> getAllAuth() {
		return allAuth;
	}

	public void setAllAuth(HashMap<String, MgAuthVo> allAuth) {
		this.allAuth = allAuth;
	}

	public HashMap<String, MgAuthVo> getOperAuth() {
		return operAuth;
	}

	public void setOperAuth(HashMap<String, MgAuthVo> operAuth) {
		this.operAuth = operAuth;
	}

	public HashMap<String, MgAuthVo> getMenuAuth() {
		return menuAuth;
	}

	public void setMenuAuth(HashMap<String, MgAuthVo> menuAuth) {
		this.menuAuth = menuAuth;
	}

	public HashMap<String, MgAuthVo> getPageAuth() {
		return pageAuth;
	}

	public void setPageAuth(HashMap<String, MgAuthVo> pageAuth) {
		this.pageAuth = pageAuth;
	}
}


package org.tio.mg.service.vo;

import java.util.List;
import java.util.Map;

import org.tio.sitexxx.service.vo.Const;

/**
 * 菜单Vo
 * @author xufei
 * 2020年5月27日 上午10:23:03
 */
public class MgMenuVo extends MgAuthVo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 281341514732054623L;

	/**
	 * 深度：1级为一级菜单/页面
	 */
	private Short deep = 1;
	
	private List<MgMenuVo> childs;
	
	private String levelname;
	
	/**
	 * 权限id
	 */
	private Integer id;
	
	/**
	 * 虚拟菜单标识:YES时，代表是一级菜单是页面，为适应vue,自动建立虚拟一级菜单，而一级页面自降为2级页面
	 */
	private Short virtualmenuflag = Const.YesOrNo.NO;
	
	private Map<String, MgAuthVo> operAuth;

	public Short getDeep() {
		return deep;
	}

	public void setDeep(Short deep) {
		this.deep = deep;
	}

	public List<MgMenuVo> getChilds() {
		return childs;
	}

	public void setChilds(List<MgMenuVo> childs) {
		this.childs = childs;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Map<String, MgAuthVo> getOperAuth() {
		return operAuth;
	}

	public void setOperAuth(Map<String, MgAuthVo> operAuth) {
		this.operAuth = operAuth;
	}

	public Short getVirtualmenuflag() {
		return virtualmenuflag;
	}

	public void setVirtualmenuflag(Short virtualmenuflag) {
		this.virtualmenuflag = virtualmenuflag;
	}

	public String getLevelname() {
		return levelname;
	}

	public void setLevelname(String levelname) {
		this.levelname = levelname;
	}
}

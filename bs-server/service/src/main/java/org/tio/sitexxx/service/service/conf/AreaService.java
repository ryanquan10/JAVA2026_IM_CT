
package org.tio.sitexxx.service.service.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.sitexxx.service.model.conf.Area;
import org.tio.sitexxx.service.vo.Const;

/**
 * 区域缓存加载处理
 * @author tanyaowu
 *
 */
public class AreaService {
	private static Logger			log				= LoggerFactory.getLogger(AreaService.class);
	public static final AreaService	me				= new AreaService();
	final static Area				areaDao			= new Area().dao();
	/**
	 * 所有区域的树形结构缓存
	 */
	private static List<Area>		allchildData	= null;

	/**
	 * 节点code的区域，包含子区域
	 */
	private static Map<String, Area> childCacheData = null;

	/**
	 * 节点code的区域，包含父区域
	 */
	private static Map<String, Area> parentCacheData = null;

	/**
	 * 清空缓存
	 */
	public static void clearCache() {
		allchildData = null;
		childCacheData = null;
		parentCacheData = null;
	}

	public static void init() {
		loadData();
	}

	/**
	 * 加载数据
	 * 
	 */
	private static void loadData() {
		//		clearCache();
		Map<String, Area> childCacheDataTemp = new HashMap<>();
		Map<String, Area> parentCacheDataTemp = new HashMap<>();
		List<Area> allchildDataTemp = new ArrayList<>();
		List<Map<String, Area>> depthChildAreaList = new ArrayList<>();
		try {
			String whereSql = " where pcode = 'area' and  status = " + Const.AreaViewStatus.VIEW;
			List<Area> depthArea = areaDao.find(getSelectSql() + whereSql);
			for (int depth = 0; depthArea != null && !depthArea.isEmpty(); depth++) {
				Map<String, Area> depthChildMap = new HashMap<>();
				for (Area area : depthArea) {
					Area copy = copyArea(area);
					if (depth == 0) {
						allchildDataTemp.add(area);
						parentCacheDataTemp.put(area.getCode(), copy);
					} else {
						Area parentArea = parentCacheDataTemp.get(area.getPcode());
						copy.setParentArea(parentArea);
						parentCacheDataTemp.put(area.getCode(), copy);
					}
					childCacheDataTemp.put(area.getCode(), area);
					depthChildMap.put(area.getCode(), area);
				}
				depthChildAreaList.add(depthChildMap);
				whereSql = getWhereSql(whereSql);
				depthArea = areaDao.find(getSelectSql() + whereSql);
			}
			if (depthChildAreaList != null && !depthChildAreaList.isEmpty()) {
				for (int depth = 0; depth < depthChildAreaList.size(); depth++) {
					Map<String, Area> depthMap = depthChildAreaList.get(depth);
					if (depthMap != null && !depthMap.isEmpty()) {
						for (String key : depthMap.keySet()) {
							Area area = depthMap.get(key);
							areaChildInit(area, depth, depthChildAreaList);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			childCacheData = childCacheDataTemp;
			parentCacheData = parentCacheDataTemp;
			allchildData = allchildDataTemp;
		}
	}

	/**
	 * 区域子code初始化
	 * @param area
	 * @param depth
	 * @param depthAreaList
	 * 
	 */
	private static void areaChildInit(Area area, int depth, List<Map<String, Area>> depthAreaList) {
		if (depth >= (depthAreaList.size() - 1)) {
			return;
		}
		Map<String, Area> childAreaMap = depthAreaList.get(depth + 1);
		String childPcode = area.getCode();
		List<Area> childArea = new ArrayList<>();
		for (String key : childAreaMap.keySet()) {
			Area child = childAreaMap.get(key);
			if (child.getPcode().equals(childPcode)) {
				childArea.add(child);
			}
		}
		area.setChildArea(childArea);
	}

	/**
	 * 根据code,获取区域信息
	 * @param name
	 * @param defaultValue 默认值
	 * @return
	 */
	public static Area getParent(String code) {
		if (allchildData == null) {
			synchronized (AreaService.class) {
				if (allchildData == null) {
					loadData();
				}
			}
		}
		Area value = parentCacheData.get(code);
		return value;
	}

	/**
	 * 获取区域以及区域
	 * @param code
	 * @return
	 * 
	 */
	public static Area getChild(String code) {
		if (allchildData == null) {
			synchronized (AreaService.class) {
				if (allchildData == null) {
					loadData();
				}
			}
		}
		Area value = childCacheData.get(code);
		return value;
	}

	/**
	 * 获取区域树
	 * @return
	 * 
	 */
	public static List<Area> getAreaTree() {
		if (allchildData == null) {
			synchronized (AreaService.class) {
				if (allchildData == null) {
					loadData();
				}
			}
		}
		return allchildData;
	}

	/**
	 * 组装查询sql
	 * @param whereSql
	 * @return
	 * 
	 */
	private static String getWhereSql(String whereSql) {
		StringBuffer retSql = new StringBuffer();
		retSql.append(" where status = ").append(Const.AreaViewStatus.VIEW).append(" and pcode in ( ").append("select code from area ").append(whereSql).append(")");
		return retSql.toString();
	}

	/**
	 * 查询区域的返回列表字段
	 * @return
	 * 
	 */
	private static String getSelectSql() {
		return "select code,name,pcode from area ";
	}

	/**
	 * 复制区域，为避免子列表和父列表的冲突，导致死循环
	 * @param area
	 * @return
	 * 
	 */
	private static Area copyArea(Area area) {
		Area copyArea = new Area();
		copyArea.setName(area.getName());
		copyArea.setCode(area.getCode());
		copyArea.setPcode(area.getPcode());
		return copyArea;
	}

	public static void main(String[] args) {
	}
}

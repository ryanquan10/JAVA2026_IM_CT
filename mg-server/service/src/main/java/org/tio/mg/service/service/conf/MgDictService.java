
package org.tio.mg.service.service.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.conf.Dict;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.utils.PyUtils;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 字典数据加载
 * 
 */
public class MgDictService {

	private static Logger log = LoggerFactory.getLogger(MgDictService.class);

	public static final MgDictService me = new MgDictService();

	final static Dict dictDao = new Dict().dao();

	/**
	 * 子列表缓存前缀
	 */
	private static String CHILD_DICT_PRE = "child_list_";

	/**
	 * 字典缓存
	 * key: parentCode / code, value: List<Dict> / <Dict>
	 */
	private static Map<String, Object> cacheData = new HashMap<>();

	/**
	 * 清空字典缓存
	 * 
	 */
	public static void clearDict() {
		synchronized (Dict.class) {
			cacheData = new HashMap<>();
		}
	}

	/**
	 * 获取父节点下的子节点信息，后台去掉状态处理，上层调用需要进行状态判断
	 * @param parentCode
	 * @return
	 * 
	 */
	@SuppressWarnings("unchecked")
	public static List<Dict> getChildDictByParentCode(String pcode) {
		String key = CHILD_DICT_PRE + pcode;
		Object childDictList = cacheData.get(key);
		if (childDictList == null) {
			synchronized (Dict.class) {
				if (childDictList == null) {
					childDictList = dictDao.find("select d.id,d.name,d.code,d.pcode,d.attribute,pd.name pname,d.orderby,d.status from dict d left join dict pd on pd.code = d.pcode where d.pcode = ? order by d.orderby,d.id", pcode);
					if (childDictList != null) {
						cacheData.put(key, childDictList);
					}
				}
			}
		}
		return childDictList != null ? (List<Dict>) childDictList : new ArrayList<Dict>();
	}
	
	
	/**
	 * @param pcode
	 * @param name
	 * @param status
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午3:35:59
	 */
	public static List<Dict> getChildDictByParentCode(String pcode,String name,Short status) {
		if(StrUtil.isBlank(name) && status == null) {
			return getChildDictByParentCode(pcode);
		}
		Kv params = Kv.by("pcode", pcode);
		if(status != null) {
			params.set("status",status);
		}
		if(StrUtil.isNotBlank(name)) {
			params.set("name",name);
		}
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_CONF).getSqlPara("sys.dictQuery", params);
		return dictDao.find(sqlPara);
	}
	
	/**
	 * 调整顺序
	 * @param did
	 * @param topdid
	 * @return
	 * @author xufei
	 * 2020年6月10日 上午11:22:11
	 */
	public static Ret index(Integer did,Integer topdid) {
		Dict dict = dictDao.findById(did);
		if(dict == null) {
			return RetUtils.noExistParam();
		}
		if(topdid == null) {
			if(Objects.equals(dict.getOrderby(), 1)) {
				return RetUtils.failMsg("已在最顶端,不需要进行调整");
			}
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					int index = 1;
					Dict newdict = new Dict();
					newdict.setId(dict.getId());
					newdict.setOrderby(index);
					boolean comit = newdict.update(); {
						if(!comit) {
							return failRet("修改失败");
						}
					}
					List<Dict> childs = getChildDictByParentCode(dict.getPcode());
					if(CollectionUtil.isEmpty(childs)) {
						return failRet("系统错误");
					}
					for(Dict query : childs) {
						if(Objects.equals(query.getId(), dict.getId())) {
							continue;
						}
						index++;
						Dict rep = new Dict();
						rep.setId(query.getId());
						rep.setOrderby(index);
						boolean update = rep.update(); {
							if(!update) {
								return failRet("调整失败");
							}
						}
					}
					return true;
				}
			};
			boolean commit = Db.use(MgConst.Db.TIO_SITE_CONF).tx(atom);
			if(!commit) {
				return atom.getRetObj();
			}
		} else {
			Dict brother = dictDao.findById(topdid);
			if(brother == null) {
				return RetUtils.failMsg("兄弟节点不存在");
			}
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					int index = brother.getOrderby() + 1;
					Dict newdict = new Dict();
					newdict.setId(dict.getId());
					newdict.setOrderby(index);
					boolean comit = newdict.update(); {
						if(!comit) {
							return failRet("修改失败");
						}
					}
					List<Dict> childs = getChildDictByParentCode(dict.getPcode());
					if(CollectionUtil.isEmpty(childs)) {
						return failRet("系统错误");
					}
					boolean start = false;
					for(Dict query : childs) {
						if(Objects.equals(query.getId(), brother.getId())) {
							start = true;
							continue;
						}
						if(!start) {
							continue;
						}
						if(Objects.equals(query.getId(), dict.getId())) {
							continue;
						}
						index++;
						Dict rep = new Dict();
						rep.setId(query.getId());
						rep.setOrderby(index);
						boolean update = rep.update(); {
							if(!update) {
								return failRet("调整失败");
							}
						}
					}
					return true;
				}
			};
			boolean commit = Db.use(MgConst.Db.TIO_SITE_CONF).tx(atom);
			if(!commit) {
				return atom.getRetObj();
			}
		}
		if(StrUtil.isNotBlank(dict.getPcode())) {
			clearChildDictByParent(dict.getPcode());
		}
		synchronized (Dict.class) {
			cacheData.remove(dict.getCode());
		}
		return RetUtils.okOper();
	}

	/**
	 * 移除父节点下的列表
	 * @param parentCode
	 * 
	 */
	public static void clearChildDictByParent(String pcode) {
		synchronized (Dict.class) {
			cacheData.remove(CHILD_DICT_PRE + pcode);
		}
	}

	/**
	 * 清空节点信息
	 * @param code
	 * @param parentCode
	 * 
	 */
	public static void clearDictByCode(String code, String pcode) {
		synchronized (Dict.class) {
			cacheData.remove(code);
			cacheData.remove(CHILD_DICT_PRE + pcode);
		}
	}

	/**
	 * 获取字典信息-无状态，后台去掉状态处理，上层调用需要进行状态判断
	 * @param code
	 * @return
	 * 
	 */
	public static Dict getDictByCode(String code) {
		Object dict = cacheData.get(code);
		if (dict == null) {
			synchronized (Dict.class) {
				if (dict == null) {
					dict = dictDao.findFirst("select d.id,d.name,d.code,d.pcode,d.attribute,pd.name pname from dict d left join dict pd on pd.code = d.pcode where d.code = ? order by d.orderby,d.id", code);
					if (dict != null) {
						cacheData.put(code, dict);
					}
				}
			}
		}
		return dict != null ? (Dict) dict : null;
	}
	
	/**
	 * @param name
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午2:51:34
	 */
	public Dict existDictOfName(String name) {
		if(StrUtil.isBlank(name)) {
			return null;
		}
		return dictDao.findFirst("select id,name,code,pcode from dict where name = ?", name);
	}
	
	/**
	 * @param code
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午2:51:33
	 */
	public Dict existDictOfCode(String code) {
		if(StrUtil.isBlank(code)) {
			return null;
		}
		return dictDao.findFirst("select id,name,code,pcode,depth from dict where code = ?", code);
	}

	/**
	 * 
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午1:58:47
	 */
	public List<Dict> topList(String name) {
		if(StrUtil.isBlank(name)) {
			return dictDao.find("select id,name,code,pcode,depth,status,orderby,attribute from dict where pcode = '-1' order by charindex,id");
		} else {
			return dictDao.find("select id,name,code,pcode,depth,status,orderby,attribute from dict where pcode = '-1' and name like ? order by charindex,id","%" + name + "%");
		}
	}
	
	/**
	 * @param dict
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午2:42:05
	 */
	public Ret add(Dict dict) {
		if(StrUtil.isBlank(dict.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(StrUtil.isBlank(dict.getCode())) {
			return RetUtils.failMsg("编码为空");
		}
		if(Objects.equals(dict.getCode(), "-1")) {
			return RetUtils.failMsg("编码已存在");
		}
		if(StrUtil.isBlank(dict.getPcode())) {
			return RetUtils.failMsg("父节点code为空");
		}
		if(existDictOfCode(dict.getCode()) != null) {
			return RetUtils.failMsg("编码已存在");
		}
		if(existDictOfName(dict.getCode()) != null) {
			return RetUtils.failMsg("名称已存在");
		}
		if(Objects.equals(dict.getPcode(), "-1")) {
			dict.setDepth(1);
		} else {
			Dict pdict = existDictOfCode(dict.getPcode());
			if(pdict == null) {
				return RetUtils.failMsg("父节点不存在");
			}
			dict.setDepth(pdict.getDepth() + 1);
		}
		dict.setCharindex(PyUtils.getAllChat(dict.getName()));
		boolean save = dict.save();
		if(!save) {
			return RetUtils.failOper();
		}
		if(StrUtil.isNotBlank(dict.getPcode())) {
			clearChildDictByParent(dict.getPcode());
		}
		return RetUtils.okOper();
	}
	
	
	/**
	 * @param dict
	 * @return
	 * @author xufei
	 * 2020年6月15日 下午4:11:24
	 */
	public Ret insert(Dict dict) {
		if(dict.getBid() == null) {
			return RetUtils.failMsg("插入节点为空");
		}
		Dict brother = dictDao.findById(dict.getBid());
		if(brother == null) {
			return RetUtils.failMsg("插入节点不存在");
		}
		if(StrUtil.isBlank(dict.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(StrUtil.isBlank(dict.getCode())) {
			return RetUtils.failMsg("编码为空");
		}
		if(Objects.equals(dict.getCode(), "-1")) {
			return RetUtils.failMsg("编码已存在");
		}
		if(StrUtil.isBlank(dict.getPcode())) {
			return RetUtils.failMsg("父节点code为空");
		}
		if(existDictOfCode(dict.getCode()) != null) {
			return RetUtils.failMsg("编码已存在");
		}
		if(existDictOfName(dict.getCode()) != null) {
			return RetUtils.failMsg("名称已存在");
		}
		dict.setDepth(brother.getDepth());
		dict.setCharindex(PyUtils.getAllChat(dict.getName()));
		
		AbsTxAtom atom = new AbsTxAtom() {
			
			@Override
			public boolean noTxRun() {
				int index = brother.getOrderby() + 1;
				dict.setOrderby(index);
				boolean save = dict.save();
				if(!save) {
					return failRet("保存失败");
				}

				List<Dict> childs = getChildDictByParentCode(dict.getPcode());
				if(CollectionUtil.isEmpty(childs)) {
					return failRet("系统错误");
				}
				boolean start = false;
				for(Dict query : childs) {
					if(Objects.equals(query.getId(), brother.getId())) {
						start = true;
						continue;
					}
					if(!start) {
						continue;
					}
					if(Objects.equals(query.getId(), dict.getId())) {
						continue;
					}
					index++;
					Dict rep = new Dict();
					rep.setId(query.getId());
					rep.setOrderby(index);
					boolean update = rep.update(); {
						if(!update) {
							return failRet("调整失败");
						}
					}
				}
				return true;
			}
		};
		boolean commit = Db.use(MgConst.Db.TIO_SITE_CONF).tx(atom);
		if(!commit) {
			return atom.getRetObj();
		}
		if(StrUtil.isNotBlank(dict.getPcode())) {
			clearChildDictByParent(dict.getPcode());
		}
		return RetUtils.okOper();
	}
	
	/**
	 * @param dict
	 * @return
	 * @author xufei
	 * 2020年5月29日 下午2:44:18
	 */
	public Ret update(Dict dict) {
		if(dict.getId() == null) {
			log.error("修改字典数据id为空");
			return RetUtils.failMsg("id为空");
		}
		if(StrUtil.isBlank(dict.getName())) {
			return RetUtils.failMsg("名称为空");
		}
		if(StrUtil.isNotBlank(dict.getCode())) {
			return RetUtils.failMsg("编码不可修改");
		}
		if(StrUtil.isNotBlank(dict.getPcode())) {
			return RetUtils.failMsg("父节点不可修改");
		}
		Dict old = dictDao.findById(dict.getId());
		if(old == null) {
			return RetUtils.failMsg("字典不存在");
		}
		dict.setCharindex(PyUtils.getAllChat(dict.getName()));
		boolean update = dict.update(); 
		if(!update) {
			return RetUtils.failOper();
		}
		if(StrUtil.isNotBlank(old.getPcode())) {
			clearChildDictByParent(old.getPcode());
		}
		synchronized (Dict.class) {
			cacheData.remove(old.getCode());
		}
		return RetUtils.okOper();
	}
	
	
	/**
	 * @param id
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:38:49
	 */
	public Ret del(Integer id) {
		Dict old = dictDao.findById(id);
		if(old == null) {
			return RetUtils.noExistParam();
		}
		if(Objects.equals("-1", old.getPcode())) {
			boolean commit = dictDao.deleteById(id);
			if(!commit) {
				return RetUtils.failOper();
			}
		} else {
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					Db.use(MgConst.Db.TIO_SITE_CONF).delete("delete from dict where pcode = ?",old.getCode());
					boolean commit = dictDao.deleteById(id);
					if(!commit) {
						return failRet("删除失败");
					}
					return true;
				}
			};
			boolean commit = Db.use(MgConst.Db.TIO_SITE_CONF).tx(atom);
			if(!commit) {
				return RetUtils.failOper();
			}
		}
		if(StrUtil.isNotBlank(old.getPcode())) {
			clearChildDictByParent(old.getPcode());
		}
		synchronized (Dict.class) {
			clearDictByCode(old.getCode(), old.getCode());
		}
		return RetUtils.okOper();
	}
	
	
	/**
	 * @param id
	 * @return
	 * @author xufei
	 * 2020年6月2日 下午4:38:48
	 */
	public Ret disable(Integer id,Short status) {
		Dict old = dictDao.findById(id);
		if(old == null) {
			return RetUtils.noExistParam();
		}
		Dict update = new Dict();
		update.setId(old.getId());
		update.setStatus(status);
		boolean commit = update.update();
		if(!commit) {
			return RetUtils.failOper();
		}
		if(StrUtil.isNotBlank(old.getPcode())) {
			clearChildDictByParent(old.getPcode());
		}
		synchronized (Dict.class) {
			cacheData.remove(old.getCode());
		}
		return RetUtils.okOper();
	}
}

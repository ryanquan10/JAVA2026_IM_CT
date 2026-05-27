
package org.tio.mg.service.service.mg;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.mg.MgFavoritePath;
import org.tio.mg.service.service.atom.AbsTxAtom;
import org.tio.mg.service.service.conf.MgConfService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.mg.service.vo.MgConst;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 
 * @author xufei
 * 2020年7月7日 下午3:50:55
 */
public class MgTopMenuService {
	private static Logger log = LoggerFactory.getLogger(MgTopMenuService.class);

	public static final MgTopMenuService ME = new MgTopMenuService();
	
	/**
	 * @param pageSize
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午3:50:58
	 */
	public Ret recentPage(Integer pageSize,Integer mguid) {
		Integer pageNumber = 1;
		if(pageSize == null || pageNumber <= 0) {
			pageSize = MgConfService.getInt(MgConst.MgConfMapping.RECENT_PAGE_SIZE, 15);
		}
		Kv params = Kv.by("mguid", mguid).set("status",Const.Status.NORMAL).set("type",MgConst.AuthType.PAGE);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.recentPageList", params);
		Page<Record> recentPage = Db.use(MgConst.Db.TIO_MG).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okList(recentPage.getList());
	}
	
	/**
	 * 收藏列表
	 * @param pageSize
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午4:00:03
	 */
	public Ret favoritePage(Integer pageSize,Integer mguid) {
		Integer pageNumber = 1;
		if(pageSize == null || pageNumber <= 0) {
			pageSize = MgConfService.getInt(MgConst.MgConfMapping.FAVORITE_PAGE_SIZE, 15);
		}
		Kv params = Kv.by("mguid", mguid).set("status",Const.Status.NORMAL).set("type",MgConst.AuthType.PAGE);
		SqlPara sqlPara = Db.use(MgConst.Db.TIO_MG).getSqlPara("sys.favoritePageList", params);
		Page<Record> faPage = Db.use(MgConst.Db.TIO_MG).paginate(pageNumber, pageSize, sqlPara);
		return RetUtils.okList(faPage.getList());
	}
	
	/**
	 * @param path
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午4:04:36
	 */
	public Ret addfavorite(MgFavoritePath path) {
		if(path == null) {
			log.error("收藏页面空参数");
			return RetUtils.invalidParam();
		}
		if(StrUtil.isBlank(path.getRoutkey()) || path.getAid() == null) {
			return RetUtils.invalidParam();
		}
		path.ignoreSave();
		return RetUtils.okOper();
	}
	
	/**
	 * 删除
	 * @param id
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午4:09:24
	 */
	public Ret delfavorite(Integer id) {
		if(id == null) {
			log.error("收藏页面空参数");
			return RetUtils.invalidParam();
		}
		MgFavoritePath.dao.deleteById(id);
		return RetUtils.okOper();
	}
	
	
	/**
	 * 调整顺序
	 * @param id
	 * @param topid
	 * @param mguid
	 * @return
	 * @author xufei
	 * 2020年7月7日 下午4:40:38
	 */
	public Ret index(Integer id,Integer topid,Integer mguid) {
		MgFavoritePath path = MgFavoritePath.dao.findById(id);
		if(path == null) {
			return RetUtils.noExistParam();
		}
		MgFavoritePath brother = null;
		if(topid != null) {
			brother = MgFavoritePath.dao.findById(topid);
			if(brother == null) {
				return RetUtils.failMsg("兄弟节点不存在");
			}
		}
		List<Record> records = RetUtils.getOkTList(favoritePage(null, mguid));
		if(CollectionUtil.isEmpty(records)) {
			return RetUtils.okOper();
		}
		String ids = "";
		boolean isexist = false;
		boolean isbrother = brother != null ? false : true;
		for(Record record : records) {
			Integer pathid = record.getInt("id");
			if(!isexist && Objects.equals(pathid, path.getId())) {
				isexist = true;
			}
			if(!isbrother && Objects.equals(pathid, brother.getId())) {
				isbrother = true;
			}
			ids += "," + pathid;
		}
		if(!isexist) {
			return RetUtils.noExistParam();
		}
		if(!isbrother) {
			return RetUtils.failMsg("兄弟节点不存在");
		}
		ids = "(" + ids.substring(1) + ")";
		Db.use(MgConst.Db.TIO_MG).update("delete from mg_favorite_path where id not in " + ids);
		if(topid == null) {
			if(Objects.equals(path.getFindex(), (short)1)) {
				return RetUtils.failMsg("已在最顶端,不需要进行调整");
			}
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					short index = MgConfService.getInt(MgConst.MgConfMapping.FAVORITE_PAGE_SIZE, 15).shortValue();
					MgFavoritePath newpath = new MgFavoritePath();
					newpath.setId(path.getId());
					newpath.setFindex(index);
					boolean comit = newpath.update(); {
						if(!comit) {
							return failRet("修改失败");
						}
					}
					for(Record query : records) {
						Integer queryid = query.getInt("id");
						if(Objects.equals(queryid, path.getId())) {
							continue;
						}
						index--;
						MgFavoritePath rep = new MgFavoritePath();
						rep.setId(queryid);
						rep.setFindex(index);
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
			MgFavoritePath finalbrother = new MgFavoritePath();
			AbsTxAtom atom = new AbsTxAtom() {
				
				@Override
				public boolean noTxRun() {
					short index = (short) (finalbrother.getFindex() - 1);
					MgFavoritePath newpath = new MgFavoritePath();
					newpath.setId(path.getId());
					newpath.setFindex(index);
					boolean comit = newpath.update(); {
						if(!comit) {
							return failRet("修改失败");
						}
					}
					boolean start = false;
					for(Record query : records) {
						Integer queryid = query.getInt("id");
						if(Objects.equals(queryid, finalbrother.getId())) {
							start = true;
							continue;
						}
						if(!start) {
							continue;
						}
						if(Objects.equals(queryid, path.getId())) {
							continue;
						}
						index--;
						MgFavoritePath rep = new MgFavoritePath();
						rep.setId(queryid);
						rep.setFindex(index);
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
		return RetUtils.okOper();
	}
}

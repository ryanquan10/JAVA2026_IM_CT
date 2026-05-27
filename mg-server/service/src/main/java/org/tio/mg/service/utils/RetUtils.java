
package org.tio.mg.service.utils;

import java.util.ArrayList;
import java.util.List;

import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.service.jf.TioModel;
import org.tio.mg.service.service.atom.AbsTxAtom;

/**
 * Ret工具类
 * @author xufei
 * 2020年1月7日 上午10:11:46
 */
public class RetUtils  {

	public static final String INVALID_PARAMETER = "无效参数";
	
	public static final String SYS_ERROR = "系统错误";
	
	public static final String VERSION_ERROR = "版本不一致导致的数据错误";
	
	public static final String GRANT_ERROR = "权限不足";
	
	public static final String NOT_EXIST = "记录不存在";
	
	public static final String EXIST = "记录已存在";
	
	public static final String LOGIN_ERROR = "登录信息不一致";
	
	public static final String OPER_RIGHT = "操作成功";
	
	public static final String OPER_ERROR = "操作失败";
	
	public static final String NO_IMPLEMENT = "暂不支持";
	
	public static interface Code {
		
		/**
		 * 
		 */
		short OK = 1;
		
		/**
		 *  已存在
		 */
		short EXIST = 101;
		
	}
	
	/**
	 * 失败Ret
	 * @param msg
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:33:57
	 */
	public static Ret failMsg(String msg) {
		return Ret.fail().set("msg",msg);
	}
	
	/**
	 * @param ret
	 * @param key
	 * @return
	 * @author xufei
	 * 2020年1月15日 下午6:22:15
	 */
	public static String retKey(Ret ret,String key) {
		return ret.getStr(key);
	}
	
	/**
	 * @param ret
	 * @param key
	 * @return
	 * @author xufei
	 * 2020年1月15日 下午6:25:18
	 */
	public static Long retLongKey(Ret ret,String key) {
		Object object = ret.get(key);
		if(object != null) {
			return (Long) object;
		}
		return null;
	}
	
	/**
	 * @param msg
	 * @param code
	 * @return
	 * @author xufei
	 * 2020年1月9日 下午5:36:58
	 */
	public static Ret failMsg(String msg,Short code) {
		return Ret.fail().set("msg",msg).set("code",code);
	}
	
	
	/**
	 * @param msg
	 * @param code
	 * @return
	 * @author xufei
	 * 2020年1月20日 下午1:56:05
	 */
	public static Ret failMsg(String msg,Integer code) {
		return Ret.fail().set("msg",msg).set("code",code);
	}
	
	/**
	 * 无效参数
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午3:16:57
	 */
	public static Ret invalidParam() {
		return Ret.fail().set("msg",INVALID_PARAMETER);
	}
	
	/**
	 * @return
	 * @author xufei
	 * 2020年2月11日 下午12:41:21
	 */
	public static Ret noImplement() {
		return Ret.fail().set("msg",NO_IMPLEMENT);
	}
	
	/**
	 * 记录不存在
	 * @return
	 * @author xufei
	 * 2020年2月3日 下午12:34:18
	 */
	public static Ret noExistParam() {
		return Ret.fail().set("msg",NOT_EXIST);
	}
	
	/**
	 * 记录存在
	 * @return
	 * @author xufei
	 * 2020年2月4日 下午10:13:59
	 */
	public static Ret existParam() {
		return Ret.fail().set("msg",EXIST);
	}
	
	/**
	 * 权限不足
	 * @return
	 * @author xufei
	 * 2020年1月21日 上午10:29:03
	 */
	public static Ret grantError() {
		return Ret.fail().set("msg",GRANT_ERROR);
	}
	
	/**
	 * 版本异常
	 * @return
	 * @author xufei
	 * 2020年2月24日 上午11:43:12
	 */
	public static Ret versionError() {
		return Ret.fail().set("msg",VERSION_ERROR);
	}
	
	/**
	 * 系统异常
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午3:18:15
	 */
	public static Ret sysError() {
		return Ret.fail().set("msg",SYS_ERROR);
	}
	
	/**
	 * 成功ret
	 * @param msg
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:33:55
	 */
	public static Ret okMsg(String msg) {
		return Ret.ok().set("msg",msg);
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午5:23:18
	 */
	public static Ret setCode(Short code) {
		return Ret.ok().set("code",code);
	}
	
	/**
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午5:23:17
	 */
	public static Short getCode(Ret ret) {
		Object object = ret.get("code");
		if(object == null) {
			return null;
		}
		return (Short) object;
	}
	
	/**
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月20日 下午1:57:58
	 */
	public static Integer getIntCode(Ret ret) {
		Object object = ret.get("code");
		if(object == null) {
			return null;
		}
		return (Integer) object;
	}
	
	/**
	 * data数据
	 * @param data
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:35:05
	 */
	public static Ret okData(Object data) {
		return Ret.ok().set("data",data);
	}
	
	/**
	 * 操作成功
	 * @return
	 * @author xufei
	 * 2020年2月4日 下午9:47:33
	 */
	public static Ret okOper() {
		return okData(OPER_RIGHT);
	}
	
	/**
	 * 操作失败
	 * @return
	 * @author xufei
	 * 2020年2月4日 下午9:48:32
	 */
	public static Ret failOper() {
		return failMsg(OPER_ERROR);
	}
	
	/**
	 * 获取data数据
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:36:57
	 */
	public static Object getOkList(Ret ret) {
		return ret.get("list");
	}
	
	/**
	 * @param list
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午5:23:23
	 */
	public static Ret okList(Object list) {
		return Ret.ok().set("list",list);
	}
	
	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午5:23:25
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getOkTList(Ret ret) {
		Object object = getOkList(ret);
		if(object != null) {
			return (List<T>) object;
		}
		return null;
	}
	
	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月8日 下午5:23:28
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> getOkTArrayList(Ret ret) {
		Object object = getOkList(ret);
		if(object != null) {
			return (ArrayList<T>) object;
		}
		return null;
	}
	
	/**
	 * 获取data数据
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:36:57
	 */
	public static Object getOkData(Ret ret) {
		return ret.get("data");
	}
	
	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:41:21
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOkTData(Ret ret) {
		Object object = getOkData(ret);
		if(object != null) {
			return (T) object;
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getOkTData(Ret ret,String key) {
		Object object = ret.get(key);
		if(object != null) {
			return (T) object;
		}
		return null;
	}
	
	/**
	 * ret分页数据
	 * @param page
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:36:27
	 */
	public static Ret okPage(Object page) {
		return Ret.ok().set("page",page);
	}
	
	/**
	 * ret page数据
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:36:27
	 */
	public static Object getOkPage(Ret ret) {
		return ret.get("page");
	}
	
	/**
	 * @param ret
	 * @return
	 * @author xufei
	 * 2020年1月6日 下午1:33:52
	 */
	public static String getRetMsg(Ret ret) {
		return ret.getStr("msg");
	}
	
	/**
	 * 新增ret
	 * @param model
	 * @return
	 * @author xufei
	 * 2020年6月18日 上午10:28:20
	 */
	public static Ret saveRet(TioModel<?> model) {
		if(model.save()) {
			return okOper();
		}
		return failMsg(OPER_ERROR);
	}
	
	/**
	 * 事务ret
	 * @param atom
	 * @param db
	 * @return
	 * @author xufei
	 * 2020年6月18日 上午11:30:05
	 */
	public static Ret atomRet(AbsTxAtom atom,String db) {
		boolean commit = Db.use(db).tx(atom);
		if(!commit) {
			return atom.getRetObj();
		}
		return okOper();
	}
	
	/**
	 * 修改ret
	 * @param model
	 * @return
	 * @author xufei
	 * 2020年6月18日 上午10:30:26
	 */
	public static Ret upateRet(TioModel<?> model) {
		if(model.update()) {
			return okOper();
		}
		return failMsg(OPER_ERROR);
	}
	
	public static void main(String[] args) {
	}
}

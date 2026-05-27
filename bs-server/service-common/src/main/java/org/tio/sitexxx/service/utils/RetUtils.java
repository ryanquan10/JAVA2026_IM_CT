
package org.tio.sitexxx.service.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.RetCode.CommonCode;
import org.tio.utils.resp.Resp;

/**
 * Ret工具类
 * @author lixinji
 * 2020年1月7日 上午10:11:46
 */
public class RetUtils {

	public static final String VERSION_ERROR = "版本不一致导致的数据错误";

	public static final String GRANT_ERROR = "权限不足";

	public static final String LOGIN_ERROR = "登录信息不一致";

	public static final String OPER_RIGHT = "操作成功";

	public static final String OPER_ERROR = "操作失败";

	public static final String NO_IMPLEMENT = "暂不支持";

	public static final String NULL_UID = "用户id为空";

	public static final String USER_NOT_EXIST = "用户不存在";

	public static final String INVALID_USER = "用户已注销";

	public static final String NOT_REALNAME_USER = "未实名用户";

	/**
	 * @param msg
	 * @param code
	 * @return
	 * @author lixinji
	 * 2020年1月9日 下午5:36:58
	 */
	public static Ret failMsg(String msg,Short code) {
		return Ret.fail().set("msg", msg).set("code", code);
	}
	
	/**
	 * @param msg
	 * @param code
	 * @return
	 * @author lixinji
	 * 2020年1月20日 下午1:56:05
	 */
	public static Ret failMsg(String msg,Integer code) {
		return Ret.fail().set("msg", msg).set("code", code);
	}
	
	
	/**
	 * 失败Ret
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:33:57
	 */
	public static Ret failMsg(String msg) {
		return failMsg(msg,CommonCode.BIZ_ERROR.code);
	}

	/**
	 * 无效参数
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午3:16:57
	 */
	public static Ret invalidParam() {
		return failMsg(CommonCode.PARAM_ERROR.value, CommonCode.PARAM_ERROR.code);
	}

	/**
	 * 未实名用户
	 * @return
	 * @author lixinji
	 * 2021年2月3日 下午4:36:59
	 */
	public static Ret noRealnameUser() {
		return failMsg(NOT_REALNAME_USER,CommonCode.BIZ_ERROR.code);
	}

	/**
	 * 无效用户
	 * @return
	 * @author lixinji
	 * 2021年2月3日 下午3:51:36
	 */
	public static Ret invalidUser() {
		return failMsg(INVALID_USER);
	}

	/**
	 * 用户不存在
	 * @return
	 * @author lixinji
	 * 2021年2月3日 下午3:51:57
	 */
	public static Ret notExistUser() {
		return failMsg(USER_NOT_EXIST,CommonCode.BIZ_NOT_EXIST.code);
	}

	/**
	 * 用户uid为空
	 * @return
	 * @author lixinji
	 * 2021年2月3日 下午3:32:14
	 */
	public static Ret invalidUid() {
		return failMsg(NULL_UID,CommonCode.PARAM_ERROR.code);
	}

	/**
	 * @return
	 * @author lixinji
	 * 2020年2月11日 下午12:41:21
	 */
	public static Ret noImplement() {
		return failMsg(NO_IMPLEMENT);
	}

	/**
	 * 记录不存在
	 * @return
	 * @author lixinji
	 * 2020年2月3日 下午12:34:18
	 */
	public static Ret noExistParam() {
		return failMsg(CommonCode.BIZ_NOT_EXIST.value,CommonCode.BIZ_NOT_EXIST.code);
	}

	/**
	 * 记录存在
	 * @return
	 * @author lixinji
	 * 2020年2月4日 下午10:13:59
	 */
	public static Ret existParam() {
		return failMsg(CommonCode.BIZ_EXIST.value,CommonCode.BIZ_EXIST.code);
	}

	/**
	 * 权限不足
	 * @return
	 * @author lixinji
	 * 2020年1月21日 上午10:29:03
	 */
	public static Ret grantError() {
		return failMsg(CommonCode.GRANT_ERROR.value,CommonCode.GRANT_ERROR.code);
	}

	/**
	 * 版本异常
	 * @return
	 * @author lixinji
	 * 2020年2月24日 上午11:43:12
	 */
	public static Ret versionError() {
		return failMsg(VERSION_ERROR);
	}

	/**
	 * 系统异常
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午3:18:15
	 */
	public static Ret sysError() {
		return failMsg(CommonCode.SYS_ERROR.value, CommonCode.SYS_ERROR.code);
	}
	
	
	/**
	 * 成功ret
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:33:55
	 */
	public static Ret okMsg(String msg) {
		return Ret.ok().set("msg", msg);
	}
	
	/**
	 * 
	 * @param code
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:23:18
	 */
	public static Ret setCode(Short code) {
		return Ret.ok().set("code", code);
	}
	
	/**
	 * @param ret
	 * @param key
	 * @return
	 * @author lixinji
	 * 2020年1月15日 下午6:22:15
	 */
	public static String retKey(Ret ret, String key) {
		if(ret == null) {
			return "";
		}
		return ret.getStr(key);
	}

	/**
	 * @param ret
	 * @param key
	 * @return
	 * @author lixinji
	 * 2020年1月15日 下午6:25:18
	 */
	public static Long retLongKey(Ret ret, String key) {
		if(ret == null) {
			return null;
		}
		Object object = ret.get(key);
		if (object != null) {
			return (Long) object;
		}
		return null;
	}

	/**
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:23:17
	 */
	public static Short getCode(Ret ret) {
		if(ret == null) {
			return null;
		}
		Object object = ret.get("code");
		if (object == null) {
			return null;
		}
		return (Short) object;
	}

	/**
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月20日 下午1:57:58
	 */
	public static Integer getIntCode(Ret ret) {
		if(ret == null) {
			return null;
		}
		Object object = ret.get("code");
		if (object == null) {
			return null;
		}
		return (Integer) object;
	}

	/**
	 * data数据
	 * @param data
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:35:05
	 */
	public static Ret okData(Object data) {
		return Ret.ok().set("data", data);
	}

	/**
	 * 操作成功
	 * @return
	 * @author lixinji
	 * 2020年2月4日 下午9:47:33
	 */
	public static Ret okOper() {
		return okData(OPER_RIGHT);
	}

	/**
	 * 操作失败
	 * @return
	 * @author lixinji
	 * 2020年2月4日 下午9:48:32
	 */
	public static Ret failOper() {
		return failMsg(OPER_ERROR);
	}

	/**
	 * 获取data数据
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:36:57
	 */
	public static Object getOkList(Ret ret) {
		if(ret == null) {
			return null;
		}
		return ret.get("list");
	}

	/**
	 * @param list
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:23:23
	 */
	public static Ret okList(Object list) {
		return Ret.ok().set("list", list);
	}

	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:23:25
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getOkTList(Ret ret) {
		if(ret == null) {
			return null;
		}
		Object object = getOkList(ret);
		if (object != null) {
			return (List<T>) object;
		}
		return null;
	}

	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月8日 下午5:23:28
	 */
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> getOkTArrayList(Ret ret) {
		if(ret == null) {
			return null;
		}
		Object object = getOkList(ret);
		if (object != null) {
			return (ArrayList<T>) object;
		}
		return null;
	}

	/**
	 * 获取data数据
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:36:57
	 */
	public static Object getOkData(Ret ret) {
		return ret.get("data");
	}

	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:41:21
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOkTData(Ret ret) {
		if(ret == null) {
			return null;
		}
		Object object = getOkData(ret);
		if (object != null) {
			return (T) object;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getOkTData(Ret ret, String key) {
		if(ret == null) {
			return null;
		}
		Object object = ret.get(key);
		if (object != null) {
			return (T) object;
		}
		return null;
	}

	/**
	 * @param <T>
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年6月10日 上午10:22:47
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getOkTPage(Ret ret) {
		if(ret == null) {
			return null;
		}
		Object object = ret.get("page");
		if (object != null) {
			return (T) object;
		}
		return null;
	}

	/**
	 * ret分页数据
	 * @param page
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:36:27
	 */
	public static Ret okPage(Object page) {
		return Ret.ok().set("page", page);
	}

	/**
	 * ret page数据
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:36:27
	 */
	public static Object getOkPage(Ret ret) {
		return ret.get("page");
	}

	/**
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2020年1月6日 下午1:33:52
	 */
	public static String getRetMsg(Ret ret) {
		return ret.getStr("msg");
	}


	/**
	 * 获取错误Resp
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2021年6月17日 下午5:01:00
	 */
	public static Resp getFailResp(Ret ret) {
		Resp resp = Resp.fail();
		resp.code(RetUtils.getIntCode(ret)).msg(RetUtils.getRetMsg(ret));
		return resp;
	}
	
	/**
	 * 获取无效参数的resp
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:30:42
	 */
	public static Resp getInvalidResp() {
		return getFailResp(invalidParam());
	}
	
	/**
	 * 获取错误resp
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:29:18
	 */
	public static Resp getFailResp(String msg) {
		Resp resp = Resp.fail();
		resp.code(CommonCode.BIZ_ERROR.code).msg(msg);
		return resp;
	}
	
	/**
	 * 获取系统错误resp
	 * @return
	 * @author lixinji
	 * 2021年6月28日 下午1:50:58
	 */
	public static Resp getSysErrorResp() {
		Resp resp = Resp.fail();
		resp.code(CommonCode.SYS_ERROR.code).msg(CommonCode.SYS_ERROR.value);
		return resp;
	}
	
	/**
	 * 获取正确的List数据Resp
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:14:18
	 */
	public static Resp getListResp(Ret ret) {
		return Resp.ok(RetUtils.getOkList(ret));
	}
	
	/**
	 * 获取正确的分页数据Resp
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:14:48
	 */
	public static Resp getPageResp(Ret ret) {
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 获取正确的对象Resp
	 * @param ret
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:15:17
	 */
	public static Resp getDataResp(Ret ret) {
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
	/**
	 * 获取正确的oper操作Resp
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:15:58
	 */
	public static Resp getOperResp() {
		return Resp.ok(OPER_RIGHT);
	}
	
	/**
	 * 获取正确的oper操作
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2021年6月24日 下午4:32:26
	 */
	public static Resp getOperResp(String msg) {
		return Resp.ok(msg);
	}
	
	/**
	 * @param value
	 * @return
	 * @author lixinji
	 * 2021年6月30日 下午6:54:30
	 */
	public static Short toByte(Boolean value,Short def) {
		if(value == null) {
			return def;
		}
		return value ? Const.YesOrNo.YES : Const.YesOrNo.NO;
	}
	
	
	/**
	 * @param value
	 * @return
	 * @author lixinji
	 * 2021年7月9日 上午11:04:11
	 */
	public static boolean toBool(Short value,boolean def) {
		if(value == null) {
			return def;
		}
		return Objects.equals(value, Const.YesOrNo.YES) ? true : false;
	}

	public static void main(String[] args) {
	}
}

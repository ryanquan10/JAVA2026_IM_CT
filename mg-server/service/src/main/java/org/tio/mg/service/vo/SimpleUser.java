
package org.tio.mg.service.vo;

import java.io.Serializable;
import java.util.List;

import org.tio.mg.service.model.main.IpInfo;
import org.tio.mg.service.model.main.User;
import org.tio.mg.service.model.main.UserAgent;
import org.tio.mg.service.service.base.UserService;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.utils.SystemTimer;

import cn.hutool.core.util.NumberUtil;

/**
 * 字段简化版的User，用于传递给其它用户展示的
 * @author tanyaowu 
 * 2016年10月25日 下午2:37:57
 */
public class SimpleUser implements Serializable {

	private static final long	serialVersionUID	= 5240107764998103700L;
	/**
	 * 可能是这个用户
	 */
	private SimpleUser			may					= null;
	/**
	 * id
	 */
	private Integer				i;
	/**
	 * connection id， tcp连接唯一标识，对应channelContext的id字段
	 */
	private String				cid;
	/**
	 * 昵称
	 */
	private String				n					= null;
	/**
	 * 1：机器人
	 * 2：正常人
	 */
	private Short				x					= 2;
	/**
	 * 角色
	 */
	private List<Short>			r;
	/**
	 * 等级
	 */
	private Integer				l;
	/**
	 * 头像
	 */
	private String				a;
	/**
	 * 手机号码
	 */
	private String				phone				= null;
	/**
	 * loginname登录名
	 */
	private String				ln					= null;
	/**
	 * 连接创建时间（和ChannelContext绑定时，此值才有意义）
	 */
	private long				timeCreated			= SystemTimer.currTime;
	/**
	 * 进入群组的时间
	 */
	private long				timeJoinGroup		= SystemTimer.currTime;
	/**
	 * 对于channel，当前所在的groupid
	 */
	private String				groupid;
	/**
	 * ip信息
	 */
	private IpInfo				ipInfo;
	/**
	 * 设备信息，手机端才有
	 */
	private MobileInfo			mobileInfo;
	/**
	 * 浏览器信息，PC才有
	 */
	private UserAgent			userAgent;

	/**
	 * 根据user对象创建SimpleUser对象，建议不要用反射，添加字段时，注意修改本方法
	 * @param user
	 * @return
	 * @author tanyaowu
	 */
	public static SimpleUser fromUser(User user) {
		if (user == null) {
			return null;
		}

		SimpleUser ret = new SimpleUser();
		ret.setI(user.getId());
		ret.setN(user.getNick());
		ret.setR(user.getRoles());
		ret.setA(user.getAvatar());
		ret.setL(user.getLevel());

		if (user.getXx() != null && user.getXx() != (short) 1) {
			if (NumberUtil.isNumber(user.getLoginname())) {
				ret.setPhone(user.getLoginname());
			}
		}
		ret.setLn(user.getLoginname());

		ret.setX(user.getXx());
		return ret;
	}

	/**
	 * 
	 * @param uid
	 * @return
	 * @author tanyaowu
	 */
	public static SimpleUser fromUid(Integer uid) {
		if (uid == null) {
			return null;
		}
		/**
		 * TO-USER-缺少用户状态处理，根据业务处理
		 */
		User user = UserService.ME.getById(uid);
		return fromUser(user);
	}

	/**
	 * userid
	 * @return
	 */
	public Integer getI() {
		return i;
	}

	/**
	 * userid
	 * @param i
	 */
	public void setI(Integer i) {
		this.i = i;
	}

	/**
	 * nick
	 * @return
	 */
	public String getN() {
		return n;
	}

	/**
	 * nick
	 * @param n
	 */
	public void setN(String n) {
		this.n = n;
	}

	/**
	 * 角色
	 * @return
	 */
	public List<Short> getR() {
		return r;
	}

	/**
	 * 角色
	 * @param r
	 */
	public void setR(List<Short> r) {
		this.r = r;
	}

	/**
	 * 等级
	 * @return
	 */
	public Integer getL() {
		return l;
	}

	/**
	 * 等级
	 * @param l
	 */
	public void setL(Integer l) {
		this.l = l;
	}

	/**
	 * 头像
	 * @return
	 */
	public String getA() {
		return a;
	}

	/**
	 * 头像
	 */
	public void setA(String a) {
		this.a = a;
	}

	/**
	 * 
	 * @author tanyaowu
	 */
	public SimpleUser() {
	}

	/**
	 * connection id， tcp连接唯一标识，对应channelContext的id字段
	 * @return
	 */
	public String getCid() {
		return cid;
	}

	/**
	 * connection id， tcp连接唯一标识，对应channelContext的id字段
	 */
	public void setCid(String cid) {
		this.cid = cid;
	}

	public IpInfo getIpInfo() {
		return ipInfo;
	}

	public void setIpInfo(IpInfo ipInfo) {
		this.ipInfo = ipInfo;
	}

	/**
	 * 设备信息，手机端才有
	 * @return
	 */
	public MobileInfo getMobileInfo() {
		return mobileInfo;
	}

	/**
	 * 设备信息，手机端才有
	 */
	public void setMobileInfo(MobileInfo mobileInfo) {
		this.mobileInfo = mobileInfo;
	}

	/**
	 * 浏览器信息，PC才有
	 * @return
	 */
	public UserAgent getUserAgent() {
		return userAgent;
	}

	/**
	 * 浏览器信息，PC才有
	 */
	public void setUserAgent(UserAgent userAgent) {
		this.userAgent = userAgent;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Short getX() {
		return x;
	}

	public void setX(Short x) {
		this.x = x;
	}

	/**
	 * loginname登录名
	 * @return the ln
	 */
	public String getLn() {
		return ln;
	}

	/**
	 * loginname登录名
	 * @param ln the ln to set
	 */
	public void setLn(String ln) {
		this.ln = ln;
	}

	/**
	 * @return the groupid
	 */
	public String getGroupid() {
		return groupid;
	}

	/**
	 * @param groupid the groupid to set
	 */
	public void setGroupid(String groupid) {
		this.groupid = groupid;
	}

	/**
	 * @return the may
	 */
	public SimpleUser getMay() {
		return may;
	}

	/**
	 * @param may the may to set
	 */
	public void setMay(SimpleUser may) {
		this.may = may;
	}

	public long getTimeJoinGroup() {
		return timeJoinGroup;
	}

	public void setTimeJoinGroup(long timeJoinGroup) {
		this.timeJoinGroup = timeJoinGroup;
	}
}

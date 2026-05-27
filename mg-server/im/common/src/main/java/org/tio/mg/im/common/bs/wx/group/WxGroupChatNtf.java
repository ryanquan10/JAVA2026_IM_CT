
/**
 *
 */
package org.tio.mg.im.common.bs.wx.group;

import java.io.Serializable;
import java.util.Objects;

import org.tio.mg.service.model.main.Audio;
import org.tio.mg.service.model.main.File;
import org.tio.mg.service.model.main.Img;
import org.tio.mg.service.model.main.Video;
import org.tio.mg.service.model.main.WxGroupMsg;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.utils.SystemTimer;
import org.tio.utils.json.Json;

/**
 * 新版群聊通知
 * @author xufei
 * 2020年2月13日 下午6:40:28
 */
public class WxGroupChatNtf implements Serializable {
	private static final long	serialVersionUID	= -7027325613034796001L;
	private String				c					= null;					//聊天内容
	private Long				g					= null;					//groupid
	private Short				d					= 1;					//主聊人员的设备类型（DeviceType），1：WS，2：安卓，3：IOS
	private Integer				f					= null;					//from的简写，聊天发起人信息
	private String			at					= null;					//被艾特的用户信息
	private Long				t					= SystemTimer.currTime;	//聊天消息时间
	private Short				ct					= null;					//WxGroupMsg.ContentType 1、普通文本消息，2、博客，3、文件，4、音频，5、视频，6：图片
	private Long				mid					= null;					//msg id 消息id，全局唯一
	private Short				sendbysys			= 2;					//是否是系统发出的消息，参见：Const.Sendbysys.YES，1：是，2或null：不是
	private Short				actflag				= 2;					//该消息是否是激活聊天消息
	private String				nick;										//发送方昵称
	private String				avatar;										//发送方头像
	private Long 				chatlinkid;									//聊天列表id
	
	private String 				sysmsgkey;									//系统消息模板key
	private String 				opernick;									//系统消息操作者
	private String 				tonicks;									//系统消息被操作者
	
	private Video				vc;											//视频内容
	private String				bc;											//博客内容
	private Audio				ac;											//音频内容
	private File				fc;											//文件内容
	private Img 				ic;											//图片内容
	private WxMsgCardVo 				cardc;										//名片内容
	
	private String				actname;									//激活时的名称
	private String				actavatar;									//激活时的头像
	private Short				joinnum;									//激活时告诉多少用户
	private Short				grouprole;									//激活时群的角色
	
	/**
	 * @param msg
	 * @return
	 * @author xufei
	 * 2020年2月13日 下午9:39:02
	 */
	public static WxGroupChatNtf from(WxGroupMsg msg,SysMsgVo sysMsgVo) {
		WxGroupChatNtf reslut = new WxGroupChatNtf();
		reslut.setNick(msg.getNick());
		reslut.setAvatar(msg.getAvatar());
		reslut.setC(msg.getText());
		switch (msg.getContenttype()) {
			case Const.ContentType.TEXT:
				//保存缩略文字
				if(Objects.equals(msg.getSendbysys(), Const.YesOrNo.YES)) {
					if(sysMsgVo != null) {
						reslut.setOpernick(sysMsgVo.getOpernick());
						reslut.setSysmsgkey(sysMsgVo.getMsgkey());
						reslut.setTonicks(sysMsgVo.getTonicks());
					}
				}
				break;
			case Const.ContentType.AUDIO:
				Audio audio = Json.toBean(msg.getText(), Audio.class);
				//保存音频
				reslut.setAc(audio);
				break;
			case Const.ContentType.BLOG:
				//保存微博
				reslut.setBc(msg.getText());
				break;
			case Const.ContentType.FILE:
				File file = Json.toBean(msg.getText(), File.class);
				//保存文件
				reslut.setFc(file);
				break;
			case Const.ContentType.IMG:
				Img img = Json.toBean(msg.getText(), Img.class);
				//保存图片
				reslut.setIc(img);
				break;
			case Const.ContentType.VIDEO:
				Video video = Json.toBean(msg.getText(), Video.class);
				//保存视频
				reslut.setVc(video);
				break;
			case Const.ContentType.MSG_CARD:
				WxMsgCardVo cardVo = Json.toBean(msg.getText(), WxMsgCardVo.class);
				//保存名片
				reslut.setCardc(cardVo);
				break;
			default:
		}
		reslut.setCt(msg.getContenttype());
		reslut.setMid(msg.getId());
		reslut.setT(msg.getTime().getTime());
		reslut.setF(msg.getUid());
		reslut.setG(msg.getGroupid());
		reslut.setChatlinkid(-msg.getGroupid());
		reslut.setD(msg.getDevice());
		reslut.setSendbysys(msg.getSendbysys());
		return reslut;
	}

	public String getSysmsgkey() {
		return sysmsgkey;
	}

	public void setSysmsgkey(String sysmsgkey) {
		this.sysmsgkey = sysmsgkey;
	}

	public String getOpernick() {
		return opernick;
	}

	public void setOpernick(String opernick) {
		this.opernick = opernick;
	}

	public Video getVc() {
		return vc;
	}

	public void setVc(Video vc) {
		this.vc = vc;
	}

	public Audio getAc() {
		return ac;
	}

	public void setAc(Audio ac) {
		this.ac = ac;
	}

	public File getFc() {
		return fc;
	}

	public void setFc(File fc) {
		this.fc = fc;
	}

	public Img getIc() {
		return ic;
	}

	public void setIc(Img ic) {
		this.ic = ic;
	}

	public String getTonicks() {
		return tonicks;
	}

	public void setTonicks(String tonicks) {
		this.tonicks = tonicks;
	}

	public Short getGrouprole() {
		return grouprole;
	}

	public WxMsgCardVo getCardc() {
		return cardc;
	}

	public void setCardc(WxMsgCardVo cardc) {
		this.cardc = cardc;
	}

	public void setGrouprole(Short grouprole) {
		this.grouprole = grouprole;
	}

	public Short getJoinnum() {
		return joinnum;
	}

	public void setJoinnum(Short joinnum) {
		this.joinnum = joinnum;
	}

	public String getBc() {
		return bc;
	}

	public void setBc(String bc) {
		this.bc = bc;
	}

	public String getActname() {
		return actname;
	}

	public void setActname(String actname) {
		this.actname = actname;
	}

	public String getActavatar() {
		return actavatar;
	}

	public void setActavatar(String actavatar) {
		this.actavatar = actavatar;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Long getG() {
		return g;
	}

	public void setG(Long g) {
		this.g = g;
	}

	public Integer getF() {
		return f;
	}

	public void setF(Integer f) {
		this.f = f;
	}

	public String getAt() {
		return at;
	}

	public void setAt(String at) {
		this.at = at;
	}

	public Long getT() {
		return t;
	}

	public void setT(Long t) {
		this.t = t;
	}

	public Short getD() {
		return d;
	}

	public void setD(Short d) {
		this.d = d;
	}

	//	public Short getHs() {
	//		return hs;
	//	}
	//
	//	public void setHs(Short hs) {
	//		this.hs = hs;
	//	}

	public Short getCt() {
		return ct;
	}

	public void setCt(Short ct) {
		this.ct = ct;
	}

	/**
	 * @return the mid
	 */
	public Long getMid() {
		return mid;
	}

	/**
	 * @param mid the mid to set
	 */
	public void setMid(Long mid) {
		this.mid = mid;
	}

	public Short getSendbysys() {
		return sendbysys;
	}

	public void setSendbysys(Short sendbysys) {
		this.sendbysys = sendbysys;
	}

	public Short getActflag() {
		return actflag;
	}

	public void setActflag(Short actflag) {
		this.actflag = actflag;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}
}

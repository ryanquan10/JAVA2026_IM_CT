
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.group;

import java.io.Serializable;
import java.util.Objects;

import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.chat.ChatIndexService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.service.vo.wx.WxGroupApplyVo;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.sitexxx.service.vo.wx.WxPositionVo;
import org.tio.sitexxx.service.vo.wx.WxRedVo;
import org.tio.sitexxx.service.vo.wx.WxTemplateMsgVo;
import org.tio.utils.SystemTimer;
import org.tio.utils.json.Json;

/**
 * 新版群聊通知
 * @author lixinji
 * 2020年2月13日 下午6:40:28
 */
public class WxGroupChatNtf implements Serializable {
	private static final long	serialVersionUID	= -7027325613034796001L;
	private String				c					= null;					//聊天内容
	private Long				g					= null;					//groupid
	private Short				d					= 1;					//主聊人员的设备类型（DeviceType），1：WS，2：安卓，3：IOS
	private Integer				f					= null;					//from的简写，聊天发起人信息
	private String				at					= null;					//被艾特的用户信息
	private Long				t					= SystemTimer.currTime;	//聊天消息时间
	private Short				ct					= null;					//WxGroupMsg.ContentType 1、普通文本消息，2、博客，3、文件，4、音频，5、视频，6：图片
	private Long				mid					= null;					//msg id 消息id，全局唯一
	private Short				sendbysys			= 2;					//是否是系统发出的消息，参见：Const.Sendbysys.YES，1：是，2或null：不是
	private Short				actflag				= 2;					//该消息是否是激活聊天消息
	private String				nick;										//发送方昵称
	private String				avatar;										//发送方头像
	private Long				chatlinkid;									//聊天列表id

	private String	sysmsgkey;	//系统消息模板key
	private String	opernick;	//系统消息操作者
	private String	tonicks;	//系统消息被操作者

	private Video			vc;			//视频内容
	private String			bc;			//博客内容
	private Audio			ac;			//音频内容
	private File			fc;			//文件内容
	private Img				ic;			//图片内容
	private WxMsgCardVo		cardc;		//名片内容
	private WxRedVo			red;		//红包订单号
	private WxGroupApplyVo	apply;		//红包订单号
	private WxTemplateMsgVo	temp;		//模板消息
	private WxPositionVo	position;		//位置消息
	private Short			msgfreeflag;

	private String	actname;					//激活时的名称
	private String	actavatar;					//激活时的头像
	private Short	joinnum;					//激活时告诉多少用户
	private Short	grouprole;					//激活时群的角色
	private Short	redflag	= Const.YesOrNo.NO;	//红包通知
	private Short	updatelist	= Const.YesOrNo.NO;	//群页面是否需要刷新
	private Short	openforbidden	= Const.YesOrNo.NO;	//1:禁言 2:解除禁言
	private String	touid	= null;	//指定用户

	private String quotemsgcontent;
	private String quotemid;
	private String quotemsgtype;
	private String quotesrcnick;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	private String label;

	/**
	 * @param msg
	 * @return
	 * @author lixinji
	 * 2020年2月13日 下午9:39:02
	 */
	public static WxGroupChatNtf from(WxGroupMsg msg, SysMsgVo sysMsgVo) {
		WxGroupChatNtf reslut = new WxGroupChatNtf();
		reslut.setNick(msg.getNick());
		reslut.setAvatar(msg.getAvatar());
		reslut.setC(msg.getText());
		switch (msg.getContenttype()) {
			case Const.ContentType.QUOTE_MSG:
				//保存缩略文字
				if (Objects.equals(msg.getSendbysys(), Const.YesOrNo.YES)) {
					if (sysMsgVo != null) {
						reslut.setOpernick(sysMsgVo.getOpernick());
						reslut.setSysmsgkey(sysMsgVo.getMsgkey());
						reslut.setTonicks(sysMsgVo.getTonicks());
					}
				}
				reslut.setQuotemid(msg.getQuotemid().toString());
				reslut.setQuotesrcnick(msg.getQuotesrcnick());
				reslut.setQuotemsgcontent(msg.getQuotemsgcontent());
				reslut.setQuotemsgtype(msg.getQuotemsgtype().toString());
				break;
			case Const.ContentType.TEXT:
				//保存缩略文字
				if (Objects.equals(msg.getSendbysys(), Const.YesOrNo.YES)) {
					if (sysMsgVo != null) {
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
			case Const.ContentType.REDPACKET:
				WxRedVo redVo = Json.toBean(msg.getText(), WxRedVo.class);
				reslut.setRed(redVo);
				break;
			case Const.ContentType.GROUP_APPLY:
				WxGroupApplyVo applyVo = Json.toBean(msg.getText(), WxGroupApplyVo.class);
				reslut.setApply(applyVo);
				break;
			case Const.ContentType.TEMPLATE:
				WxTemplateMsgVo tempVo = Json.toBean(msg.getText(), WxTemplateMsgVo.class);
				reslut.setTemp(tempVo);
				break;
			case Const.ContentType.POSITION:
				WxPositionVo positionVo = Json.toBean(msg.getText(), WxPositionVo.class);
				reslut.setPosition(positionVo);
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
		WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(msg.getUid(), msg.getGroupid());
		if (groupItem != null) {
			reslut.setGrouprole(groupItem.getGrouprole());
			WxGroup group = WxGroup.dao.findById(msg.getGroupid());
			if (groupItem.getGrouprole().equals((short)1)) {
				reslut.setLabel(group.getGroupleaderlabel() != null && !group.getGroupleaderlabel().isEmpty() ? group.getGroupleaderlabel() : "群主");
			} else if (groupItem.getGrouprole().equals((short)3)) {
				reslut.setLabel(group.getGroupmanagerlabel() != null && !group.getGroupmanagerlabel().isEmpty() ? group.getGroupmanagerlabel() : "管理员");
			} else {
				reslut.setLabel(group.getGroupmemberlabel() != null && !group.getGroupmemberlabel().isEmpty() ? group.getGroupmemberlabel() : "成员");
			}
		}
		return reslut;
	}
	
	public WxPositionVo getPosition() {
		return position;
	}

	public void setPosition(WxPositionVo position) {
		this.position = position;
	}

	public Short getMsgfreeflag() {
		return msgfreeflag;
	}

	public void setMsgfreeflag(Short msgfreeflag) {
		this.msgfreeflag = msgfreeflag;
	}

	public Short getRedflag() {
		return redflag;
	}

	public void setRedflag(Short redflag) {
		this.redflag = redflag;
	}

	public String getSysmsgkey() {
		return sysmsgkey;
	}

	public WxGroupApplyVo getApply() {
		return apply;
	}

	public void setApply(WxGroupApplyVo apply) {
		this.apply = apply;
	}

	public WxTemplateMsgVo getTemp() {
		return temp;
	}

	public void setTemp(WxTemplateMsgVo temp) {
		this.temp = temp;
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

	public WxRedVo getRed() {
		return red;
	}

	public void setRed(WxRedVo red) {
		this.red = red;
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

	public String getQuotemsgcontent() {
		return quotemsgcontent;
	}

	public void setQuotemsgcontent(String quotemsgcontent) {
		this.quotemsgcontent = quotemsgcontent;
	}

	public String getQuotemid() {
		return quotemid;
	}

	public void setQuotemid(String quotemid) {
		this.quotemid = quotemid;
	}

	public String getQuotemsgtype() {
		return quotemsgtype;
	}

	public void setQuotemsgtype(String quotemsgtype) {
		this.quotemsgtype = quotemsgtype;
	}

	public String getQuotesrcnick() {
		return quotesrcnick;
	}

	public void setQuotesrcnick(String quotesrcnick) {
		this.quotesrcnick = quotesrcnick;
	}

	public Short getUpdatelist() {
		return updatelist;
	}

	public void setUpdatelist(Short updatelist) {
		this.updatelist = updatelist;
	}

	public Short getOpenforbidden() {
		return openforbidden;
	}

	public void setOpenforbidden(Short openforbidden) {
		this.openforbidden = openforbidden;
	}

	public String getTouid() {
		return touid;
	}

	public void setTouid(String touid) {
		this.touid = touid;
	}
}

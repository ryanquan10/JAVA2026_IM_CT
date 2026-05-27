
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.friend;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.tio.sitexxx.service.model.main.Audio;
import org.tio.sitexxx.service.model.main.File;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.model.main.Video;
import org.tio.sitexxx.service.model.main.WxFriendMsg;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.WxCallItemVo;
import org.tio.sitexxx.service.vo.wx.WxMsgCardVo;
import org.tio.sitexxx.service.vo.wx.WxPositionVo;
import org.tio.sitexxx.service.vo.wx.WxRedVo;
import org.tio.sitexxx.service.vo.wx.WxTemplateMsgVo;
import org.tio.utils.json.Json;

/**
 * 朋友间的聊天通知-- Server-->Client
 * @author tanyaowu 
 * 2016年9月12日 下午3:09:08
 */
public class WxFriendChatNtf implements Serializable {
	private static final long	serialVersionUID	= 4487525418584644680L;
	private Long				mid;										//消息id，全局唯一，一条消息一个id
	private Long				t;											//消息发送时间
	private String				c;											//聊天内容
	private Short				ct					= null;					//WxFriendMsg.ContentType 1、普通文本消息，2、超链接卡片消息，3、文件，4、音频，5、视频
	private Integer				uid;										//发送方的userid
	private Integer				touid;										//接收方的userid
	private Short				sendbysys			= 2;					//该消息是否由系统发出，true：为系统发出，null/false：非系统发出
	private Date				readtime;
	private Short				readflag			= 2;
	private Short				actflag				= 2;					//该消息是否是激活聊天消息
	private Short				msgtype				= 1;					//消息的类型：1：正常消息：2：操作消息
	private String				nick;										//发送方昵称
	private String				avatar;										//发送方头像
	private Long				chatlinkid;									//聊天列表id

	private Video			vc;							//视频内容
	private String			bc;							//博客内容
	private Audio			ac;							//音频内容
	private File			fc;							//文件内容
	private Img				ic;							//图片内容
	private WxMsgCardVo		cardc;						//名片内容
	private WxCallItemVo	call;						//音视频通话内容
	private WxRedVo			red;						//红包订单号
	private Short			redflag	= Const.YesOrNo.NO;	//红包通知
	private WxTemplateMsgVo	temp;						//模板消息
	private WxPositionVo position;						//位置消息
	private Short			msgfreeflag;

	private String	actname;	//激活时的名称
	private String	actavatar;	//激活时的头像

	private String quotemsgcontent;
	private String quotemid;
	private String quotemsgtype;
	private String quotesrcnick;

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

	/**
	 * @param wxFriendMsg
	 * @return
	 * @author lixinji
	 * 2020年2月13日 下午9:35:48
	 */
	public static WxFriendChatNtf from(WxFriendMsg wxFriendMsg) {
		WxFriendChatNtf ret = new WxFriendChatNtf();
		ret.setC(wxFriendMsg.getText());
		switch (wxFriendMsg.getContenttype()) {
		case Const.ContentType.QUOTE_MSG:
			ret.setQuotemid(wxFriendMsg.getQuotemid().toString());
			ret.setQuotesrcnick(wxFriendMsg.getQuotesrcnick());
			ret.setQuotemsgcontent(wxFriendMsg.getQuotemsgcontent());
			ret.setQuotemsgtype(wxFriendMsg.getQuotemsgtype().toString());
			//保存缩略文字
			break;
		case Const.ContentType.TEXT:
			//保存缩略文字
			break;
		case Const.ContentType.AUDIO:
			Audio audio = Json.toBean(wxFriendMsg.getText(), Audio.class);
			//保存音频
			ret.setAc(audio);
			break;
		case Const.ContentType.BLOG:
			//保存微博
			ret.setBc(wxFriendMsg.getText());
			break;
		case Const.ContentType.FILE:
			File file = Json.toBean(wxFriendMsg.getText(), File.class);
			//保存文件
			ret.setFc(file);
			break;
		case Const.ContentType.IMG:
			Img img = Json.toBean(wxFriendMsg.getText(), Img.class);
			//保存图片
			ret.setIc(img);
			break;
		case Const.ContentType.VIDEO:
			Video video = Json.toBean(wxFriendMsg.getText(), Video.class);
			//保存视频
			ret.setVc(video);
			break;
		case Const.ContentType.MSG_CARD:
			WxMsgCardVo cardVo = Json.toBean(wxFriendMsg.getText(), WxMsgCardVo.class);
			//保存名片
			ret.setCardc(cardVo);
			break;
		case Const.ContentType.CALL_AUDIO:
			WxCallItemVo audioCall = Json.toBean(wxFriendMsg.getText(), WxCallItemVo.class);
			ret.setCall(audioCall);
			break;
		case Const.ContentType.CALL_VIDEO:
			WxCallItemVo videoCall = Json.toBean(wxFriendMsg.getText(), WxCallItemVo.class);
			ret.setCall(videoCall);
			break;
		case Const.ContentType.REDPACKET:
			WxRedVo redVo = Json.toBean(wxFriendMsg.getText(), WxRedVo.class);
			ret.setRed(redVo);
			break;
		case Const.ContentType.TEMPLATE:
			WxTemplateMsgVo tempVo = Json.toBean(wxFriendMsg.getText(), WxTemplateMsgVo.class);
			ret.setTemp(tempVo);
			break;
		case Const.ContentType.POSITION:
			WxPositionVo positionVo = Json.toBean(wxFriendMsg.getText(), WxPositionVo.class);
			ret.setPosition(positionVo);
			break;
		default:
		}
		ret.setCt(wxFriendMsg.getContenttype());
		ret.setMid(wxFriendMsg.getId());
		ret.setT(wxFriendMsg.getTime().getTime());
		ret.setTouid(wxFriendMsg.getTouid());
		ret.setUid(wxFriendMsg.getUid());
		ret.setReadflag(wxFriendMsg.getReadflag());
		if (Objects.equals(wxFriendMsg.getSendbysys(), Const.Sendbysys.YES)) {
			ret.setSendbysys(wxFriendMsg.getSendbysys());
		}
		ret.setMsgtype(wxFriendMsg.getMsgtype());
		ret.setReadtime(wxFriendMsg.getReadtime());
		return ret;
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

	public WxRedVo getRed() {
		return red;
	}

	public void setRed(WxRedVo red) {
		this.red = red;
	}

	public WxTemplateMsgVo getTemp() {
		return temp;
	}

	public void setTemp(WxTemplateMsgVo temp) {
		this.temp = temp;
	}

	public Short getRedflag() {
		return redflag;
	}

	public void setRedflag(Short redflag) {
		this.redflag = redflag;
	}

	public WxCallItemVo getCall() {
		return call;
	}

	public void setCall(WxCallItemVo call) {
		this.call = call;
	}

	public String getBc() {
		return bc;
	}

	public void setBc(String bc) {
		this.bc = bc;
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

	public WxMsgCardVo getCardc() {
		return cardc;
	}

	public void setCardc(WxMsgCardVo cardc) {
		this.cardc = cardc;
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

	public Long getMid() {
		return mid;
	}

	public void setMid(Long mid) {
		this.mid = mid;
	}

	public Long getT() {
		return t;
	}

	public void setT(Long t) {
		this.t = t;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}

	public Short getCt() {
		return ct;
	}

	public void setCt(Short ct) {
		this.ct = ct;
	}

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public Integer getTouid() {
		return touid;
	}

	public void setTouid(Integer touid) {
		this.touid = touid;
	}

	/**
	 * @return the readflag
	 */
	public Short getReadflag() {
		return readflag;
	}

	/**
	 * @param readflag the readflag to set
	 */
	public void setReadflag(Short readflag) {
		this.readflag = readflag;
	}

	public Short getSendbysys() {
		return sendbysys;
	}

	public void setSendbysys(Short sendbysys) {
		this.sendbysys = sendbysys;
	}

	public Date getReadtime() {
		return readtime;
	}

	public void setReadtime(Date readtime) {
		this.readtime = readtime;
	}

	public Short getActflag() {
		return actflag;
	}

	public void setActflag(Short actflag) {
		this.actflag = actflag;
	}

	public Short getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(Short msgtype) {
		this.msgtype = msgtype;
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

	public Long getChatlinkid() {
		return chatlinkid;
	}

	public void setChatlinkid(Long chatlinkid) {
		this.chatlinkid = chatlinkid;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}
}

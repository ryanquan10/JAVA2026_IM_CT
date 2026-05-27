
package org.tio.sitexxx.web.server.controller.wx;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.common.UploadFile;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.model.conf.ClientConf;
import org.tio.sitexxx.service.model.main.Img;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.service.ImgService;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.chat.ChatMsgService;
import org.tio.sitexxx.service.service.chat.ChatMsgService.MsgTemplate;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.ImgUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.wx.SysMsgVo;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.resp.Resp;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.util.EscapeUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 群信息接口
 * @author lixinji
 * 2020年11月7日 下午6:26:46
 */
@RequestPath(value = "/group")
public class GroupController {
	private static Logger log = LoggerFactory.getLogger(GroupController.class);

	/**
	 * 修改群简介
	 * @param request
	 * @param groupid
	 * @param intro
	 * @return
	 */
	@RequestPath(value = "/modifyIntro")
	public Resp modifyIntro(HttpRequest request, Long groupid, String intro) {
		User curr = WebUtils.currUser(request);
		Resp resp = GroupService.me.modifyIntro(curr, groupid, intro);
		return resp;
	}

	/**
	 * 修改群名字
	 * @param request
	 * @param groupid
	 * @param name
	 * @return
	 * @author lixinji
	 * 2020年11月13日 下午2:23:29
	 */
	@RequestPath(value = "/modifyName")
	public Resp modifyName(HttpRequest request, Long groupid, String name) {
		User curr = WebUtils.currUser(request);
		name = EscapeUtil.escapeHtml4(name);
		Ret ret = GroupService.me.modifyName(curr.getId(), groupid, name);
		if (ret.isFail()) {

			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = ?", "isOpenUpdateGroupNameNotify");
		//消息触发
		if (clientConf.getValue().equals(1)) {
			String sendName = name;
//			Const.getBsExecutor().execute(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Ret ret = WxChatApi.updateGroupInfo(request, curr, groupid, sendName);
//						if (ret.isFail()) {
//							log.error(RetUtils.getRetMsg(ret));
//						}
//					} catch (Exception e) {
//						log.error("", e);
//					}
//				}
//			});
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 修改群头像
	 * @param request
	 * @param groupid
	 * @param avatar
	 * @return
	 * @author lixinji
	 * 2021年1月13日 下午7:08:07
	 */
	@RequestPath(value = "/modifyAvatar")
	public Resp modifyAvatar(HttpRequest request, Long groupid, UploadFile uploadFile) {
		User curr = WebUtils.currUser(request);
		if (uploadFile == null) {
			return Resp.fail("上传信息为空");
		}
		Resp imgret = null;
		byte[] imageBytes = uploadFile.getData();
		if (!UserService.isSuper(curr)) {
			int maxsize = ConfService.getInt("user.upload.avatar.maxsize", 512);
			if (imageBytes.length > 1024 * maxsize) {
				imgret = Resp.fail("文件尺寸不能大于" + maxsize + "KB");
				return imgret;
			}
		}
		BufferedImage bi = ImgUtil.toImage(imageBytes);
		float scale = ImgUtils.calcScaleWithWidth(168, bi);
		Img img = null;
		try {
			img = ImgUtils.processImg(Const.UPLOAD_DIR.GROUP_AVATAR, groupid.intValue(), uploadFile, scale);
			img.setComefrom(Img.ComeFrom.MODIFY_AVATAR);
			img.setStatus((short) 1);
			img.setSession(request.getHttpSession().getId());
			boolean f = ImgService.me.save(img);
			if (!f) {
				return Resp.ok(RetUtils.OPER_ERROR);
			}
		} catch (Exception e1) {
			log.error(e1.toString(), e1);
			return Resp.ok(RetUtils.OPER_ERROR);
		}
		Ret ret = GroupService.me.modifyUploadAvatar(curr.getId(), groupid, img.getCoverurl());
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		String sendAvatar = img.getCoverurl();
		//消息触发
//		Const.getBsExecutor().execute(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Ret ret = WxChatApi.updateGroupAvatar(request, curr, groupid, sendAvatar);
//					if (ret.isFail()) {
//						log.error(RetUtils.getRetMsg(ret));
//					}
//				} catch (Exception e) {
//					log.error("", e);
//				}
//			}
//		});

		return Resp.ok(new HashMap().put("data", img.getCoverurl()));
	}

	/**
	 * 编辑新的群公告
	 * @param request
	 * @param groupid
	 * @param notice
	 * @return
	 */
	@RequestPath(value = "/modifyNotice")
	public Resp modifyNotice(HttpRequest request, Long groupid, String notice, Integer isTop) {
		User curr = WebUtils.currUser(request);
		if (isTop == null) {
			isTop = 0;
		}
		Resp resp = GroupService.me.modifyNotice(curr, groupid, notice, isTop);
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenUpdateGroupNoticeNotify'");
		if (StrUtil.isNotBlank(notice) && clientConf.getValue().equals(1)) {
			SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.addnotice, notice, "addnotice");
			//消息触发
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						Ret ret = WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
						if (ret.isFail()) {
							log.error(RetUtils.getRetMsg(ret));
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return resp;
	}


	/**
	 * 修改历史群公告
	 * @param request
	 * @param groupid
	 * @param notice
	 * @return
	 */
	@RequestPath(value = "/updateNotice")
	public Resp updateNotice(HttpRequest request, Long groupid, Integer noticeid, String notice, Integer isTop) {
		User curr = WebUtils.currUser(request);
		Resp resp = GroupService.me.updateNotice(curr, groupid, noticeid, notice, isTop);
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenUpdateGroupNoticeNotify'");
		if (StrUtil.isNotBlank(notice) && clientConf.getValue().equals(1)) {
			SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.updatenotice, notice, "updatenotice");
			//消息触发
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						Ret ret = WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
						if (ret.isFail()) {
							log.error(RetUtils.getRetMsg(ret));
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return resp;
	}


	/**
	 * 修改标签名称
	 * @param request
	 * @param groupid
	 * @param label
	 * @return
	 */
	@RequestPath(value = "/updateLabel")
	public Resp updateLabel(HttpRequest request, Long groupid, Integer type, String label) {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		Ret ret = GroupService.me.updateLabel(curr, groupid, type, label);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}


	/**
	 * 删除历史群公告
	 * @param request
	 * @param groupid
	 * @param noticeid
	 * @return
	 */
	@RequestPath(value = "/delNotice")
	public Resp delNotice(HttpRequest request, Long groupid, String noticeid) {
		User curr = WebUtils.currUser(request);
		if (groupid == null) {
			return Resp.fail("群组id不能为空");
		}
		if (noticeid == null || noticeid.isEmpty()) {
			return Resp.fail("公告id不能为空");
		}
		Resp resp = GroupService.me.delNotice(curr, groupid, noticeid);
		if (!resp.isOk()) {
			return Resp.fail("删除失败");
		}
		String notice = resp.getData().toString();
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenUpdateGroupNoticeNotify'");
		if (clientConf.getValue().equals(1)) {
			SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.delnotice, notice, "delnotice");
			//消息触发
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						Ret ret = WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
						if (ret.isFail()) {
							log.error(RetUtils.getRetMsg(ret));
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return resp;
	}


	/**
	 * 群签到
	 * @param request
	 * @param groupid
	 * @return
	 */
	@RequestPath(value = "/sign")
	public Resp sign(HttpRequest request, Long groupid) {
		User curr = WebUtils.currUser(request);
		Resp resp = GroupService.me.sign(curr, groupid);
		if (resp.isOk()) {
			SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), MsgTemplate.groupsign, "", "groupsign");
			//消息触发
			Const.getBsExecutor().execute(new Runnable() {
				@Override
				public void run() {
					try {
						Ret ret = WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
						if (ret.isFail()) {
							log.error(RetUtils.getRetMsg(ret));
						}
					} catch (Exception e) {
						log.error("", e);
					}
				}
			});
		}
		return resp;
	}


	/**
	 * 查询当月在群组中的个人签到信息
	 * @param request
	 * @param groupid
	 * @return
	 */
	@RequestPath(value = "/userSignInfo")
	public Resp userSignInfo(HttpRequest request, Long groupid, int year, int month) {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		Ret ret = GroupService.me.userSignInfo(curr, groupid, year, month);
		if (ret.isFail()) {
			return Resp.fail(ret.get("errormsg").toString());
		}
		return Resp.ok(ret.get("data"));
	}


	/**
	 * 查询某月群组中的签到信息
	 * @param request
	 * @param groupid
	 * @return
	 */
	@RequestPath(value = "/signInfo")
	public Resp signInfo(HttpRequest request, Long groupid, Integer uid, Integer year, Integer month, Integer day) {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		Ret ret = GroupService.me.signInfo(curr, groupid, uid, year, month, day);
		if (ret.isFail()) {
			return Resp.fail(ret.get("errormsg").toString());
		}
		return Resp.ok(ret.get("data"));
	}


	/**
	 * 查询某月群组中的签到信息
	 * @param request
	 * @param groupid
	 * @return
	 */
	@RequestPath(value = "/signRecords")
	public Resp signRecords(HttpRequest request, Long groupid, Integer year, Integer month) {
		User curr = WebUtils.currUser(request);
		if (curr == null) {
			return Resp.fail("请登录");
		}
		Ret ret = GroupService.me.signRecords(curr, groupid, year, month);
		if (ret.isFail()) {
			return Resp.fail(ret.get("errormsg").toString());
		}
		return Resp.ok(ret.get("data"));
	}

	/**
	 * 管理员操作
	 * @param request
	 * @param groupid
	 * @param uid
	 * @param grouprole
	 * @return
	 * @author lixinji
	 * 2021年1月13日 下午3:17:35
	 */
	@RequestPath(value = "/manager")
	public Resp manager(HttpRequest request, Long groupid, Integer uid, Short grouprole) {
		User curr = WebUtils.currUser(request);
		Ret ret = GroupService.me.manager(curr.getId(), uid, groupid, grouprole);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					Ret ret = WxChatApi.manager(request, curr, groupid, uid, grouprole);
					if (ret.isFail()) {
						log.error(RetUtils.getRetMsg(ret));
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 好友显示操作
	 * @param request
	 * @param groupid
	 * @param friendflag
	 * @return
	 * @author lixinji
	 * 2021年1月13日 下午3:52:10
	 */
	@RequestPath(value = "/modifyFriendFlag")
	public Resp modifyFriendFlag(HttpRequest request, Long groupid, Short friendflag) {
		User curr = WebUtils.currUser(request);
		Ret ret = GroupService.me.friendFlag(curr.getId(), groupid, friendflag);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 修改群公告滚屏显示
	 * @param request
	 * @param groupid
	 * @param noticeRoll
	 * @return
	 * @author xinji
	 * 2023年10月17日 下午3:52:10
	 */
	@RequestPath(value = "/modifyNoticeRoll")
	public Resp modifyNoticeRoll(HttpRequest request, Long groupid, Integer noticeRoll) {
		User curr = WebUtils.currUser(request);
		Ret ret = GroupService.me.isOpenNoticeRoll(curr, groupid, noticeRoll);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		String key = "";
		String msgTemplate = "";
		if (noticeRoll.equals(1)) {
			key = "groupNoticeRollOpen";
			msgTemplate = MsgTemplate.groupNoticeRollOpen;
		} else {
			key = "groupNoticeRollClose";
			msgTemplate = MsgTemplate.groupNoticeRollClose;
		}
		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), msgTemplate, /*text*/"", key);
		//消息触发
		Const.getBsExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					Ret ret = WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
					if (ret.isFail()) {
						log.error(RetUtils.getRetMsg(ret));
					}
				} catch (Exception e) {
					log.error("", e);
				}
			}
		});
		return Resp.ok(RetUtils.OPER_RIGHT);
	}


	/**
	 * 修改开关
	 * @param request
	 * @param groupid
	 * @param isOpen
	 * @param type inviteCode(邀请码), inviteFriend(邀请好友), card(名片), memberNum(成员数量)
	 * @return
	 * @author xinji
	 * 2023年10月17日 下午3:52:10
	 */
	@RequestPath(value = "/modifyGroupSwitch")
	public Resp modifyGroupSwitch(HttpRequest request, Long groupid, Integer isOpen, String type) {
		User curr = WebUtils.currUser(request);
		Ret ret = GroupService.me.updateGroupSwitch(curr, groupid, isOpen, type);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		String text = "";
		String key = type;
		String msgTemplate = "";
		switch (type) {
			case "inviteCode":
				if (isOpen.equals(1)) {
					text = curr.getNick() + " 打开邀请码 ";
					msgTemplate = MsgTemplate.inviteCodeOpen;
				} else {
					text = curr.getNick() + " 关闭邀请码 ";
					msgTemplate = MsgTemplate.inviteCodeClose;
				}
				break;
//			case "inviteFriend":
//				updateGroup.setIsOpenInviteFriend(isOpen);
//				break;
//			case "card":
//				updateGroup.setIsOpenCard(isOpen);
//				break;
			case "memberNum":
				if (isOpen.equals(1)) {
					text = "管理员 " + curr.getNick() + " 打开群成员数量显示开关 ";
					msgTemplate = MsgTemplate.memberNumOpen;
				} else {
					text = "管理员 " + curr.getNick() + " 关闭群成员数量显示开关 ";
					msgTemplate = MsgTemplate.memberNumClose;
				}
				break;
			default:
				break;
		}
		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), msgTemplate, /*text*/"", key);
		//消息触发
//		Const.getBsExecutor().execute(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					Ret ret = WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
//					if (ret.isFail()) {
//						log.error(RetUtils.getRetMsg(ret));
//					}
//				} catch (Exception e) {
//					log.error("", e);
//				}
//			}
//		});
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 修改群名片显示开关显示
	 * @param request
	 * @param groupid
	 * @param useCard
	 * @return
	 * @author xinji
	 * 2023年10月18日 下午3:55:10
	 */
	@RequestPath(value = "/updateCardSwitch")
	public Resp updateUseCardSwitch(HttpRequest request, Long groupid, Integer useCard) {
		User curr = WebUtils.currUser(request);
		Ret ret = GroupService.me.updateCardSwitch(curr, groupid, useCard);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 修改群消息撤回开关显示
	 * @param request
	 * @param groupid
	 * @param msgBack
	 * @return
	 * @author xinji
	 * 2023年10月23日 下午3:55:10
	 */
	@RequestPath(value = "/updateMsgBack")
	public Resp updateMsgBack(HttpRequest request, Long groupid, Integer msgBack) {
		User curr = WebUtils.currUser(request);
		Ret ret = GroupService.me.updateMsgBack(curr, groupid, msgBack);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.OPER_RIGHT);
	}

	/**
	 * 群审核开关修改-已调整
	 * @param request
	 * @param groupid
	 * @param mode
	 * @return
	 * @author lixinji
	 * 2020年3月9日 下午4:37:18
	 */
	@RequestPath(value = "/modifyReview")
	public Resp modifyReview(HttpRequest request, Long groupid, Short mode) {
		String text = "";
		User curr = WebUtils.currUser(request);
		SysMsgVo sysMsgVo = new SysMsgVo(curr.getNick(), "", "", "");
		switch (mode) {
		case Const.GroupJoinMode.REVIEW:
			text = MsgTemplate.reviewopen;
			sysMsgVo.setMsgbody(text);
			sysMsgVo.setMsgkey("reviewopen");
			break;
		case Const.GroupJoinMode.NO_REVIEW:
			text = MsgTemplate.reviewclose;
			sysMsgVo.setMsgbody(text);
			sysMsgVo.setMsgkey("reviewclose");
			break;
		default:
			return Resp.fail().msg("无效入群方式");
		}

		Ret ret = GroupService.me.modifyReview(curr, groupid, mode);
		if (ret.isFail()) {
			return Resp.fail().msg(RetUtils.getRetMsg(ret));
		}
		ClientConf clientConf = ClientConf.dao.findFirst("select * from client_conf where name = 'isOpenGroupReviewNotify'");
		if (clientConf.getValue().equals(1)) {
			//消息触发
//			Const.getBsExecutor().execute(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						WxChatApi.sendGroupMsgEach(request, sysMsgVo.toText(), Const.ContentType.TEXT, curr.getId(), groupid, Const.YesOrNo.YES, sysMsgVo);
//					} catch (Exception e) {
//						log.error("", e);
//					}
//				}
//			});
		}

		return Resp.ok(RetUtils.OPER_RIGHT);
	}
}

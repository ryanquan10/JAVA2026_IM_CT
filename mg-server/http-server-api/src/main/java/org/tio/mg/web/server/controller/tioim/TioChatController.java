
package org.tio.mg.web.server.controller.tioim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.http.common.HttpRequest;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.service.tioim.TioChatService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.utils.resp.Resp;

/**
 * 会话管理
 * @author xufei
 * 2020年5月25日 下午4:45:27
 */
@RequestPath(value = "/wxchat")
public class TioChatController {
	private static Logger log = LoggerFactory.getLogger(TioChatController.class);

	/**
	 * @param args
	 * @author xufei
	 * 2020年6月18日 上午10:04:21
	 */
	public static void main(String[] args) {

	}

	private TioChatService chatService = TioChatService.me;
	
	/**
	 * @param request
	 * @param searchkey
	 * @param pageNumber
	 * @param pageSize
	 * @param chatmode
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2021年3月2日 下午2:07:09
	 */
	@RequestPath(value = "/screenlist")
	public Resp screenlist(HttpRequest request,String searchkey,Integer pageNumber,Integer pageSize,Short chatmode) throws Exception {
		Ret ret = chatService.screenlist(pageNumber, pageSize, searchkey, chatmode);
		if(ret.isFail()) {
			log.error("获取好友列表(消息模型)失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 会话列表
	 * @param request
	 * @param uid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:45:24
	 */
	@RequestPath(value = "/chatlist")
	public Resp chatlist(HttpRequest request,Integer uid,Integer pageNumber, Integer pageSize) throws Exception {
		Ret ret = chatService.chatItemList(uid, pageNumber, pageSize);
		if(ret.isFail()) {
			log.error("获取会话列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 会话消息
	 * @param request
	 * @param chatlinkid
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:46:15
	 */
	@RequestPath(value = "/chatmsglist")
	public Resp chatMsgList(HttpRequest request,Long chatlinkid,Integer pageNumber, Integer pageSize) throws Exception {
		Ret ret = chatService.chatMsgList(chatlinkid, pageNumber, pageSize);
		if(ret.isFail()) {
			log.error("获取会话消息失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 群列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:47:08
	 */
	@RequestPath(value = "/grouplist")
	public Resp groupList(HttpRequest request,Integer pageNumber, Integer pageSize,Integer uid) throws Exception {
		Ret ret = chatService.groupList(pageNumber, pageSize, uid);
		if(ret.isFail()) {
			log.error("获取群列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 好友
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:47:49
	 */
	@RequestPath(value = "/friendlist")
	public Resp friendList(HttpRequest request,Integer pageNumber, Integer pageSize,Integer uid) throws Exception {
		Ret ret = chatService.friendList(pageNumber, pageSize, uid);
		if(ret.isFail()) {
			log.error("获取好友列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月30日 上午10:00:46
	 */
	@RequestPath(value = "/applylist")
	public Resp applyList(HttpRequest request,Integer pageNumber, Integer pageSize,Integer uid) throws Exception {
		Ret ret = chatService.applyList(pageNumber, pageSize, uid);
		if(ret.isFail()) {
			log.error("获取好友列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 好友消息列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param uid
	 * @param touid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:48:39
	 */
	@RequestPath(value = "/friendmsglist")
	public Resp friendMsgList(HttpRequest request,Integer pageNumber, Integer pageSize,Integer uid,Integer touid) throws Exception {
		Ret ret = chatService.friendMsgList(pageNumber, pageSize, uid, touid);
		if(ret.isFail()) {
			log.error("获取好友消息列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	
	/**
	 * 群消息列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:49:18
	 */
	@RequestPath(value = "/groupmsglist")
	public Resp groupMsgList(HttpRequest request,Integer pageNumber, Integer pageSize,Long groupid) throws Exception {
		Ret ret = chatService.groupMsgList(pageNumber, pageSize, groupid);
		if(ret.isFail()) {
			log.error("获取群消息列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 群用户列表
	 * @param request
	 * @param pageNumber
	 * @param pageSize
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:49:50
	 */
	@RequestPath(value = "/groupuserlist")
	public Resp groupUserList(HttpRequest request,Integer pageNumber, Integer pageSize,Long groupid) throws Exception {
		Ret ret = chatService.groupUserList(groupid, pageNumber, pageSize);
		if(ret.isFail()) {
			log.error("获取群用户列表失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkPage(ret));
	}
	
	/**
	 * 群信息
	 * @param request
	 * @param groupid
	 * @return
	 * @throws Exception
	 * @author xufei
	 * 2020年7月29日 下午2:50:42
	 */
	@RequestPath(value = "/groupinfo")
	public Resp groupInfo(HttpRequest request,Long groupid) throws Exception {
		Ret ret = chatService.groupInfo(groupid);
		if(ret.isFail()) {
			log.error("获取群信息失败：{}",RetUtils.getRetMsg(ret));
			return Resp.fail(RetUtils.getRetMsg(ret));
		}
		return Resp.ok(RetUtils.getOkData(ret));
	}
	
}

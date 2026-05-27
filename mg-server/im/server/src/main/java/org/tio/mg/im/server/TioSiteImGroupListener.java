
package org.tio.mg.im.server;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.intf.GroupListener;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.mg.im.common.Command;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.common.ImSessionContext;
import org.tio.mg.im.common.bs.LeaveGroupNtf;
import org.tio.mg.im.common.utils.ImUtils;
import org.tio.mg.im.server.handler.PageOnlineReqHandler;
import org.tio.mg.service.model.main.ChatroomJoinLeave;
import org.tio.mg.service.vo.SimpleUser;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.GroupStat;

/**
 * @author tanyaowu
 * 2016年5月13日 下午10:38:36
 */
public class TioSiteImGroupListener implements GroupListener {
	private static Logger log = LoggerFactory.getLogger(TioSiteImGroupListener.class);

	public static TioSiteImGroupListener me = new TioSiteImGroupListener();

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 *
	 * @author tanyaowu
	 */
	protected TioSiteImGroupListener() {
	}

	/**
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void onAfterBind(ChannelContext channelContext, String group) throws Exception {
	}

	/**
	 * @param channelContext
	 * @param group
	 * @throws Exception
	 * @author tanyaowu
	 */
	@Override
	public void onAfterUnbind(ChannelContext channelContext, String group) throws Exception {
		ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);

		if (imSessionContext.isWx()) {
			return;
		}

		PageOnlineReqHandler.ME.clearCache(group);//进出群组要清空在线观众列表的数据

		boolean con = !group.startsWith(Const.ImGroupType.PREFIX);
		if (con) {
			try {
				ChatroomJoinLeave chatroomJoinLeave = imSessionContext.getChatroomJoinLeave();
				if (chatroomJoinLeave != null) {
					if (!chatroomJoinLeave.isChat()) {
						long cost = System.currentTimeMillis() - chatroomJoinLeave.getJointime().getTime();
						String sql = "update chatroom_join_leave set leavetime = ?, cost = ?, status = 1 where id = ?";
						Db.use(Const.Db.TIO_SITE_MAIN).update(sql, new Date(), cost, chatroomJoinLeave.getId());
					}
				}
			} catch (Exception e) {
				log.error(e.toString(), e);
			}
		}

		// 暂时获取在所有页面上的用户
		con = Const.ImGroupType.ALL_IN_ONE.equals(group);

		if (con) {
			SimpleUser simpleUser = ImUtils.getHandshakeSimpleUser(channelContext);
			LeaveGroupNtf leaveGroupNtf = new LeaveGroupNtf(group, simpleUser);

			GroupStat groupStat = Ims.createGroupStat(group);

			// 暂时获取在所有页面上的用户
			groupStat = Ims.createGroupStat(Const.ImGroupType.ALL_IN_ONE);
			leaveGroupNtf.setG(Const.ImGroupType.ALL_IN_ONE);

			leaveGroupNtf.setOnline(groupStat.getCalcOnline());
			ImPacket imPacket = new ImPacket(Command.LeaveGroupNtf, leaveGroupNtf);

			Ims.sendToGroup(group, imPacket);
		}
	}

}

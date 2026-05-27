
package org.tio.sitexxx.im.server.clu;

import java.util.Date;
import java.util.List;

import org.tio.clu.client.bs.TransferListener;
import org.tio.clu.common.BindType;
import org.tio.clu.common.bs.TransferNtf;
import org.tio.core.intf.Packet;
import org.tio.sitexxx.im.common.Command;
import org.tio.sitexxx.im.common.ImPacket;
import org.tio.sitexxx.im.common.bs.wx.friend.WxFriendChatNtf;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.WxFriendMsg;
import org.tio.sitexxx.service.service.chat.FriendService;
import org.tio.sitexxx.service.service.chat.SynService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.utils.hutool.StrUtil;
import org.tio.utils.json.Json;
import org.tio.utils.lock.ListWithLock;
import org.tio.utils.lock.WriteLockHandler;

/**
 * @author talent
 *
 */
public class ImTransferListener implements TransferListener {
	public static final ImTransferListener me = new ImTransferListener();

	/**
	 * 
	 */
	public ImTransferListener() {
	}

	@Override
	public boolean onBeforeTransfer(Packet bsPacket, BindType bindType, TransferNtf transferNtf) {
		ImPacket imPacket = (ImPacket) bsPacket;
		switch (bindType) {
		case User:
			// 消息由A发给B，在这里更新B那台服务器的缓存，只有A和B不在同一台IM服务器时，才会跑到这来执行
			// 业务层可以在这更新缓存数据
			Command command = imPacket.getCommand();
			switch (command) {
			case WxFriendChatNtf:
				WxFriendChatNtf chatNtf = (WxFriendChatNtf) imPacket.getBodyObj();
				if (chatNtf == null && StrUtil.isNotBlank(imPacket.getBodyStr())) {
					chatNtf = Json.toBean(imPacket.getBodyStr(), WxFriendChatNtf.class);
				}
				p2pCacheDeal(chatNtf);
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}
		//返回true，则由tio集群将消息分发给用户B，否则由应用自己决定是否要不要把消息发给用户B
		return true;
	}

	@Override
	public void onAfterTransfer(Packet bsPacket, BindType bindType, TransferNtf transferNtf) {
		//如果onBeforeTransfer返回的是false，则不会执行onAfterTransfer
	}

	/**
	 * 私聊集群缓存处理
	 * @param chatNtf
	 * @author lixinji
	 * 2020年9月14日 上午11:34:36
	 */
	@SuppressWarnings("unchecked")
	private void p2pCacheDeal(WxFriendChatNtf chatNtf) {
		if (chatNtf != null && chatNtf.getChatlinkid() != null) {
			String key = chatNtf.getChatlinkid() + "";
			int historyCount = ConfService.getInt("im.history.chat.count.p2p", FriendService.WX_FRIEND_MSG_LIMIT);
			ListWithLock<WxFriendMsg> listWithLock = Caches.getCache(CacheConfig.WX_FRIEND_MSG_CHAT_6).get(key, ListWithLock.class);
			if (listWithLock != null) {
				listWithLock.handle(new WriteLockHandler<List<WxFriendMsg>>() {
					@Override
					public void handler(List<WxFriendMsg> list) {
						WxFriendMsg chatRecord = p2pNtfToMsg(chatNtf);
						list.add(0, chatRecord);
						while (list.size() > historyCount) {
							list.remove(historyCount);
						}
					}
				});
				SynService.me.recordCluChatCache(chatNtf.getTouid(), chatNtf.getChatlinkid());
			}
		}
	}

	/**
	 * 私聊通知转换为私聊消息
	 * @param chatNtf
	 * @return
	 * @author lixinji
	 * 2020年9月14日 上午11:32:41
	 */
	private WxFriendMsg p2pNtfToMsg(WxFriendChatNtf chatNtf) {
		WxFriendMsg chatRecord = new WxFriendMsg();
		chatRecord.setNick(chatNtf.getNick());
		chatRecord.setAvatar(chatNtf.getAvatar());
		chatRecord.setMid(chatNtf.getMid());
		chatRecord.setUid(chatNtf.getUid());
		chatRecord.setTouid(chatNtf.getTouid());
		chatRecord.setReadflag(chatNtf.getReadflag());
		chatRecord.setReadtime(chatNtf.getReadtime());
		chatRecord.setTime(new Date(chatNtf.getT()));
		chatRecord.setSendbysys(chatNtf.getSendbysys());
		chatRecord.setMsgtype(chatNtf.getMsgtype());
		chatRecord.setContenttype(chatNtf.getCt());
		String c = chatNtf.getC();
		chatRecord.setC(c);
		switch (chatNtf.getCt()) {
		case Const.ContentType.TEXT:
			//保存缩略文字
			break;
		case Const.ContentType.AUDIO:
			chatRecord.setAc(c);
			break;
		case Const.ContentType.BLOG:
			//保存微博
			chatRecord.setBc(c);
			break;
		case Const.ContentType.FILE:
			//保存文件
			chatRecord.setFc(c);
			break;
		case Const.ContentType.IMG:
			//保存图片
			chatRecord.setIc(c);
			break;
		case Const.ContentType.VIDEO:
			//保存视频
			chatRecord.setVc(c);
			break;
		case Const.ContentType.MSG_CARD:
			//保存名片
			chatRecord.setCardc(c);
			break;
		case Const.ContentType.CALL_AUDIO:
			chatRecord.setCall(c);
			break;
		case Const.ContentType.CALL_VIDEO:
			chatRecord.setCall(c);
			break;
		case Const.ContentType.REDPACKET:
			chatRecord.setRed(c);
			break;
		case Const.ContentType.TEMPLATE:
			chatRecord.setTemp(c);
			break;
		default:
		}
		return chatRecord;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	}

}

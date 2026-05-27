
package org.tio.mg.im.server.handler;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.mg.im.common.Command;
import org.tio.mg.im.common.CommandHandler;
import org.tio.mg.im.common.ImPacket;
import org.tio.mg.im.common.bs.PageOnlineReq;
import org.tio.mg.im.common.bs.PageOnlineResp;
import org.tio.mg.im.server.Ims;
import org.tio.mg.im.server.TioSiteImServerStarter;
import org.tio.mg.im.server.utils.ImUtils;
import org.tio.mg.service.cache.CacheConfig;
import org.tio.mg.service.cache.Caches;
import org.tio.mg.service.service.base.UserService;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.mg.service.vo.SimpleUser;
import org.tio.utils.cache.ICache;
import org.tio.utils.json.Json;
import org.tio.utils.page.Page;

/**
 * 分页获取在线用户
 * @author tanyaowu
 *
 */
@CommandHandler(Command.PageOnlineReq)
public class PageOnlineReqHandler implements ImServerHandler {
	private static Logger log = LoggerFactory.getLogger(PageOnlineReqHandler.class);

	public static final PageOnlineReqHandler ME = new PageOnlineReqHandler();

	public PageOnlineReqHandler() {
	}

	@SuppressWarnings("unused")
	private static UserService userService = UserService.ME;

	/**
	 * 
	 */
	@Override
	public void handler(ImPacket packet, ChannelContext channelContext, boolean isWebsocket) throws Exception {
		//		User curr = ImUtils.getUser(channelContext);

		PageOnlineReq pageOnlineReq = Json.toBean(packet.getBodyStr(), PageOnlineReq.class);
		String group = pageOnlineReq.getG();
		Integer type = pageOnlineReq.getType();
		Integer pageNumber = pageOnlineReq.getPageNumber();
		Integer pageSize = pageOnlineReq.getPageSize();
		log.info("获取在线用户列表:group:{}, type:{}, pageNumber:{}, pageSize:{}", group, type, pageNumber, pageSize);

		if (pageSize == null || pageSize > 200 || pageSize <= 0) {
			pageSize = 50;
		}

		String cachekey = group + "_" + type + "_" + pageNumber + "_" + pageSize;
		ICache cache = Caches.getCache(CacheConfig.MG_PAGE_ONLINE);
		ImPacket imPacket = cache.get(cachekey, ImPacket.class);
		if (imPacket != null) {
			Ims.send(channelContext, imPacket);
			return;
		}

		Page<SimpleUser> pc = null;
		Page<SimpleUser> android = null;
		Page<SimpleUser> ios = null;
		Page<SimpleUser> all = null;

		String groupType = Const.ImGroupType.REAL; //"_"
		if (type == null) {
			pc = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigWs, channelContext, Devicetype.WEB + groupType + group, pageNumber, pageSize);
			android = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigApp, channelContext, Devicetype.ANDROID + groupType + group, pageNumber, pageSize);
			ios = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigApp, channelContext, Devicetype.IOS + groupType + group, pageNumber, pageSize);
			all = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigWs, channelContext, groupType + group, pageNumber, pageSize);
		} else {
			if (Objects.equals(type, PageOnlineReq.Type.PC)) {
				pc = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigWs, channelContext, Devicetype.WEB + groupType + group, pageNumber, pageSize);
			} else if (Objects.equals(type, PageOnlineReq.Type.ANDROID) || Objects.equals(type, PageOnlineReq.Type.IOS)) {
				android = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigApp, channelContext, Devicetype.ANDROID + groupType + group, pageNumber, pageSize);
			} else if (Objects.equals(type, PageOnlineReq.Type.IOS)) {
				ios = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigApp, channelContext, Devicetype.IOS + groupType + group, pageNumber, pageSize);
			} else if (Objects.equals(type, PageOnlineReq.Type.ALL)) {
				all = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigWs, channelContext, groupType + group, pageNumber, pageSize);
			}
		}

		//		if (!UserService.isSuper(curr)) {
		//			if (all.getTotalRow() < 10) {
		//				return;
		//			}
		//		}

		// 暂时获取在所有页面上的用户
		all = ImUtils.getPageOfGroup(TioSiteImServerStarter.tioServerConfigWs, channelContext, Const.ImGroupType.ALL_IN_ONE, pageNumber, pageSize);

		PageOnlineResp pageOnlineResp = new PageOnlineResp();
		pageOnlineResp.setAll(all);
		pageOnlineResp.setPc(pc);
		pageOnlineResp.setAndroid(android);
		pageOnlineResp.setIos(ios);
		pageOnlineResp.setType(type);

		imPacket = new ImPacket(Command.PageOnlineResp, pageOnlineResp);
		cache.put(cachekey, imPacket);
		Ims.send(channelContext, imPacket);
	}

	/**
	 * 
	 * @param group
	 */
	public void clearCache(Integer group) {
		clearCache(group + "");
	}

	/**
	 * 
	 * @param group
	 */
	public void clearCache(String group) {
		//		String cachekey = group;// + "_" + type + "_" + pageNumber + "_" + pageSize;
		ICache cache = Caches.getCache(CacheConfig.MG_PAGE_ONLINE);
		//		cache.remove(cachekey);
		cache.clear();
	}
}

/*
 * cpigjfeid本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动aesjrexlp
 */
package org.tio.clu.client;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.ClientChannelContext;
import org.tio.clu.common.BindType;
import org.tio.clu.common.Clu;
import org.tio.clu.common.CluConst;
import org.tio.clu.common.CluPacket;
import org.tio.clu.common.Command;
import org.tio.clu.common.bs.BestNodeReq;
import org.tio.clu.common.bs.BestNodeResp;
import org.tio.clu.common.bs.BindReq;
import org.tio.clu.common.bs.TransferReq;
import org.tio.clu.common.bs.UnbindReq;
import org.tio.clu.common.bs.UpdateBsNodeReq;
import org.tio.clu.common.utils.FstUtils;
import org.tio.clu.common.vo.BsPfmData;
import org.tio.clu.common.vo.PfmData;
import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.TioConfig;
import org.tio.core.intf.Packet;
import org.tio.core.intf.PacketMeta;
import org.tio.core.utils.TioUtils;
import org.tio.server.TioServerConfig;
import org.tio.utils.lock.MapWithLock;
import org.tio.utils.lock.ReadLockHandler;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;

/**
 * @author tanyaowu 2020年8月25日 上午11:30:14
 */
public class CluClient {
	private static Logger log = LoggerFactory.getLogger(CluClient.class);

	public static boolean asBsservernode = Boolean.getBoolean("tio.clu.client.as.bsservernode");

	/**
	 * 
	 * @param cluClientStarter
	 * @param uid
	 * @param protocol
	 * @return
	 * @author tanyaowu
	 */
	public static BestNodeResp bestNodeReq(CluClientStarter cluClientStarter, Integer uid, String protocol) {
		return bestNodeReq(cluClientStarter, uid, protocol, CluConst.DFT_SYNSEND_TIMEOUT);
	}

	/**
	 * @param cluClientStarter
	 * @param uid
	 * @param protocol
	 * @param timeout
	 * @return
	 * @author tanyaowu
	 */
	public static BestNodeResp bestNodeReq(CluClientStarter cluClientStarter, Integer uid, String protocol, long timeout) {
		BestNodeReq bestNodeReq = new BestNodeReq();
		bestNodeReq.setUid(uid);
		bestNodeReq.setProtocol(protocol);

		ClientChannelContext clientChannelContext = Cc.next(cluClientStarter.getTioClientConfig());
		if (clientChannelContext == null) {
			return null;
		}
		CluPacket respPacket = Clu.synSend(clientChannelContext, Command.BestNodeReq, bestNodeReq, timeout);
		if (respPacket == null) {
			return null;
		}

		BestNodeResp bestNodeResp = Clu.getBodyObj(respPacket, BestNodeResp.class);
		return bestNodeResp;
	}

	/**
	 * @param clientChannelContext
	 * @param bindType
	 * @param v
	 * @author tanyaowu
	 */
	public static void bindXxx(ClientChannelContext clientChannelContext, BindType bindType, String v) {
		bindXxx(clientChannelContext, bindType, v, null);
	}

	/**
	 * 
	 * @param clientChannelContext
	 * @param bindType
	 * @param v
	 * @param vs
	 * @author tanyaowu
	 */
	public static void bindXxx(ClientChannelContext clientChannelContext, BindType bindType, String v, String[] vs) {
		if (clientChannelContext == null) {
			return;
		}

		// 检查v是否已经绑定过，如果绑定过了，就不发绑定请求了（不检查vs）
		// 重复绑定并不影响业务逻辑，但对性能略有影响
		if (StrUtil.isNotBlank(v) && ArrayUtil.isEmpty(vs)) {
			CluClientSessionContext cluClientSessionContext = CluClient.getCluClientSessionContext(clientChannelContext);
			if (cluClientSessionContext == null) {
				log.error("CluClientSessionContext is null");
				return;
			}
			if (Cc.getBindedData(clientChannelContext.getTioConfig()).contains(bindType, v)) {
				return;
			}
		}

		BindReq bindReq = new BindReq();
		bindReq.setBt(bindType.getValue());
		bindReq.setV(v);
		bindReq.setVs(vs);
		Clu.send(clientChannelContext, Command.BindReq, bindReq);
	}

	public static void bindXxx(TioConfig bsTioConfig, BindType bindType, String v) {
		bindXxx(bsTioConfig, bindType, v, null);
	}

	public static void bindXxx(TioConfig bsTioConfig, BindType bindType, String v, String[] vs) {
		if (bsTioConfig instanceof TioServerConfig) {
			TioServerConfig tioServerConfig = (TioServerConfig) bsTioConfig;
			bindXxx(Cc.next(tioServerConfig), bindType, v, vs);
		}
	}

	/**
	 * 
	 * @param clientChannelContext
	 * @return
	 * @author tanyaowu
	 */
	public static String getCgId(ClientChannelContext clientChannelContext) {
		return getCluClientSessionContext(clientChannelContext).getCgId();
	}

	/**
	 * 
	 * @param channelContext
	 * @return
	 * @author tanyaowu
	 */
	public static CluClientSessionContext getCluClientSessionContext(ChannelContext channelContext) {
		CluClientSessionContext cluClientSessionContext = (CluClientSessionContext) Clu.getCluSessionContext(channelContext);
		return cluClientSessionContext;
	}

	/**
	 * 
	 * @param <T>
	 * @param bindType
	 * @param clientChannelContext
	 * @param mapwithlock
	 * @author tanyaowu
	 */
	public static <T> void initBindReq(BindType bindType, ClientChannelContext clientChannelContext, MapWithLock<String, T> mapwithlock) {
		if (clientChannelContext == null) {
			return;
		}
		if (mapwithlock != null && mapwithlock.size() > 0) {
			mapwithlock.handle(new ReadLockHandler<Map<String, T>>() {
				@Override
				public void handler(Map<String, T> t) {
					Set<String> set = t.keySet();
					String[] strarray = ArrayUtil.toArray(set, String.class);
					bindXxx(clientChannelContext, bindType, null, strarray);
				}
			});
		}
	}

	/**
	 * 连接上cluServerNode后，需要做一个初始化操作
	 * 
	 * @param bsTioServerConfig
	 * @param clientChannelContext
	 * @author tanyaowu
	 */
	public static void initBindReq(TioServerConfig bsTioServerConfig, ClientChannelContext clientChannelContext) {
		if (clientChannelContext == null) {
			return;
		}
		if (bsTioServerConfig == null) {
			return;
		}
		initBindReq(BindType.User, clientChannelContext, bsTioServerConfig.users.getMap());
		initBindReq(BindType.Group, clientChannelContext, bsTioServerConfig.groups.getGroupmap());
		initBindReq(BindType.BsId, clientChannelContext, bsTioServerConfig.bsIds.getMap());
		initBindReq(BindType.Token, clientChannelContext, bsTioServerConfig.tokens.getMap());
		initBindReq(BindType.Ip, clientChannelContext, bsTioServerConfig.ips.getIpmap());
	}

	/**
	 * @param args
	 * @author tanyaowu
	 */
	public static void main(String[] args) {

	}

	/**
	 * 
	 * @param clientChannelContext
	 * @param bindType
	 * @param v
	 * @param transferPacket
	 * @author tanyaowu
	 */
	public static void transfer(ClientChannelContext clientChannelContext, BindType bindType, String v, Packet transferPacket) {
		transfer(clientChannelContext, bindType, v, null, transferPacket);
	}

	/**
	 * 
	 * @param clientChannelContext
	 * @param bindType
	 * @param v
	 * @param vs
	 * @param transferPacket
	 * @author tanyaowu
	 */
	public static void transfer(ClientChannelContext clientChannelContext, BindType bindType, String v, String[] vs, Packet transferPacket) {
		if (clientChannelContext == null) {
			return;
		}

		if (BooleanUtil.isTrue(transferPacket.isFromClu())) {
			return;
		}

		try {
			TransferReq transferReq = new TransferReq();
			transferReq.setBt(bindType.getValue());
			transferReq.setV(v);
			transferReq.setVs(vs);

			PacketMeta meta = transferPacket.getMeta();
			if (meta != null && meta.getCountDownLatch() != null) {
				transferPacket = transferPacket.clone();
				transferPacket.setMeta(null);
			}
			transferReq.setP(FstUtils.asByteArray(transferPacket));

			Clu.send(clientChannelContext, Command.TransferReq, transferReq);
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 
	 * @param bsTioConfig
	 * @param bindType
	 * @param v
	 * @param transferPacket
	 * @author tanyaowu
	 */
	public static void transfer(TioConfig bsTioConfig, BindType bindType, String v, Packet transferPacket) {
		transfer(bsTioConfig, bindType, v, null, transferPacket);
	}

	/**
	 * 
	 * @param bsTioConfig
	 * @param bindType
	 * @param v
	 * @param vs
	 * @param transferPacket
	 * @author tanyaowu
	 */
	public static void transfer(TioConfig bsTioConfig, BindType bindType, String v, String[] vs, Packet transferPacket) {
		if (bsTioConfig instanceof TioServerConfig) {
			TioServerConfig tioServerConfig = (TioServerConfig) bsTioConfig;
			transfer(Cc.next(tioServerConfig), bindType, v, vs, transferPacket);
		}
	}

	/**
	 * 解绑定
	 * 
	 * @param clientChannelContext
	 * @param bindType
	 * @param v
	 * @author tanyaowu
	 */
	public static void unbindXxx(ClientChannelContext clientChannelContext, BindType bindType, String v) {
		unbindXxx(clientChannelContext, bindType, v, null);
	}

	/**
	 * 解绑
	 * 
	 * @param clientChannelContext
	 * @param bindType
	 * @param v
	 * @param vs
	 * @author tanyaowu
	 */
	public static void unbindXxx(ClientChannelContext clientChannelContext, BindType bindType, String v, String[] vs) {
		if (clientChannelContext == null) {
			return;
		}
		UnbindReq unbindReq = new UnbindReq();
		unbindReq.setBt(bindType.getValue());
		unbindReq.setV(v);
		unbindReq.setVs(vs);
		Clu.send(clientChannelContext, Command.UnbindReq, unbindReq);
	}

	/**
	 * 
	 * @param bsTioConfig
	 * @param bindType
	 * @param v
	 * @author tanyaowu
	 */
	public static void unbindXxx(TioConfig bsTioConfig, BindType bindType, String v) {
		unbindXxx(bsTioConfig, bindType, v, null);
	}

	/**
	 * 
	 * @param bsTioConfig
	 * @param bindType
	 * @param v
	 * @param vs
	 * @author tanyaowu
	 */
	public static void unbindXxx(TioConfig bsTioConfig, BindType bindType, String v, String[] vs) {
		if (bsTioConfig instanceof TioServerConfig) {
			TioServerConfig tioServerConfig = (TioServerConfig) bsTioConfig;
			unbindXxx(Cc.next(tioServerConfig), bindType, v, vs);
		}
	}

	/**
	 * 更新业务服务器数据给集群服务器
	 * 
	 * @param clientChannelContext
	 * @param bsTioServerConfig
	 * @author tanyaowu
	 */
	public static void updateBsNode(ClientChannelContext clientChannelContext, TioServerConfig bsTioServerConfig) {
		if (!asBsservernode) {
			return;
		}

		if (bsTioServerConfig == null) {
			return;
		}

		if (TioUtils.checkBeforeIO(clientChannelContext)) {
			Map<String, Node> clientAccessNodeMap = bsTioServerConfig.getClientAccessNodeMap();
			if (clientAccessNodeMap == null) { // null表示这里不提供对外业务服务，无须向集群节点注册
				return;
			}

			PfmData pfm = new PfmData();
			pfm.setTcpCount(bsTioServerConfig.connections.size());

			// UpdateBsNodeReq lastUpdateBsNodeReq =
			// Cc.getBindedData(clientChannelContext.getTioConfig()).getLastUpdateBsNodeReq();

			// if (lastUpdateBsNodeReq != null) {
			// PfmData lastPfm = lastUpdateBsNodeReq.getBsNodeData().getPfm();
			// int diff = pfm.getTcpCount() - lastPfm.getTcpCount();
			// if (Math.abs(diff) <
			// Integer.getInteger("tio.clu.client.report.pfm.data.diff", 500)) {
			// log.info("本次和上次的性能数据差{}-{}=[{}]，不用通知集群服务器更新", pfm.getTcpCount(),
			// lastPfm.getTcpCount(), diff);
			// return;
			// }
			// }

			BsPfmData bsNodeData = new BsPfmData(clientAccessNodeMap);
			bsNodeData.setCreateTime(bsTioServerConfig.startTime);
			bsNodeData.setPfm(pfm);

			UpdateBsNodeReq updateBsNodeReq = new UpdateBsNodeReq();
			updateBsNodeReq.setBsNodeData(bsNodeData);
			updateBsNodeReq.setType(UpdateBsNodeReq.UpdateBsNodeReqType.ADD);

			// CluPacket respPacket =
			Clu.send(clientChannelContext, Command.UpdateBsNodeReq, updateBsNodeReq);
			// if (respPacket != null) {
			// UpdateBsNodeResp updateBsNodeResp = (UpdateBsNodeResp)
			// respPacket.getBodyObj();
			// if (updateBsNodeResp.isOk()) {
			// cluClientSessionContext.setLastUpdateBsNodeReq(updateBsNodeReq);
			// }
			// } else {
			// log.warn("超时了\r\n{}", Json.toFormatedJson(updateBsNodeReq));
			// }
		}
	}

	/**
	 * 
	 * @author tanyaowu
	 */
	public CluClient() {
	}

}


package org.tio.sitexxx.im.server.handler.wx;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.jfinal.kit.Ret;
import org.tio.sitexxx.im.server.utils.ImUtils;
import org.tio.sitexxx.service.model.main.User;
import org.tio.sitexxx.service.model.main.WxChatItemsMeta;
import org.tio.sitexxx.service.model.main.WxGroupMsg;
import org.tio.sitexxx.service.model.main.WxReadAck;
import org.tio.sitexxx.service.service.chat.ChatMsgService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.AfterGroupMsgSendVo;
import org.tio.sitexxx.service.vo.AppCode;
import org.tio.sitexxx.service.vo.ChatMsgMergeVo;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.MobileInfo;
import org.tio.sitexxx.service.vo.wx.FocusVo;
import org.tio.utils.json.Json;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.util.StrUtil;

/**
 * wx的队列API
 * @author lixinji
 * 2020年1月17日 下午2:25:27
 */
public class WxChatQueueApi {

	private static Logger log = LoggerFactory.getLogger(WxChatQueueApi.class);

	public static final WxChatQueueApi me = new WxChatQueueApi();

	/**
	 * 焦点队列数
	 */
	private static final int focusIndex = 5;

	/**
	 * 焦点队列
	 */
	private static ArrayList<LinkedBlockingQueue<FocusVo>> focusQueue = new ArrayList<LinkedBlockingQueue<FocusVo>>();

	/**
	 * 群发送后处理队列数
	 */
	private static final int groupSendMsgAfterIndex = 10;

	/**
	 * 群发送后处理队列
	 */
	private static ArrayList<LinkedBlockingQueue<AfterGroupMsgSendVo>> groupSendMsgAfterQueue = new ArrayList<LinkedBlockingQueue<AfterGroupMsgSendVo>>();

	/**
	 * 群处理合并
	 */
	private static ArrayList<Map<String, ChatMsgMergeVo>> groupMergeMsgMap = new ArrayList<Map<String, ChatMsgMergeVo>>();

	/**
	 * 队列初始化
	 * @author lixinji
	 * 2020年10月12日 下午3:17:04
	 */
	public static void wxQueueInit() {
		focusQueueInit();
		groupSendMsgAfterQueueInit();
	}

	/**
	 * 警告：目前分布式使用是在im服务器分布式，http服务器依赖nginx服务分布，此队列分布式请谨慎扩展
	 * 1、如果未来消息存储和已读未读逻辑转存在客户端，此队列可废除
	 * 2、如果未来im分布式算法更改，此方法有可能会引起数据一致性问题和并发同步问题
	 * 3、此队列如果涉及到短链接-http，有可能出现数据一致性问题
	 * 
	 * 初始化队列
	 * 该队列列表进行焦点处理
	 * 1、如果未来一个用户一次会话只在一个服务器上请求，该方法不变
	 * 2、如果未来一个用户一次会话会时刻变动（10个请求有可能出跳到10个不同的服务器），该方法需要进行分布式调整
	 * @author lixinji
	 * 2020年6月18日 下午2:54:37
	 */
	public static void focusQueueInit() {
		for (int i = 0; i < focusIndex; i++) {
			final int qindex = i;
			focusQueue.add(new LinkedBlockingQueue<FocusVo>());
			new Thread(new Runnable() {

				@Override
				public void run() {
					int error = 0;
					while (true) {
						FocusVo focusVo = null;
						try {
							ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
							long[] threadIds = threadBean.findMonitorDeadlockedThreads();

							if (threadIds != null) {
								log.error("发现死锁线程：");
								for (long threadId : threadIds) {
									log.error("线程 " + threadId + " 处于死锁状态。");
								}
							}
//							log.error("info focusQueueInit 消费队列 begin 线程信息：" + Thread.currentThread().getName());
							focusVo = focusQueue.get(qindex).take();
//							log.error("info focusQueueInit 消费队列 end 线程信息：" + Thread.currentThread().getName());
							error = 0;
						} catch (InterruptedException e1) {
							error++;
							log.error("队列获取焦点对象失败，队列index:{}，错误次数：{}", qindex, error, e1);
							if (error >= 5) {
								log.error("队列联系获取焦点对象5次失败，退出循环，队列index:{}", qindex);
								break;
							}
							continue;
						}
						try {
							if (focusVo == null) {
								log.error("焦点对象为空，队列index:{}", qindex);
								continue;
							}
							if (focusVo.getOper() == null) {
								log.error("焦点对象操作类型为空,focusvo:{}", Json.toJson(focusVo));
								continue;
							}
							switch (focusVo.getOper()) {
							case FocusVo.operCode.JOIN:
								Ret ret = ChatMsgService.joinDeal(focusVo);
								Short chatmode = focusVo.getChatmode();
								Integer uid = focusVo.getUid();
								WxChatApi.focusNtf(uid, focusVo.getRchatlinkid());
								if (ret.isFail()) {
									if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
										WxChatApi.sendFriendErrorMsg(focusVo.getDevicetype(), focusVo.getSessionid(), focusVo.getIp(), uid, uid, uid, focusVo.getRchatlinkid(),
										        AppCode.GroupErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
									} else {
										WxChatApi.sendFriendErrorMsg(focusVo.getDevicetype(), focusVo.getSessionid(), focusVo.getIp(), uid, uid, uid, focusVo.getRchatlinkid(),
										        AppCode.FriendErrorCode.SYS_ERROR, RetUtils.getRetMsg(ret));
									}
									continue;
								}
								boolean isToRead = RetUtils.getOkTData(ret);
								if (isToRead) {
									Long tochatlinkid = RetUtils.getOkTData(ret, "tochatlinkid");
									Integer touid = RetUtils.getOkTData(ret, "touid");
									if (WxSynApi.isSynVersion()) {
										if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
											WxSynApi.synMsgFocusJoin(tochatlinkid, touid, focusVo);
										}
									} else {
										if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
											WxChatApi.userChatOper(focusVo.getDevicetype(), focusVo.getSessionid(), focusVo.getIp(), focusVo.getAppversion(), focusVo.getUid(),
											        -focusVo.getGroupid(), Const.WxUserOper.TO_READ, "你已读群消息", focusVo.getChannelid());
										} else {
											WxChatApi.userChatOper(focusVo.getDevicetype(), focusVo.getSessionid(), focusVo.getIp(), focusVo.getAppversion(), touid, tochatlinkid,
											        Const.WxUserOper.TO_READ, "好友已读你的消息", focusVo.getChannelid());
											WxReadAck readAck = new WxReadAck();
											readAck.setUid(touid);
											readAck.setTouid(uid);
											readAck.setChatlinkid(tochatlinkid);
											readAck.setType(Const.YesOrNo.NO);
											readAck.setDevicetype(focusVo.getDevicetype());
											readAck.save();
										}
									}

								}
								break;
							case FocusVo.operCode.LEAVE:
								ChatMsgService.leaveDeal(focusVo);
								WxChatApi.focusNtf(focusVo.getUid(), null);
								break;
							case FocusVo.operCode.REFRESH:
								ChatMsgService.refreshDeal(focusVo);
								break;
							default:
								log.error("无效焦点操作：opercode:{}", focusVo.getOper());
								break;
							}
						} catch (Exception e) {
							log.error("", e);
						}
					}

				}
			}).start();
		}
	}

	/**
	 * 后消息逻辑处理
	 * @author lixinji
	 * 2020年10月12日 下午2:19:31
	 */
	public static void groupSendMsgAfterQueueInit() {
		for (int i = 0; i < groupSendMsgAfterIndex; i++) {
			final int qindex = i;
			groupSendMsgAfterQueue.add(new LinkedBlockingQueue<AfterGroupMsgSendVo>());
			groupMergeMsgMap.add(new HashMap<String, ChatMsgMergeVo>());
			new Thread(new Runnable() {

				@Override
				public void run() {
					int error = 0;
					while (true) {
						AfterGroupMsgSendVo afterVo = null;
						ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
						long[] threadIds = threadBean.findMonitorDeadlockedThreads();

						if (threadIds != null) {
							log.error("发现死锁线程：");
							for (long threadId : threadIds) {
								log.error("线程 " + threadId + " 处于死锁状态。");
							}
						}
						try {
//							log.error("info groupSendMsgAfterQueueInit 消费队列 begin 线程信息：" + Thread.currentThread().getName());
							afterVo = groupSendMsgAfterQueue.get(qindex).take();
//							log.error("info groupSendMsgAfterQueueInit 消费队列 end 线程信息：" + Thread.currentThread().getName());
							error = 0;
						} catch (InterruptedException e1) {
							error++;
							log.error("队列获取群发送对象失败，队列index:{}，错误次数：{}", qindex, error, e1);
							if (error >= 5) {
								log.error("队列联系获取群发送对象5次失败，退出循环，队列index:{}", qindex);
								break;
							}
							continue;
						}
						try {
							if (afterVo == null) {
								log.error("群消息对象为空，队列index:{}", qindex);
								continue;
							}
							if (afterVo.getMsg() == null) {
								log.error("群消息对象消息为空,focusvo:{}", Json.toJson(afterVo));
								continue;
							}
							Long groupid = afterVo.getMsg().getGroupid();
							//							int msgNotRead = afterVo.getIndex();
							//							Map<String, ChatMsgMergeVo> groupMap = groupMergeMsgMap.get(qindex);
							//							ChatMsgMergeVo mergeVo = groupMap.get(groupid + "");
							//							if(mergeVo != null) {
							//								int dealIndex = afterVo.getIndex();
							//								int curIndex = mergeVo.getCurrCount();
							//								if(curIndex != dealIndex) {
							//									log.error("合并消息处理前：count：{},atcount:{},msgNotRead:{}",curIndex,mergeVo.getAtDev(),msgNotRead);
							//									if(curIndex > dealIndex && StrUtil.isBlank(afterVo.getAts())) {
							//										log.error("合并消息处理：跳出循环，继续下一条");
							//										continue;
							//									} else if(curIndex > dealIndex){
							//										Integer dev = curIndex - dealIndex;
							//										log.error("合并消息处理：at消息，剩余，count：{},偏差前：{}，偏差后：{}",curIndex,mergeVo.getAtDev(),dev);
							//										mergeVo.addAndGetDev(dev);
							//									} 
							//								} else {
							//									mergeVo.setCurrCount(0);
							//									mergeVo.setAtDev(0);
							//								}
							//								//此处出现异常的话，存在并发问题
							//								msgNotRead = dealIndex - mergeVo.getAtDev();
							//								if(msgNotRead <= 0) {
							//									log.error("合并消息处理异常,未读消息为0或者负数,notreadcount:{}",msgNotRead);
							//									mergeVo.setCurrCount(0);
							//									mergeVo.setAtDev(0);
							//									msgNotRead = 0;
							//								}
							//							}
							ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
							WriteLock writeLock = rwLock.writeLock();
							writeLock.lock();
							try {
								//未读处理
								//								ChatMsgService.me.afterSendGroupChatMsg(afterVo.getMsg(), (short)msgNotRead,Const.YesOrNo.YES);
								ChatMsgService.me.afterSendGroupChatMsg(afterVo.getMsg(), 1, Const.YesOrNo.YES);
								//已读处理
								ChatMsgService.me.afterSendGroupChatMsg(afterVo.getMsg(), null, Const.YesOrNo.YES);
								if (StrUtil.isNotBlank(afterVo.getAts())) {
									//at逻辑处理
									ChatMsgService.me.afterSendGroupAtMsg(afterVo.getMsg(), afterVo.getAts(), 1, Const.YesOrNo.YES);
									//									ChatMsgService.me.afterSendGroupAtMsg(afterVo.getMsg(), afterVo.getAts(), (short)msgNotRead,Const.YesOrNo.YES);
								}
							} catch (Exception e) {
								// TODO: handle exception
							} finally {
								writeLock.unlock();
							}
						} catch (Exception e) {
							log.error("", e);
						}
					}

				}
			}).start();
		}
	}

	public WxChatQueueApi() {
	}

	/**
	 * 离开焦点添加队列
	 * @param curr
	 * @param devicetype
	 * @author lixinji
	 * 2020年6月5日 下午2:54:07
	 */
	public static void leaveFocusQueue(User curr, Short devicetype, String cid) {
		FocusVo focusVo = new FocusVo();
		focusVo.setUid(curr.getId());
		focusVo.setDevicetype(devicetype);
		focusVo.setOper(FocusVo.operCode.LEAVE);
		focusVo.setChannelid(cid);
		int queueindex = curr.getId() % focusIndex;
		try {
			//此处会阻塞队列，如果是短链接谨慎使用
			focusQueue.get(queueindex).put(focusVo);
//			log.error("info leaveFocusQueue put 线程信息：" + Thread.currentThread().getName());
		} catch (InterruptedException e) {
			log.error("", e);
		}
	}

	/**
	 * 刷新焦点添加队列
	 * @param curr
	 * @param devicetype
	 * @author lixinji
	 * 2020年6月5日 下午2:58:06
	 */
	public static void refreshFocusQueue(User curr, Short devicetype, String cid) {
		FocusVo focusVo = new FocusVo();
		focusVo.setUid(curr.getId());
		focusVo.setDevicetype(devicetype);
		focusVo.setOper(FocusVo.operCode.REFRESH);
		focusVo.setChannelid(cid);
		int queueindex = curr.getId() % focusIndex;
		try {
			//此处会阻塞队列，如果是短链接谨慎使用
			focusQueue.get(queueindex).put(focusVo);
//			log.error("info refreshFocusQueue put 线程信息：" + Thread.currentThread().getName());
		} catch (InterruptedException e) {
			log.error("", e);
		}
	}

	/**
	 * 进入焦点添加队列
	 * @param curr
	 * @param chatlinkid
	 * @param groupid
	 * @param chatmode
	 * @param devicetype
	 * @param ipid
	 * @author lixinji
	 * 2020年6月5日 下午2:46:04
	 */
	public static void joinFocusQueue(ChannelContext channelContext, User curr, Long chatlinkid, Long groupid, Short chatmode, Short devicetype, Integer ipid) {
		if (Objects.equals(devicetype, Devicetype.IOS.getValue()) || Objects.equals(devicetype, Devicetype.ANDROID.getValue())) {
			devicetype = Devicetype.APP.getValue();
		}
		String appversion = "0.0.0";
		String ip = "0.0.0.0";
		if (channelContext != null) {
			MobileInfo mobileInfo = ImUtils.getMobileInfo(channelContext);
			if (mobileInfo != null) {
				appversion = mobileInfo.getAppversion();
			}
			ip = channelContext.getClientNode().getIp();
		}
		FocusVo focusVo = new FocusVo();
		focusVo.setChannelid(channelContext.getId());
		focusVo.setUid(curr.getId());
		focusVo.setChatlinkid(chatlinkid);
		focusVo.setDevicetype(devicetype);
		focusVo.setChatmode(chatmode);
		focusVo.setIpid(ipid);
		focusVo.setIp(ip);
		focusVo.setAppversion(appversion);
		focusVo.setGroupid(groupid);
		focusVo.setOper(FocusVo.operCode.JOIN);
		int queueindex = curr.getId() % focusIndex;
		try {
			//此处会阻塞队列，如果是短链接谨慎使用
			focusQueue.get(queueindex).put(focusVo);
//			log.error("info joinFocusQueue put 线程信息：" + Thread.currentThread().getName());
		} catch (InterruptedException e) {
			log.error("", e);
		}

	}

	/**
	 * TODO:此处可以进行数据库操作合并-待优化
	 * @param groupid
	 * @param msg
	 * @param notreadcount
	 * @param viewflag
	 * @param ats
	 * @author lixinji
	 * 2020年10月12日 下午2:14:56
	 */
	public static void joinGroupSendMsgAfterQueue(Long groupid, WxGroupMsg msg, Short notreadcount, String ats) {
		AfterGroupMsgSendVo sendVo = new AfterGroupMsgSendVo(msg, notreadcount, ats);
		Long queueindex = groupid % groupSendMsgAfterIndex;
		try {

			//			//此处可以使用AtomicInteger
			//			Map<String, ChatMsgMergeVo> groupMap = groupMergeMsgMap.get(queueindex.intValue());
			//			ChatMsgMergeVo mergeVo = groupMap.get(groupid + "");
			//			if(notreadcount == null) {
			//				notreadcount = 1;
			//			}
			//			int index = notreadcount;
			//			if(mergeVo == null) {
			//				ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_USER_QUEUE_KEY_PREFIX + "." + msg.getGroupid() + "_g_" + msg.getUid(), AfterGroupMsgSendVo.class);
			//				WriteLock writeLock = rwLock.writeLock();
			//				writeLock.lock();
			//				try {
			//					mergeVo = groupMap.get(groupid + "");
			//					if(mergeVo == null) {
			//						mergeVo = new ChatMsgMergeVo(new Integer(notreadcount));
			//					}
			//					groupMap.put(groupid  + "", mergeVo);
			//				} catch (Exception e) {
			//					log.error("",e);
			//				} finally {
			//					writeLock.unlock();
			//				}
			//			} else {
			//				index = mergeVo.addAndGet(notreadcount);
			//			}
			//			sendVo.setIndex(index);
			//			log.error("合并消息前统计：count:{},偏差:{}",mergeVo.getCurrCount(),mergeVo.getAtDev());
			//此处会阻塞队列，如果是短链接谨慎使用
			groupSendMsgAfterQueue.get(queueindex.intValue()).put(sendVo);
//			log.error("info joinGroupSendMsgAfterQueue put 线程信息：" + Thread.currentThread().getName());
		} catch (InterruptedException e) {
			log.error("", e);
		}

	}
}

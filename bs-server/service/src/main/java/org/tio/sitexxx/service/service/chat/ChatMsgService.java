
package org.tio.sitexxx.service.service.chat;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.service.atom.AbsTxAtom;
import org.tio.sitexxx.service.service.base.UserService;
import org.tio.sitexxx.service.service.conf.ConfService;
import org.tio.sitexxx.service.utils.LockUtils.LockPrefix;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.service.vo.Devicetype;
import org.tio.sitexxx.service.vo.wx.FocusVo;
import org.tio.utils.SystemTimer;
import org.tio.utils.cache.ICache;
import org.tio.utils.lock.ListWithLock;
import org.tio.utils.lock.LockUtils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 聊天消息服务
 *
 * @author lixinji
 * 2019年12月31日 下午5:57:32
 */
public class ChatMsgService {
    private static Logger log = LoggerFactory.getLogger(ChatMsgService.class);
    public static final ChatMsgService me = new ChatMsgService();

    static final ICache CHAT_FOUCUS_CACHE = Caches.getCache(CacheConfig.CHAT_ON_FOCUS_3);

    static final ICache CHAT_FOUCUS_DEVICE_CACHE = Caches.getCache(CacheConfig.CHAT_ON_FOCUS_DEVICE_2);

    /**
     * 撤回最大消息
     */
    public static Long MSG_BACK_MAX_TIME = ConfService.getLong(Const.ConfMapping.WX_MSG_BACK_MAX_TIME, 1000L * 120);

    /**
     * 消息模板
     *
     * @author lixinji
     * 2020年3月18日 上午10:37:03
     */
    public interface MsgTemplate {

        /**
         * 创建群消息模板
         */
//        String create = "%%% 邀请 ### 加入了群聊";
        String create = "创建了群聊";
        /**
         * 进群消息模板
         */
        String join = "%%% 邀请 ### 加入了群聊";

        /**
         * 群主退群模板
         */
        String ownerleave = "%%% 退出了群聊，### 自动成为群主";

        /**
         * 主动退群模板
         */
        String leave = "退出了群聊";
//        String leave = "%%% 退出了群聊";

        /**
         * 踢人退群模板
         */
        String operkick = "%%% 将 ### 移除了群聊";

        /**
         * 被踢者接受到的模板
         */
        String tokick = "### 被 %%% 移除了群聊";

        /**
         * 撤回消息模板
         */
//        String msgback = "%%% 撤回了一条消息";
        String msgback = "撤回了一条消息";
        String msgback1 = "撤回了一条消息";

        /**
         * 新的朋友圈消息
         */
        String newmoment = "%%% 发布了一条新的朋友圈";

        /**
         * 新的朋友圈消息
         */
        String commentorlike = "{\"title\":\"您有一条新的朋友圈消息\", \"type\":%%%, \"id\":###, \"text\":\"@@@\", \"mid\":\"mmm\", \"pid\":\"ppp\"}";

        /**
         * 圈子名片发送
         */
        String circlecard = "{\"title\":\"帮办圈子名片\", \"id\":%%%, \"name\":###, \"avatar\":\"@@@\"}";

        /**
         * 笔记发送
         */
        String note = "{\"title\":\"aaa\", \"id\":%%%, \"imgUrl\":###, \"text\":\"@@@\"}";

        /**
         * 收藏发送
         */
        String sendcollectmsg = "{\"title\":\"收藏转发\", \"id\":%%%, \"content\":###}";

        /**
         * 管理员撤回消息模板
         */
        String managermsgback = "%%% 撤回了一条成员消息";

        /**
         * 群转移模板
         */
        String ownerchange = "%%% 将群主转让给了 ###";

        /**
         * 开启群邀请信息开关
         */
        String applyopen = "%%% 开启了群邀请开关：所有人都可以邀请人员进群";

        /**
         * 关闭群邀请信息开关
         */
        String applyclose = "%%% 关闭了群邀请开关：只有群主或者群管理员才能邀请人员进群";

        /**
         * 开启群审核开关
         */
        String reviewopen = "%%% 开启群审核开关：成员进群前,必须群主或者群管理员审核通过";

        /**
         * 开启群审核开关
         */
        String inviteCodeOpen = "%%% 开启群邀请码：可通过群邀请码邀请好友进群";
        String inviteCodeClose = "%%% 关闭群邀请码：无法通过群邀请码邀请好友进群";

        /**
         * 开启群审核开关
         */
        String memberNumOpen = "%%% 开启群成员数量显示：群组详情中显示群成员数量";
        String memberNumClose = "%%% 关闭群成员数量显示：群组详情中不显示群成员数量";

        /**
         * 开启群审核开关
         */
        String groupNoticeRollOpen = "%%% 开启群公告滚动";
        String groupNoticeRollClose = "%%% 关闭群公告滚动";

        /**
         * 关闭群审核开关
         */
        String reviewclose = "%%% 关闭了群审核开关：成员进群不需要审核";
        String addnotice = "%%% 添加了群公告:###";

        /**
         * 修改群公告
         */
        String updatenotice = "%%% 修改了群公告:###";
        /**
         * 修改群公告
         */
        String delnotice = "%%% 删除了群公告:###";
        String groupsign = "群成员 %%% 签到成功";

        /**
         * 修改群名称
         */
        String updatename = "%%% 修改了群名称:###";

        /**
         * 解散群
         */
        String delgroup = "解散了群";
//        String delgroup = "%%% 解散了群";

        /**
         * 抢红包模板
         */
        String grab = "%%% 领取了 ### 的红包";

        /**
         * 禁言模板
         */
        String forbidden = "### 已被禁言";

        /**
         * 解除禁言
         */
        String cancelforbidden = "### 已被解除禁言 ";
    }

    /*******************************************begin-调整-**********************************************************/

    /**
     * 私聊消息列表-已调整
     *
     * @param chatlinkid
     * @param startmid
     * @return
     * @author lixinji
     * 2020年1月31日 上午11:18:36
     */
    public Ret p2pMsgList(Long chatlinkid, Integer uid, Long startmid, Long endmid) {
        long currTime = SystemTimer.currentTimeMillis();
        Short type = 1;
        WxChatItems items = ChatService.me.getBaseChatItems(chatlinkid);
        if (items == null || !Objects.equals(items.getChatmode(), Const.ChatMode.P2P)) {
            //TODO:lixinji-如果未激活（一般是BUG,或者其他系统异常导致），是否处理
            return RetUtils.invalidParam();
        }
        if (!Objects.equals(items.getUid(), uid)) {
            return RetUtils.grantError();
        }
        String key = items.getFidkey();
        if (StrUtil.isBlank(key)) {
            return RetUtils.versionError();
        }
        if (startmid == null && endmid == null) {
            ListWithLock<WxFriendMsg> listWithLock = FriendService.me.getP2PMsgList(chatlinkid);
            type = 1;
            long endtime = SystemTimer.currentTimeMillis();
            long useTime = endtime - currTime;
            if (useTime > 500) {
                log.error("私聊消息查询慢，条件：type:{},usetime:{},uid:{},chatlinkid:{}", type, useTime, uid, chatlinkid);
            }
            if (listWithLock != null) {
                return RetUtils.okData(listWithLock.getObj());
            } else {
                return RetUtils.okData(new ArrayList<WxFriendMsg>());
            }
        } else if (startmid != null) {
            List<WxFriendMsg> msglistList = FriendService.me.getOtherP2PMsgList(chatlinkid, startmid);
            type = 2;
            long endtime = System.currentTimeMillis();
            long useTime = endtime - currTime;
            if (useTime > 500) {
                log.error("私聊消息查询慢，条件：type:{},usetime:{},uid:{},chatlinkid:{}", type, useTime, uid, chatlinkid);
            }
            if (msglistList != null) {
                return RetUtils.okData(msglistList);
            } else {
                return RetUtils.okData(new ArrayList<WxFriendMsg>());
            }
        } else {
            List<WxFriendMsg> msglistList = FriendService.me.getSynP2PMsgList(chatlinkid, endmid);
            type = 3;
            long endtime = System.currentTimeMillis();
            long useTime = endtime - currTime;
            if (useTime > 500) {
                log.error("私聊消息查询慢，条件：type:{},usetime:{},uid:{},chatlinkid:{}", type, useTime, uid, chatlinkid);
            }
            if (msglistList != null) {
                return RetUtils.okData(msglistList);
            } else {
                return RetUtils.okData(new ArrayList<WxFriendMsg>());
            }
        }
    }

    /**
     * 获取群聊消息列表-已调整
     *
     * @param chatlinkid
     * @param uid
     * @param startmid
     * @return
     * @author lixinji
     * 2020年2月13日 下午8:47:04
     */
    public Ret groupMsgList(Long chatlinkid, Integer uid, Long startmid, Long endmid) {
        long currTime = System.currentTimeMillis();
        Short type = 1;
        WxChatItems items = ChatService.me.getBaseChatItems(chatlinkid);
        if (items == null || !Objects.equals(items.getChatmode(), Const.ChatMode.GROUP)) {
            //TODO:lixinji-如果未激活（一般是BUG,或者其他系统异常导致），是否处理
            return RetUtils.invalidParam();
        }
        if (!Objects.equals(items.getUid(), uid)) {
            return RetUtils.grantError();
        }
        if (Objects.equals(items.getStatus(), Const.Status.DISABLED)) {
            return RetUtils.okData(new ArrayList<WxGroupMsg>());
        }
        if (startmid == null && endmid == null) {
            type = 1;
            List<WxGroupMsg> list = GroupService.me.getGroupCacheMsgList(chatlinkid);
            for (WxGroupMsg wxGroupMsg : list) {

                WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(wxGroupMsg.getF(), wxGroupMsg.getG());
                if (groupItem != null) {
                    wxGroupMsg.setGrouprole(groupItem.getGrouprole());
                    WxGroup group = WxGroup.dao.findById(wxGroupMsg.getG());
                    if (groupItem.getGrouprole().equals((short) 1)) {
                        wxGroupMsg.setLabel(group.getGroupleaderlabel() != null && !group.getGroupleaderlabel().isEmpty() ? group.getGroupleaderlabel() : "群主");
                    } else if (groupItem.getGrouprole().equals((short) 3)) {
                        wxGroupMsg.setLabel(group.getGroupmanagerlabel() != null && !group.getGroupmanagerlabel().isEmpty() ? group.getGroupmanagerlabel() : "管理员");
                    } else {
                        wxGroupMsg.setLabel(group.getGroupmemberlabel() != null && !group.getGroupmemberlabel().isEmpty() ? group.getGroupmemberlabel() : "成员");
                    }
                }

            }
            long endtime = System.currentTimeMillis();
            long useTime = endtime - currTime;
            if (useTime > 500) {
                log.error("群消息查询慢，条件：type:{},usetime:{},uid:{},chatlinkid:{}", type, useTime, uid, chatlinkid);
            }
            if (list != null) {
                return RetUtils.okData(list);
            } else {
                return RetUtils.okData(new ArrayList<WxGroupMsg>());
            }
        } else if (startmid != null) {
            type = 2;
            List<WxGroupMsg> msglistList = GroupService.me.getOtherGroupMsgList(chatlinkid, startmid);
            for (WxGroupMsg wxGroupMsg : msglistList) {
                WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(wxGroupMsg.getF(), wxGroupMsg.getG());
                if (groupItem == null) {
                    wxGroupMsg.setGrouprole((short) 2);
                    wxGroupMsg.setLabel("成员");
                    continue;
                }
                wxGroupMsg.setGrouprole(groupItem.getGrouprole());
                WxGroup group = WxGroup.dao.findById(wxGroupMsg.getG());
                if (groupItem.getGrouprole().equals((short) 1)) {
                    wxGroupMsg.setLabel(group.getGroupleaderlabel() != null && !group.getGroupleaderlabel().isEmpty() ? group.getGroupleaderlabel() : "群主");
                } else if (groupItem.getGrouprole().equals((short) 3)) {
                    wxGroupMsg.setLabel(group.getGroupmanagerlabel() != null && !group.getGroupmanagerlabel().isEmpty() ? group.getGroupmanagerlabel() : "管理员");
                } else {
                    wxGroupMsg.setLabel(group.getGroupmemberlabel() != null && !group.getGroupmemberlabel().isEmpty() ? group.getGroupmemberlabel() : "成员");
                }
            }
            long endtime = System.currentTimeMillis();
            long useTime = endtime - currTime;
            if (useTime > 500) {
                log.error("群消息查询慢，条件：type:{},usetime:{},uid:{},chatlinkid:{}", type, useTime, uid, chatlinkid);
            }
            if (msglistList != null) {
                return RetUtils.okData(msglistList);
            } else {
                return RetUtils.okData(new ArrayList<WxGroupMsg>());
            }
        } else {
            type = 3;
            List<WxGroupMsg> msglistList = GroupService.me.getSynGroupMsgList(chatlinkid, endmid);
            for (WxGroupMsg wxGroupMsg : msglistList) {
                WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(wxGroupMsg.getF(), wxGroupMsg.getG());
                wxGroupMsg.setGrouprole(groupItem.getGrouprole());
                WxGroup group = WxGroup.dao.findById(wxGroupMsg.getG());
                if (groupItem.getGrouprole().equals((short) 1)) {
                    wxGroupMsg.setLabel(group.getGroupleaderlabel() != null && !group.getGroupleaderlabel().isEmpty() ? group.getGroupleaderlabel() : "群主");
                } else if (groupItem.getGrouprole().equals((short) 3)) {
                    wxGroupMsg.setLabel(group.getGroupmanagerlabel() != null && !group.getGroupmanagerlabel().isEmpty() ? group.getGroupmanagerlabel() : "管理员");
                } else {
                    wxGroupMsg.setLabel(group.getGroupmemberlabel() != null && !group.getGroupmemberlabel().isEmpty() ? group.getGroupmemberlabel() : "成员");
                }
            }
            long endtime = System.currentTimeMillis();
            long useTime = endtime - currTime;
            if (useTime > 500) {
                log.error("群消息查询慢，条件：type:{},usetime:{},uid:{},chatlinkid:{}", type, useTime, uid, chatlinkid);
            }
            if (msglistList != null) {
                return RetUtils.okData(msglistList);
            } else {
                return RetUtils.okData(new ArrayList<WxGroupMsg>());
            }
        }
    }

    /*********************************************end-调整-*********************************************************/

    /**
     * 消息操作-已调整
     *
     * @param chatlinkid
     * @param oper
     * @param mid
     * @return
     * @author lixinji
     * 2020年2月11日 下午12:07:26
     */
    public Ret msgOper(User curr, Long chatlinkid, Short oper, String mids, Short chatmode) {
        if (chatlinkid == null || oper == null || StrUtil.isBlank(mids)) {
            return RetUtils.invalidParam();
        }
        if (Objects.equals(chatmode, Const.ChatMode.P2P)) {
            return p2pMsgOper(curr, chatlinkid, oper, mids);
        } else {
            return groupMsgOper(curr, chatlinkid, oper, mids);
        }

    }

    /**
     * 收藏列表
     *
     * @param uid
     * @param type
     * @param pageNumber
     * @return
     * @author lixinji
     * 2021年1月26日 下午5:16:53
     */
    public Ret favoriteList(Integer uid, Short type, Integer pageNumber) {
        if (type == null) {
            return RetUtils.invalidParam();
        }
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        Kv params = Kv.by("uid", uid);
        if (type != null) {
            params.set("type", type);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.favoriteList", params);
        Page<Record> page = Db.use(Const.Db.TIO_SITE_MAIN).paginate(pageNumber, 16, sqlPara);
        return RetUtils.okPage(page);
    }

    /**
     * 收藏
     *
     * @param curr
     * @param chatlinkid
     * @param oper
     * @param mids
     * @param chatmode
     * @return
     * @author lixinji
     * 2021年1月26日 下午4:49:07
     */
    public Ret msgFavorite(User user, Long mid, Short chatmode) {
        if (mid == null || chatmode == null) {
            return RetUtils.invalidParam();
        }
        WxUserFavorites favorites = new WxUserFavorites();
        favorites.setUid(user.getId());
        favorites.setChatmode(chatmode);
        favorites.setMid(mid);
        if (Objects.equals(chatmode, Const.ChatMode.GROUP)) {
            WxGroupMsg msg = WxGroupMsg.dao.findById(mid);
            if (msg == null) {
                return RetUtils.noExistParam();
            }
            favorites.setMsgtext(msg.getText());
            favorites.setType(msg.getContenttype());
            switch (msg.getContenttype()) {
                case Const.ContentType.FILE:
                    favorites.setBizstr(msg.getFc().getUrl());
                    break;
                case Const.ContentType.IMG:
                    favorites.setBizstr(msg.getIc().getUrl());
                    break;
                case Const.ContentType.VIDEO:
                    favorites.setBizstr(msg.getVc().getUrl());
                    break;
                default:
                    favorites.setBizstr(msg.getText());
            }
        } else {
            WxFriendMsg msg = WxFriendMsg.dao.findById(mid);
            if (msg == null) {
                return RetUtils.noExistParam();
            }
            favorites.setType(msg.getContenttype());
            favorites.setMsgtext(msg.getText());
            switch (msg.getContenttype()) {
                case Const.ContentType.FILE:
                    favorites.setBizstr(msg.getFc().getUrl());
                    break;
                case Const.ContentType.IMG:
                    favorites.setBizstr(msg.getIc().getUrl());
                    break;
                case Const.ContentType.VIDEO:
                    favorites.setBizstr(msg.getVc().getUrl());
                    break;
                default:
                    favorites.setBizstr(msg.getText());
            }
        }
        favorites.replaceSave();
        return RetUtils.okOper();
    }

    /**
     * 取消收藏
     *
     * @param user
     * @param mid
     * @param chatmode
     * @return
     * @author lixinji
     * 2021年1月26日 下午5:07:24
     */
    public Ret cancelMsgFavorite(Integer fid) {
        if (fid == null) {
            return RetUtils.invalidParam();
        }
        WxUserFavorites.dao.deleteById(fid);
        return RetUtils.okOper();
    }

    /**
     * 私聊消息操作-已调整
     *
     * @param curr
     * @param chatlinkid
     * @param oper
     * @param mids
     * @return
     * @author lixinji
     * 2020年3月11日 下午2:07:33
     */
    public Ret p2pMsgOper(User curr, Long chatlinkid, Short oper, String mids) {
        WxChatUserItem chatUserItem = ChatIndexService.chatUserIndex(chatlinkid);
        if (!ChatService.friendExistChat(chatUserItem)) {
            return RetUtils.noExistParam();
        }
        Ret ret = null;
        switch (oper) {
            case Const.WxMsgOper.DEL:
                ret = FriendService.me.delMsg(chatUserItem, mids);
                break;
            case Const.WxMsgOper.BACK:
                ret = FriendService.me.backMsg(chatUserItem, mids);
                break;
            case Const.WxMsgOper.MOMENT:
                ret = RetUtils.okOper().set(":msg", "新的朋友圈");
                break;
            case Const.WxMsgOper.COMMENTSORLIKES:
                ret = RetUtils.okOper().set(":msg", "新的朋友圈消息");
                break;
            case Const.WxMsgOper.REPORT:
                ret = RetUtils.okOper();
                break;
            default:
                return RetUtils.noImplement();
        }
        return ret;
    }

    /**
     * 群聊消息操作-已调整
     *
     * @param curr
     * @param chatlinkid
     * @param oper
     * @param mids
     * @return
     * @author lixinji
     * 2020年3月11日 下午2:07:37
     */
    public Ret groupMsgOper(User curr, Long chatlinkid, Short oper, String mids) {
        WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(chatlinkid);
        Ret ret = null;
        switch (oper) {
            case Const.WxMsgOper.DEL:
                ret = GroupService.me.delMsg(groupItem, mids);
                break;
            case Const.WxMsgOper.BACK:
                ret = GroupService.me.backMsg(groupItem, mids);
                break;
            case Const.WxMsgOper.REPORT:
                ret = RetUtils.okOper();
                break;
            default:
                return RetUtils.noImplement();
        }
        return ret;
    }

    /**
     * 队列-进入节点处理
     * 1：记录所有的节点的焦点
     * 2：如果是H5/PC,记录下所有设备的焦点会话
     * 2021-01-29修改：焦点移交给会话处理，缓存不记录焦点逻辑
     *
     * @param focusVo
     * @author lixinji
     * 2020年6月5日 下午2:49:14
     */
    @SuppressWarnings("unchecked")
    public static Ret joinDeal(FocusVo focusVo) {
        String key = focusVo.getUid() + "_" + focusVo.getDevicetype();
        if (!Objects.equals(focusVo.getDevicetype(), Devicetype.APP.getValue())) {
            //不是app的话，需要记录通道id
            key += "_" + focusVo.getChannelid();
        }
        Serializable object = CHAT_FOUCUS_CACHE.get(key);
        if (object != null) {
            CHAT_FOUCUS_CACHE.remove(key);
            FocusVo old = (FocusVo) object;
            if (Objects.equals(old.getChatmode(), Const.ChatMode.GROUP)) {
                int many = manyFocus(old.getUid(), old.getRchatlinkid(), focusVo.getDevicetype());
                if (many == 0) {
                    WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(old.getUid(), old.getGroupid());
                    if (groupItem != null && groupItem.getChatlinkmetaid() != null) {
                        ChatIndexService.me.updateFocus(groupItem.getChatlinkmetaid(), Const.YesOrNo.NO);
                    }
                    //					ChatIndexService.me.updateFocus(old.getGroupid(),old.getUid(), Const.YesOrNo.NO);
                }
            }
        }
        CHAT_FOUCUS_CACHE.put(key, focusVo);
        //此处调整到后处理逻辑
        //		if(Objects.equals(focusVo.getChatmode(), Const.ChatMode.GROUP)) {
        //			int many = manyFocus(focusVo.getUid(), focusVo.getRchatlinkid(),focusVo.getDevicetype());
        //			if(many == 0) {
        //				WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(focusVo.getUid(), focusVo.getGroupid());
        //				if(groupItem.getChatlinkmetaid() != null) {
        //					ChatIndexService.me.updateFocus(groupItem.getChatlinkmetaid(),Const.YesOrNo.YES);
        //				}
        //				ChatIndexService.me.updateFocus(focusVo.getGroupid(),focusVo.getUid(), Const.YesOrNo.YES);
        //			}
        //		}
        if (!Objects.equals(focusVo.getDevicetype(), Devicetype.APP.getValue())) {
            //记录设备信息
            String devicekey = focusVo.getUid() + "_" + focusVo.getDevicetype();
            Serializable deviceObject = CHAT_FOUCUS_DEVICE_CACHE.get(devicekey);
            if (deviceObject != null) {
                HashMap<String, Long> deviceMap = (HashMap<String, Long>) deviceObject;
                deviceMap.put(focusVo.getChannelid(), focusVo.getRchatlinkid());
            } else {
                HashMap<String, Long> deviceMap = new HashMap<String, Long>();
                deviceMap.put(focusVo.getChannelid(), focusVo.getRchatlinkid());
                CHAT_FOUCUS_DEVICE_CACHE.put(devicekey, deviceMap);
            }
        }
        return beforeFocus(focusVo.getUid(), focusVo.getChatlinkid(), focusVo.getIpid(), focusVo.getDevicetype());
    }

    /**
     * 队列-刷新节点处理
     *
     * @param focusVo
     * @author lixinji
     * 2020年6月5日 下午2:53:18
     */
    public static void refreshDeal(FocusVo focusVo) {
        String key = focusVo.getUid() + "_" + focusVo.getDevicetype();
        if (!Objects.equals(focusVo.getDevicetype(), Devicetype.APP.getValue())) {
            //不是app的话，需要记录通道id
            key += "_" + focusVo.getChannelid();
        }
        Serializable object = CHAT_FOUCUS_CACHE.get(key);
        if (object != null) {
            CHAT_FOUCUS_CACHE.remove(key);
            CHAT_FOUCUS_CACHE.put(key, object);
        }
        if (!Objects.equals(focusVo.getDevicetype(), Devicetype.APP.getValue())) {
            //记录设备信息
            String devicekey = focusVo.getUid() + "_" + focusVo.getDevicetype();
            Serializable deviceObject = CHAT_FOUCUS_DEVICE_CACHE.get(devicekey);
            if (deviceObject != null) {
                ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(CHAT_FOUCUS_DEVICE_CACHE.getCacheName() + "_" + devicekey, null);
                WriteLock writeLock = lock.writeLock();
                writeLock.lock();
                try {
                    CHAT_FOUCUS_DEVICE_CACHE.remove(devicekey);
                    CHAT_FOUCUS_DEVICE_CACHE.put(devicekey, deviceObject);
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                } finally {
                    writeLock.unlock();
                }
            }
        }
    }

    /**
     * 获取焦点
     *
     * @param curr
     * @param devicetype
     * @return
     * @author lixinji
     * 2020年2月10日 下午4:16:20
     */
    public FocusVo getFocus(Integer uid, Short devicetype, String cid) {
        String key = uid + "_" + devicetype;
        if (StrUtil.isNotBlank(cid)) {
            key += "_" + cid;
        }
        Serializable object = CHAT_FOUCUS_CACHE.get(key);
        if (object != null) {
            ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(CHAT_FOUCUS_CACHE.getCacheName() + "_" + key, null);
            ReadLock readLock = lock.readLock();
            readLock.lock();
            try {
                return (FocusVo) object;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                readLock.unlock();
            }
        }
        return null;
    }

    /**
     * @param uid
     * @param devicetype
     * @return
     * @author lixinji
     * 2020年9月28日 下午6:36:13
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, Long> getFocusDeviceMap(Integer uid, Short devicetype) {
        String key = uid + "_" + devicetype;
        Serializable object = CHAT_FOUCUS_DEVICE_CACHE.get(key);
        if (object != null) {
            ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(CHAT_FOUCUS_CACHE.getCacheName() + "_" + key, null);
            ReadLock readLock = lock.readLock();
            readLock.lock();
            try {
                return (HashMap<String, Long>) object;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                readLock.unlock();
            }
        }
        return null;
    }

    /**
     * 队列-离开焦点处理
     *
     * @param leave
     * @author lixinji
     * 2020年6月5日 下午2:56:17
     */
    public static void leaveDeal(FocusVo leave) {
        String key = leave.getUid() + "_" + leave.getDevicetype();
        if (!Objects.equals(leave.getDevicetype(), Devicetype.APP.getValue())) {
            //不是app的话，需要记录通道id
            key += "_" + leave.getChannelid();
        }
        Serializable object = CHAT_FOUCUS_CACHE.get(key);
        if (object != null) {
            CHAT_FOUCUS_CACHE.remove(key);
            FocusVo focusVo = (FocusVo) object;
            if (Objects.equals(focusVo.getChatmode(), Const.ChatMode.GROUP)) {
                int many = manyFocus(focusVo.getUid(), focusVo.getRchatlinkid(), leave.getDevicetype());
                if (many == 0) {
                    WxChatGroupItem groupItem = ChatIndexService.chatGroupIndex(focusVo.getUid(), focusVo.getGroupid());
                    if (groupItem != null && groupItem.getChatlinkmetaid() != null) {
                        ChatIndexService.me.updateFocus(groupItem.getChatlinkmetaid(), Const.YesOrNo.NO);
                    }
                    //					ChatIndexService.me.updateFocus(focusVo.getGroupid(),focusVo.getUid(), Const.YesOrNo.NO);
                }
            }
        }
        if (!Objects.equals(leave.getDevicetype(), Devicetype.APP.getValue())) {
            //记录设备信息
            String devicekey = leave.getUid() + "_" + leave.getDevicetype();
            Serializable deviceObject = CHAT_FOUCUS_DEVICE_CACHE.get(devicekey);
            if (deviceObject != null) {
                ReentrantReadWriteLock lock = LockUtils.getReentrantReadWriteLock(CHAT_FOUCUS_DEVICE_CACHE.getCacheName() + "_" + devicekey, null);
                WriteLock writeLock = lock.writeLock();
                writeLock.lock();
                try {
                    if (deviceObject != null) {
                        @SuppressWarnings("unchecked")
                        HashMap<String, Long> deviceMap = (HashMap<String, Long>) deviceObject;
                        deviceMap.remove(leave.getChannelid());
                    }
                } catch (Throwable e) {
                    log.error(e.getMessage(), e);
                } finally {
                    writeLock.unlock();
                }
            }
        }
    }

    /**
     * @param uid
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年8月18日 下午4:40:25
     */
    public static int manyFocus(Integer uid, Long chatlinkid, Short devicetype) {
        int i = 0;
        if (!Objects.equals(devicetype, Devicetype.APP.getValue())) {
            FocusVo app = ChatMsgService.me.getFocus(uid, Devicetype.APP.getValue(), "");
            if (app != null && Objects.equals(app.getRchatlinkid(), chatlinkid)) {
                i++;
            }
        }
        if (!Objects.equals(devicetype, Devicetype.WEB.getValue())) {
            HashMap<String, Long> pc = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.WEB.getValue());
            if (pc != null) {
                for (String key : pc.keySet()) {
                    Long existChatLinkid = pc.get(key);
                    if (Objects.equals(existChatLinkid, chatlinkid)) {
                        i++;
                        break;
                    }
                }
            }
        }
        if (!Objects.equals(devicetype, Devicetype.H5.getValue())) {
            HashMap<String, Long> h5 = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.H5.getValue());
            if (h5 != null) {
                for (String key : h5.keySet()) {
                    Long existChatLinkid = h5.get(key);
                    if (Objects.equals(existChatLinkid, chatlinkid)) {
                        i++;
                        break;
                    }
                }
            }
        }
        return i;
    }

    /**
     * 私聊消息发送后处理-已调整
     * TODO:lixinji
     * update-lixinji-2020-0210:
     * 逻辑冗余：是否将发送消息的已读处理移到焦点处理，不在进行已读的清空处理
     * 正常逻辑应该移除，如果未来效率差，可以考虑优化次逻辑
     * TODO:lixinji-转发自己不能是未读消息，待处理
     *
     * @param msg          消息
     * @param user         发送者
     * @param chatlinkid   修改的聊天会话
     * @param readflag     自己的未读状态
     * @param toreadflag   自己的对方的未读状态
     * @param notreadcount 未读条数
     * @return
     * @author lixinji
     * 2020年1月17日 下午6:00:18
     */
    public Ret afterSendFriendChatMsg(WxFriendMsg msg, User user, Long chatlinkmetaid, Short readflag, Short toreadflag, Integer notreadcount) {
        if (chatlinkmetaid == null) {
            log.error("消息发送后处理,聊天会话为空");
            return RetUtils.failMsg("消息发送后处理,聊天会话为空");
        }
        if (user == null) {
            user = UserService.ME.getById(msg.getUid());
            if (user == null) {
                log.error("消息发送后处理,消息发送用户不存在");
                return RetUtils.failMsg("消息发送后处理,消息发送用户不存在");
            }
        }
        Kv params = Kv.by("id", chatlinkmetaid).set("lastmsgid", msg.getId()).set("lastmsguid", user.getId()).set("fromnick", user.getNick()).set("msgresume", msg.getResume())
                .set("msgtype", msg.getContenttype()).set("sendtime", msg.getTime()).set("sysflag", msg.getSendbysys()).set("viewflag", Const.YesOrNo.YES);
        if (readflag != null) {
            params.set("readflag", readflag);
            if (Objects.equals(readflag, Const.YesOrNo.YES)) {
                params.set("readnull", Const.YesOrNo.YES);
            } else {
                if (notreadcount != null && Objects.equals(Const.YesOrNo.NO, msg.getSendbysys())) {
                    params.set("notreadcount", notreadcount).set("notreadstartmsgid", msg.getId());
                }
            }
        }
        if (toreadflag != null) {
            params.set("toreadflag", toreadflag);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.sendP2PMsg", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        if (update <= 0) {
            return RetUtils.failMsg("后处理修改失败");
        }
        return Ret.ok();
    }

    /**
     * 群消息后处理-队列处理，不需要加锁-已调整
     * TODO:lixinji-转发自己不能是未读消息，待处理
     *
     * @param msg
     * @param user
     * @param notreadcount
     * @return
     * @author lixinji
     * 2020年2月17日 下午12:38:59
     */
    public Ret afterSendGroupChatMsg(WxGroupMsg msg, Integer notreadcount, Short viewflag) {
        Kv params = Kv.by("groupid", msg.getGroupid()).set("lastmsgid", msg.getId()).set("lastmsguid", msg.getUid()).set("fromnick", msg.getNick())
                .set("msgresume", msg.getResume()).set("msgtype", msg.getContenttype()).set("sendtime", msg.getTime()).set("sysflag", msg.getSendbysys())
                .set("linkflag", Const.YesOrNo.YES);
        if (notreadcount != null && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO)) {
            params.set("notreadcount", notreadcount).set("notreadstartmsgid", msg.getId()).set("focusflag", Const.YesOrNo.NO).set("readflag", Const.YesOrNo.NO);
        } else if (notreadcount == null) {
            params.set("focusflag", Const.YesOrNo.YES).set("readflag", Const.YesOrNo.YES);
        }
        if (Objects.equals(msg.getSendbysys(), Const.YesOrNo.YES)) {
            params.set("sysmsgkey", msg.getSysmsgkey()).set("opernick", msg.getOpernick()).set("tonicks", msg.getTonicks());
        }
        if (viewflag != null) {
            params.set("viewflag", viewflag);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.sendGroupMsg", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return RetUtils.okOper();
    }

    /**
     * at消息后处理-已调整
     * 队列处理-不用加锁
     *
     * @param msg
     * @param ats
     * @param notreadcount
     * @return
     * @author lixinji
     * 2020年4月7日 下午5:43:09
     */
    public Ret afterSendGroupAtMsg(WxGroupMsg msg, String ats, Integer notreadcount, Short viewflag) {
        if (StrUtil.isBlank(ats)) {
            return Ret.ok().set("msg", "无at信息");
        }
        Kv params = Kv.by("groupid", msg.getGroupid()).set("linkflag", Const.YesOrNo.YES);
        params.set("atnotreadcount", notreadcount).set("atnotreadstartmsgid", msg.getId()).set("focusflag", Const.YesOrNo.NO).set("atreadflag", Const.YesOrNo.NO);
        if (viewflag != null) {
            params.set("viewflag", viewflag);
        }
        if (ats.indexOf("all") < 0) {
            params.set("ats", ats);
        }
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.sendGroupAtMsg", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return RetUtils.okOper();
    }

    /**
     * 发送单通道消息-已调整
     *
     * @param msg
     * @param user
     * @param notreadcount
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年3月2日 上午11:13:37
     */
    public Ret afterSendGroupById(WxGroupMsg msg, Integer notreadcount, Long chatlinkmetaid, Short viewflag) {
        ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + msg.getGroupid(), WxChatItemsMeta.class);
        WriteLock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            Kv params = Kv.by("lastmsgid", msg.getId()).set("lastmsguid", msg.getUid()).set("fromnick", msg.getNick()).set("msgresume", msg.getResume())
                    .set("msgtype", msg.getContenttype()).set("sendtime", msg.getTime()).set("sysflag", msg.getSendbysys()).set("id", chatlinkmetaid);
            if (notreadcount != null && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO)) {
                params.set("notreadcount", notreadcount).set("notreadstartmsgid", msg.getId()).set("readflag", Const.YesOrNo.NO);
            } else if (notreadcount == null) {
                params.set("readflag", Const.YesOrNo.YES);
            }
            if (Objects.equals(msg.getSendbysys(), Const.YesOrNo.YES)) {
                params.set("sysmsgkey", msg.getSysmsgkey()).set("opernick", msg.getOpernick()).set("tonicks", msg.getTonicks());
            }
            if (viewflag != null) {
                params.set("viewflag", viewflag);
            }
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.sendGroupMsgById", params);
            Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            writeLock.unlock();
        }
        return RetUtils.okOper();
    }

    /**
     * 删除群消息处理-已调整
     *
     * @param msg
     * @param user
     * @param notreadcount
     * @return
     * @author lixinji
     * 2020年3月2日 上午11:10:52
     */
    public Ret delGroupChatMsg(WxGroupMsg msg, Integer notreadcount, Short viewflag) {
        ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + msg.getGroupid(), WxChatItemsMeta.class);
        WriteLock writeLock = rwLock.writeLock();
        writeLock.lock();
        try {
            Kv params = Kv.by("groupid", msg.getGroupid()).set("lastmsgid", msg.getId()).set("lastmsguid", msg.getUid()).set("fromnick", msg.getNick())
                    .set("msgresume", msg.getResume()).set("msgtype", msg.getContenttype()).set("sendtime", msg.getTime()).set("sysflag", msg.getSendbysys())
                    .set("linkflag", Const.YesOrNo.YES).set("newlink", Const.YesOrNo.NO);
            if (notreadcount != null && Objects.equals(msg.getSendbysys(), Const.YesOrNo.NO)) {
                params.set("notreadcount", notreadcount).set("notreadstartmsgid", msg.getId()).set("focusflag", Const.YesOrNo.NO).set("readflag", Const.YesOrNo.NO);
            } else if (notreadcount == null) {
                params.set("focusflag", Const.YesOrNo.YES).set("readflag", Const.YesOrNo.YES);
            }
            if (Objects.equals(msg.getSendbysys(), Const.YesOrNo.YES)) {
                params.set("sysmsgkey", msg.getSysmsgkey()).set("opernick", msg.getOpernick()).set("tonicks", msg.getTonicks());
            }
            if (viewflag != null) {
                params.set("viewflag", viewflag);
            }
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.delGroupChatMsg", params);
            Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            writeLock.unlock();
        }
        return RetUtils.okOper();
    }

    /**
     * 焦点处理
     *
     * @param msg
     * @param user
     * @param chatlinkid
     * @param readflag
     * @param toreadflag
     * @param notreadcount
     * @return
     * @author lixinji
     * 2020年2月5日 下午5:46:17
     */
    public static Ret beforeFocus(Integer uid, Long chatlinkid, Integer ipid, Short devicetype) {
        if (chatlinkid == null) {
            log.error("焦点处理异常：会话为空");
            return RetUtils.failMsg("焦点处理异常：会话为空");
        }
        WxChatUserItem userItem = ChatIndexService.chatUserIndex(chatlinkid);
        if (userItem == null) {
            return RetUtils.failMsg("会话索引不存在:" + chatlinkid);
        }
        AbsTxAtom focus = new AbsTxAtom() {

            @Override
            public boolean noTxRun() {
                //后续可进行数据库性能优化,进行分层处理,以减小数据库访问压力
                //如果消息\焦点逻辑分离到客户端,此处进行逻辑移除
                boolean isToRead = false;
                if (Const.MSG_READ_FOCUS_FLAG) {
                    //此处Tochatlinkid有可能为空，业务逻辑为空时为会话删除但不删除消息
                    //此处可优化,进行
                    boolean isGroup = Objects.equals(userItem.getChatmode(), Const.ChatMode.GROUP);
                    Ret read = chatRead(userItem.getChatlinkmetaid(), isGroup ? Const.YesOrNo.YES : null, isGroup ? userItem.getBizid() : null);
                    if (read.isFail()) {
                        log.info("焦点逻辑处理：会话已读未变化,{},{},{}", userItem.getUid(), userItem.getChatlinkid(), devicetype);
                    } else {
                        isToRead = true;
                        if (userItem.getTochatlinkid() != null) {
                            WxChatItemsMeta tometa = WxChatItemsMeta.dao.findById(userItem.getTochatlinkmetaid());
                            if (Objects.equals(tometa.getToreadflag(), Const.YesOrNo.NO)) {
                                Ret toread = chatToRead(userItem.getTochatlinkmetaid());
                                if (toread.isFail()) {
                                    log.error("焦点逻辑处理：对方会话已读未变化，{}", userItem.getTochatlinkid());
                                }
                            }
                        }
                        if (Objects.equals(userItem.getChatmode(), Const.ChatMode.P2P)) {
                            Ret msgRead = p2pMsgRead(userItem.getFidkey(), uid, ipid, devicetype);
                            if (msgRead.isFail()) {
                                log.warn("焦点逻辑处理：消息已读未变化,{}", userItem.getFidkey());
                            }
                        }
                    }
                } else {
                    isToRead = true;
                    boolean isGroup = Objects.equals(userItem.getChatmode(), Const.ChatMode.GROUP);
                    chatRead(userItem.getChatlinkmetaid(), isGroup ? Const.YesOrNo.YES : null, isGroup ? userItem.getBizid() : null);
                    if (userItem.getTochatlinkid() != null) {
                        chatToRead(userItem.getTochatlinkmetaid());
                    }
                    if (Objects.equals(userItem.getChatmode(), Const.ChatMode.P2P)) {
                        p2pMsgRead(userItem.getFidkey(), uid, ipid, devicetype);
                    }
                }
                return okRet(isToRead);
            }
        };
        boolean tx = Db.use(Const.Db.TIO_SITE_MAIN).tx(focus);
        if (!tx) {
            return RetUtils.sysError();
        }
        Ret okRet = focus.getRetObj();
        boolean isToRead = RetUtils.getOkTData(okRet);
        if (isToRead) {
            FriendService.me.clearP2pChatCache(userItem.getChatlinkid());
            if (userItem.getTochatlinkid() != null) {
                okRet.set("touid", userItem.getBizid().intValue());
                okRet.set("tochatlinkid", userItem.getTochatlinkid());
                FriendService.me.clearP2pChatCache(userItem.getTochatlinkid());
            }
            ChatIndexService.removeChatItemsCache(chatlinkid);
        }
        return okRet;
    }

    /**
     * 会话已读-已调整
     * 此处新增群会话焦点逻辑
     *
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年2月10日 上午10:59:49
     */
    public static Ret chatRead(Long chatlinkmetaid, Short atflag, Long groupid) {
        if (chatlinkmetaid == null) {
            log.error("处理异常：会话为空");
            return RetUtils.failMsg("处理异常：会话为空");
        }
        if (Const.MSG_READ_FOCUS_FLAG) {
            WxChatItemsMeta meta = WxChatItemsMeta.dao.findById(chatlinkmetaid);
            //			此处双判断
            if (Objects.equals(meta.getReadflag(), Const.YesOrNo.YES)) {
                if (meta.getNotreadcount() == 0) {
                    return RetUtils.failMsg("会话状态已经时已读");
                } else {
                    log.error("焦点进入处理：会话已读，但是未读条数不是0,chatlinkid:{},groupid:{}", meta.getChatlinkid(), groupid);
                }
            }
        }
        if (groupid == null) {
            Kv params = Kv.by("id", chatlinkmetaid).set("readflag", Const.YesOrNo.YES);
            if (atflag != null) {
                params.set("atreadflag", Const.YesOrNo.YES);
            }
            SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.chatRead", params);
            int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
            if (update <= 0) {
                return RetUtils.failMsg("已读未变化");
            }
        } else {
            ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_CHATITEM_META_KEY_PREFIX + "." + groupid, WxChatItemsMeta.class);
            WriteLock writeLock = rwLock.writeLock();
            writeLock.lock();
            try {
                Kv params = Kv.by("id", chatlinkmetaid).set("readflag", Const.YesOrNo.YES).set("focusflag", Const.YesOrNo.YES);
                if (atflag != null) {
                    params.set("atreadflag", Const.YesOrNo.YES);
                }
                SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.chatRead", params);
                int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
                if (update <= 0) {
                    return RetUtils.failMsg("已读未变化");
                }
            } catch (Exception e) {
                log.error("", e);
            } finally {
                writeLock.unlock();
            }
        }
        return Ret.ok();
    }

    /**
     * 会话对方已读-已调整
     *
     * @param chatlinkid
     * @return
     * @author lixinji
     * 2020年2月10日 上午11:00:07
     */
    public static Ret chatToRead(Long chatlinkmetaid) {
        if (chatlinkmetaid == null) {
            log.error("处理异常：会话为空");
            return RetUtils.failMsg("处理异常：会话为空");
        }
        Kv params = Kv.by("id", chatlinkmetaid).set("toreadflag", Const.YesOrNo.YES);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.chatToRead", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        if (update <= 0) {
            return RetUtils.failMsg("对方已读未变化");
        }
        return Ret.ok();
    }

    /**
     * 最后一条消息-已调整
     *
     * @param fidKey
     * @param touid
     * @return
     * @author lixinji
     * 2020年2月11日 下午1:45:49
     */
    public WxFriendMsg getLastMsg(String fidKey, Integer touid) {
        if (StrUtil.isBlank(fidKey) || touid == null) {
            return null;
        }
        WxFriendMsg last = WxFriendMsg.dao.findFirst("select * from wx_friend_msg where twouid = ? and sigleuid != ?  order by id desc limit 0,1", fidKey, touid);
        return last;
    }

    /**
     * 消息已读-已调整
     *
     * @param fidKey
     * @param uid
     * @param ipid
     * @param devicetype
     * @return
     * @author lixinji
     * 2020年2月11日 下午1:45:32
     */
    public static Ret p2pMsgRead(String fidKey, Integer uid, Integer ipid, Short devicetype) {
        if (StrUtil.isBlank(fidKey) || uid == null) {
            return RetUtils.invalidParam();
        }
        Kv params = Kv.by("touid", uid).set("twouid", fidKey).set("readipid", ipid).set("readdevice", devicetype).set("readflag", Const.YesOrNo.YES).set("noread",
                Const.YesOrNo.NO);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.p2pMsgRead", params);
        int update = Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        if (update <= 0) {
            return RetUtils.failMsg("已读未变化");
        }
        return Ret.ok();
    }

    /**
     * 群聊消息删除-已调整
     *
     * @param mids
     * @param uid
     * @param groupid
     * @return
     * @author lixinji
     * 2020年4月1日 上午11:06:38
     */
    public Ret groupMsgDel(String mids, Integer uid, Long groupid) {
        Kv params = Kv.by("uid", uid).set("mids", mids).set("groupid", groupid).set("yes", Const.YesOrNo.YES);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.groupMsgDel", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
        return Ret.ok();
    }

    /**
     * 重新绑定会话-已调整
     *
     * @param chatItems
     * @param groupItem
     * @return
     * @author lixinji
     * 2020年4月1日 下午4:03:30
     */
    public void rebindGroupMsgDeal(WxChatItems chatItems) {
        Long startMid = chatItems.getLastmsgid();
        if (startMid == null) {
            return;
        }
        Kv params = Kv.by("uid", chatItems.getUid()).set("startMid", startMid).set("groupid", chatItems.getBizid()).set("yes", Const.YesOrNo.YES);
        SqlPara sqlPara = Db.use(Const.Db.TIO_SITE_MAIN).getSqlPara("chatmsg.rebindGroupMsgDeal", params);
        Db.use(Const.Db.TIO_SITE_MAIN).update(sqlPara);
    }

    /**
     * 获取焦点-目前只使用在私聊-群聊使用请谨慎扩展
     *
     * @param uid
     * @param chatlinkid
     * @param devicetype
     * @return
     * @author lixinji
     * 2020年2月10日 下午2:33:24
     */
    public static FocusVo isFocus(Integer uid, Long chatlinkid) {
        FocusVo app = me.getFocus(uid, Devicetype.APP.getValue(), "");
        if (app != null && Objects.equals(app.getRchatlinkid(), chatlinkid)) {
            return app;
        }
        HashMap<String, Long> pc = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.WEB.getValue());
        if (pc != null) {
            for (String key : pc.keySet()) {
                Long existChatLinkid = pc.get(key);
                if (Objects.equals(existChatLinkid, chatlinkid)) {
                    return me.getFocus(uid, Devicetype.WEB.getValue(), key);
                }
            }
        }
        HashMap<String, Long> h5 = ChatMsgService.me.getFocusDeviceMap(uid, Devicetype.H5.getValue());
        if (h5 != null) {
            for (String key : h5.keySet()) {
                Long existChatLinkid = h5.get(key);
                if (Objects.equals(existChatLinkid, chatlinkid)) {
                    return me.getFocus(uid, Devicetype.H5.getValue(), key);
                }
            }
        }
        return null;
    }

    /**
     * 备份好友历史消息
     *
     * @param dateTime
     * @author lixinji
     * 2020年7月3日 下午3:15:25
     */
    public int bakFriendMsg(DateTime dateTime) {
        DateTime dealTime = DateUtil.offsetMonth(dateTime, -3);
        Db.use(Const.Db.TIO_SITE_MAIN).update("INSERT INTO wx_friend_msg_bak select *, now() baktime FROM wx_friend_msg where createtime <= ?", DateUtil.format(dealTime, DatePattern.NORM_DATETIME_PATTERN));
        return Db.use(Const.Db.TIO_SITE_MAIN).update("delete FROM wx_friend_msg where createtime <= ?", DateUtil.format(dealTime, DatePattern.NORM_DATETIME_PATTERN));
    }

    /**
     * 群消息统计-暂时未使用
     * TODO:lixinji-可以每天统计
     *
     * @param groupid
     * @return
     * @author lixinji
     * 2020年7月8日 下午4:09:08
     */
    public void groupMetaStat(Long groupid, Long msgid, boolean lock) {
        if (lock) {
            Const.getBsExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ReentrantReadWriteLock rwLock = LockUtils.getReentrantReadWriteLock(LockPrefix.CHAT_GROUP_INFO_KEY_PREFIX + "." + groupid, WxGroup.class);
                    WriteLock writeLock = rwLock.writeLock();
                    writeLock.lock();
                    try {
                        Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_group_meta set msgcount = msgcount + 1,lastmsgid = ? where groupid = ?", msgid, groupid);
                    } catch (Exception e) {
                        log.error("", e);
                    } finally {
                        writeLock.unlock();
                    }
                }
            });
        } else {
            Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_group_meta set msgcount = msgcount + 1,lastmsgid = ? where groupid = ?", msgid, groupid);
        }

    }

    /**
     * 好友消息统计
     *
     * @param fidkey
     * @return
     * @author lixinji
     * 2020年7月14日 下午1:56:00
     */
    public void friendMsgMetaStat(String fidkey, Long msgid) {
        Const.getBsExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_friend_meta set msgcount = msgcount + 1,lastmsgid = ? where fidkey = ?", msgid, fidkey);
                } catch (Exception e) {
                    log.error("", e);
                }
            }
        });
    }

    /**
     * 删除好友消息
     *
     * @author lixinji
     * 2020年7月3日 下午3:42:15
     */
    public void delFriendMsg(String twouid) {
        Db.use(Const.Db.TIO_SITE_MAIN).update("delete from wx_friend_msg  where twouid = ?", twouid);
    }

    /**
     * 备份群历史消息
     *
     * @param dateTime
     * @return
     * @author lixinji
     * 2020年7月3日 下午3:55:18
     */
    public int bakGroupMsg(DateTime dateTime) {
        DateTime dealTime = DateUtil.offsetMonth(dateTime, -3);
        Db.use(Const.Db.TIO_SITE_MAIN).update("INSERT INTO wx_group_msg_bak select *, now() baktime FROM wx_group_msg where createtime <= ?", DateUtil.format(dealTime, DatePattern.NORM_DATETIME_PATTERN));
        return Db.use(Const.Db.TIO_SITE_MAIN).update("delete FROM wx_group_msg where createtime <= ?", DateUtil.format(dealTime, DatePattern.NORM_DATETIME_PATTERN));
    }

    /**
     * 删除无效群消息
     * 群解散，所有会话都删除
     *
     * @return
     * @author lixinji
     * 2020年7月3日 下午4:10:19
     */
    public int delGroupInvalidMsg() {
        Db.use(Const.Db.TIO_SITE_MAIN).update("INSERT INTO wx_group_msg_bak select *, now() baktime from wx_group_msg where groupid not in (select groupid from wx_chat_group_item)");
        return Db.use(Const.Db.TIO_SITE_MAIN).update("delete from wx_group_msg where groupid not in (select groupid from wx_chat_group_item)");
    }

    /**
     * 修改删除的群的消息未无效消息
     *
     * @return
     * @author lixinji
     * 2020年7月14日 下午2:09:30
     */
    public int updateGroupInvalidMsg() {
        return Db.use(Const.Db.TIO_SITE_MAIN).update("update wx_group_msg set `status` = ? where groupid not in (select id from wx_group)", Const.Status.DISABLED);
    }

    /**
     * 备份无效的群消息
     *
     * @return
     * @author lixinji
     * 2020年7月14日 下午2:10:11
     */
    public int bakGroupInvalidMsg() {
        return Db.use(Const.Db.TIO_SITE_MAIN).update(
                "INSERT INTO wx_group_msg_bak SELECT *, now() baktime FROM wx_group_msg WHERE groupid not in (select id from wx_group)  and `status` = ?", Const.Status.NORMAL);
    }



}
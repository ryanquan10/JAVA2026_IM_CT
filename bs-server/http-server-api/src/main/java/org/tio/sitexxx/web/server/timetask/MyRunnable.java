package org.tio.sitexxx.web.server.timetask;

import org.tio.http.common.HttpRequest;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.sitexxx.im.server.handler.wx.WxChatApi;
import org.tio.sitexxx.service.cache.CacheConfig;
import org.tio.sitexxx.service.cache.Caches;
import org.tio.sitexxx.service.model.main.*;
import org.tio.sitexxx.service.model.stat.GroupStat;
import org.tio.sitexxx.service.service.chat.GroupService;
import org.tio.sitexxx.service.utils.PeriodUtils;
import org.tio.sitexxx.service.utils.RetUtils;
import org.tio.sitexxx.service.vo.Const;
import org.tio.sitexxx.web.server.controller.base.UserController;
import org.tio.sitexxx.web.server.init.WebApiInit;
import org.tio.sitexxx.web.server.utils.WebUtils;
import org.tio.utils.cache.CacheUtils;
import org.tio.utils.cache.FirsthandCreater;
import org.tio.utils.cache.ICache;
import org.tio.utils.resp.Resp;

import java.io.Serializable;
import java.util.*;

public class MyRunnable implements Runnable {
    private final static GroupService groupService = GroupService.me;

    @Override
    public void run() {
        // Your task code goes here
        try {

        ICache cache = Caches.getCache(CacheConfig.TEMP_IM);
        List<TempUserKey> tempUserKeys = TempUserKey.dao.findAll();
            for (TempUserKey tempUserKey : tempUserKeys) {
                Serializable serializable = cache.get(tempUserKey.getKey());

                if (serializable == null) {
                    HttpRequest createHttpRequest = WebApiInit.TEMP_USER_REQUEST.get("create_"+tempUserKey.getKey());
                    HttpRequest memberHttpRequest = WebApiInit.TEMP_USER_REQUEST.get(tempUserKey.getKey());
                    if (createHttpRequest == null) {
                        TempUserKey.dao.deleteById(tempUserKey.getId());
                        return;
                    }
                    WxGroup group = WxGroup.dao.findById(tempUserKey.getGroupId());
                    User createUser = User.dao.findById(tempUserKey.getCreateUid());
                    Ret ret = null;
                    ret = groupService.delGroup(createUser, tempUserKey.getGroupId());

                    //消息处理
                    Ret finalRet = ret;
                    Const.getBsExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                WxChatGroupItem groupItem = RetUtils.getOkTData(finalRet, "groupitem");
                                WxGroup group = RetUtils.getOkTData(finalRet, "group");
                                WxGroupUser groupUser = RetUtils.getOkTData(finalRet, "groupuser");
                                WxChatApi.delGroup(createHttpRequest, createUser, groupItem, group, groupUser);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });
                    //统计处理
                    Const.getBsExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                WxGroup group = RetUtils.getOkTData(finalRet, "group");
                                String dayperiod = PeriodUtils.dateToPeriodByType(group.getCreatetime(), Const.PeriodType.DAY);
                                GroupStat groupStat = GroupStat.dao.findFirst("select * from group_stat where dayperiod = ? and type = ?", dayperiod, Const.Status.DELETE);
                                if (groupStat == null) {
                                    groupStat = new GroupStat();
                                    groupStat.setAddcount(1);
                                    groupStat.setDayperiod(dayperiod);
                                    groupStat.setType(Const.Status.DELETE);
                                    groupStat.ignoreSave();
                                } else {
                                    Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount + 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.DELETE);
                                }
                                Db.use(Const.Db.TIO_SITE_STAT).update("update group_stat set addcount = addcount - 1 where dayperiod = ? and type = ?", dayperiod, Const.Status.NORMAL);
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });


                    TempUserKey.dao.deleteById(tempUserKey.getId());
                    WebApiInit.TEMP_USER_REQUEST.remove(tempUserKey.getKey());
                    WebApiInit.TEMP_USER_REQUEST.remove("create_" + tempUserKey.getKey());
                    if (WxChatApi.isOnline(tempUserKey.getMemberUid())) {
                        UserController.tempUserLogout(memberHttpRequest);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}

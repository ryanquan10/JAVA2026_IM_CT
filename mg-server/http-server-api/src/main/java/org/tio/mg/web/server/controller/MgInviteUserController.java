package org.tio.mg.web.server.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.tio.http.server.annotation.RequestPath;
import org.tio.jfinal.kit.Kv;
import org.tio.jfinal.kit.Ret;
import org.tio.jfinal.plugin.activerecord.Db;
import org.tio.jfinal.plugin.activerecord.Page;
import org.tio.jfinal.plugin.activerecord.Record;
import org.tio.jfinal.plugin.activerecord.SqlPara;
import org.tio.mg.service.model.mg.MgInviteOrg;
import org.tio.mg.service.model.mg.MgInviteUser;
import org.tio.mg.service.model.mg.MgUser;
import org.tio.mg.service.service.conf.MgInviteUserService;
import org.tio.mg.service.utils.RetUtils;
import org.tio.mg.service.vo.MgConst;
import org.tio.utils.resp.Resp;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户组织管理
 */
@RequestPath(value = "/mginviteuser")
public class MgInviteUserController {

    MgInviteUserService mgInviteUserService = MgInviteUserService.me;

    final static MgInviteOrg inviteOrg = new MgInviteOrg().dao();
    final static MgInviteUser inviteUser = new MgInviteUser().dao();

    /**
     * 添加组织
     */
    @RequestPath(value = "/addMgInviteOrg")
    public Resp add(MgInviteOrg mgInviteOrg) throws Exception {
        Ret ret = mgInviteUserService.addMgInviteOrg(mgInviteOrg);
        if (ret.isFail()) {
            return Resp.fail(RetUtils.getRetMsg(ret));
        }
        return Resp.ok(RetUtils.getOkData(ret));
    }


    /**
     * 获取组织翻页
     */
    @RequestPath(value = "/listMgInviteOrg")
    public Resp getRealNameCertificationList(String searchkey, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }
        Kv params = Kv.create();
        if(StrUtil.isNotBlank(searchkey)) {
            params.set("searchkey","%" + searchkey + "%");
        }
        SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("sys.mgnnviteorguser", params);
        Page<Record> paginate = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        List<Record> list = paginate.getList();
        if (CollectionUtil.isNotEmpty(list)) {
            List<Integer> userIds = list.stream()
                    .map(record -> record.getInt("mguid"))
                    .filter(Objects::nonNull)
                    .distinct() // 去重
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                String userIdListStr = userIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                String sql = "SELECT id, nick  FROM mg_user WHERE id IN (" + userIdListStr + ")";
                List<Record> userRecords = Db.use(MgConst.Db.TIO_MG).find(sql);

                Map<Integer, String> userNickMap = userRecords.stream()
                        .collect(Collectors.toMap(
                                record -> record.getInt("id"),
                                record -> record.getStr("nick")
                        ));

                for (Record record : list) {
                    Integer userId = record.getInt("mguid");
                    String nickName = userNickMap.getOrDefault(userId, "未知用户"); // 默认值为 "未知用户"
                    record.set("unick", nickName);
                }
            }
        }

        return Resp.ok(paginate);
    }

    /**
     * 获取所有后台用户
     */
    @RequestPath(value = "/getAllAdminUser")
    public Resp getAllAdminUser() {
        MgUser baseInfo = new MgUser();
        List<MgUser> all = baseInfo.findAll();
        return Resp.ok(all);
    }

    /**
     * 删除后台组织用户
     */
    @RequestPath(value = "/delMgInviteOrg")
    public Resp delMgInviteOrg(Integer id) {
        MgInviteOrg mgInviteOrg = new MgInviteOrg();
        boolean b = mgInviteOrg.deleteById(id);
        return b ? Resp.ok() : Resp.fail();
    }

    /**
     * 查询组织成员
     */
    @RequestPath(value = "/listInviteUser")
    public Resp listInviteUser(Integer uid, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize == null) {
            pageSize = 16;
        }

        Kv params = Kv.create();
        if (uid != null) {
            params.set("mguid", uid);
        }

        // 3. 查询分页数据
        SqlPara sqlPara = Db.use(MgConst.Db.TIO_SITE_MAIN).getSqlPara("sys.listinviteuser", params);
        Page<Record> paginate = Db.use(MgConst.Db.TIO_SITE_MAIN).paginate(pageNumber, pageSize, sqlPara);
        List<Record> list = paginate.getList();

        if (CollectionUtil.isNotEmpty(list)) {
            List<Integer> userIds = list.stream()
                    .map(record -> record.getInt("mguid"))
                    .filter(Objects::nonNull)
                    .distinct() // 去重
                    .collect(Collectors.toList());

            if (!userIds.isEmpty()) {
                String userIdListStr = userIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                String sql = "SELECT id, nick  FROM mg_user WHERE id IN (" + userIdListStr + ")";
                List<Record> userRecords = Db.use(MgConst.Db.TIO_MG).find(sql);

                Map<Integer, String> userNickMap = userRecords.stream()
                        .collect(Collectors.toMap(
                                record -> record.getInt("id"),
                                record -> record.getStr("nick")
                        ));

                for (Record record : list) {
                    Integer userId = record.getInt("mguid");
                    String nickName = userNickMap.getOrDefault(userId, "未知用户");
                    record.set("mnick", nickName);
                }
            }
        }

        return Resp.ok(paginate);
    }

}

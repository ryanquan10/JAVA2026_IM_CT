package org.tio.mg.service.service.conf;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.jfinal.kit.Ret;
import org.tio.mg.service.model.mg.MgInviteOrg;
import org.tio.mg.service.model.mg.MgInviteUser;
import org.tio.mg.service.utils.RetUtils;

import java.util.Date;
import java.util.List;

public class MgInviteUserService {

    private static Logger log = LoggerFactory.getLogger(MgInviteUserService.class);

    public static final MgInviteUserService me = new MgInviteUserService();

    final static MgInviteOrg inviteOrg = new MgInviteOrg().dao();
    final static MgInviteUser inviteUser = new MgInviteUser().dao();

    /**
     * 新增组织
     */
    public Ret addMgInviteOrg(MgInviteOrg mgInviteOrg) {

        String inviteCode = mgInviteOrg.getInvitecode();
        Integer mguid = mgInviteOrg.getMguid();

        if (StrUtil.isEmpty(inviteCode)) {
            return RetUtils.failMsg("请填写邀请码");
        }
        if (mguid == null) {
            return RetUtils.failMsg("请选择用户id");
        }

        MgInviteOrg first = inviteOrg.findFirst("select id, name, invitecode, mguid from mg_invite_org where invitecode = '" + inviteCode + "'");
        if (first != null) {
            return RetUtils.failMsg("邀请码已被使用");
        }
        mgInviteOrg.setCreatetime(new Date());
        mgInviteOrg.save();
        return RetUtils.okOper();
    }

}

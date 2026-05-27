package org.tio.mg.service.model.mg.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.mg.service.jf.TioModel;

public abstract class BaseMgInviteOrg<M extends BaseMgInviteOrg<M>> extends TioModel<M> implements IBean  {

    public void setId(java.lang.Integer id) {
        set("id", id);
    }

    public java.lang.Integer getId() {
        return getInt("id");
    }

    /**
     * 名称
     */
    public void setName(java.lang.String name) {
        set("name", name);
    }

    /**
     * 名称
     */
    public java.lang.String getName() {
        return getStr("name");
    }


    /**
     * 邀请码
     */
    public void setInvitecode(java.lang.String invitecode) {
        set("invitecode", invitecode);
    }

    /**
     * 邀请码
     */
    public java.lang.String getInvitecode() {
        return getStr("invitecode");
    }


    /**
     * 用户id
     */
    public void setMguid(java.lang.Integer mguid) {
        set("mguid", mguid);
    }

    /**
     * 用户id
     */
    public java.lang.Integer getMguid() {
        return getInt("mguid");
    }

    /**
     * 创建时间
     */
    public void setCreatetime(java.util.Date createtime) {
        set("createtime", createtime);
    }

    /**
     * 创建时间
     */
    public java.util.Date getCreatetime() {
        return getDate("createtime");
    }



}

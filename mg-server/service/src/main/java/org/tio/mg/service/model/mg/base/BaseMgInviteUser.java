package org.tio.mg.service.model.mg.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.mg.service.jf.TioModel;

public abstract class BaseMgInviteUser<M extends BaseMgInviteUser<M>> extends TioModel<M> implements IBean  {

    public void setId(Integer id) {
        set("id", id);
    }

    public Integer getId() {
        return getInt("id");
    }

    /**
     * 组织id
     */
    public void setInviteOrgId(Integer mguid) {
        set("inviteorgid", mguid);
    }

    /**
     * 组织id
     */
    public Integer getInviteOrgId() {
        return getInt("inviteorgid");
    }

    /**
     * 用户id
     */
    public void setMguid(Integer mguid) {
        set("uid", mguid);
    }

    /**
     * 用户id
     */
    public Integer getMguid() {
        return getInt("uid");
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

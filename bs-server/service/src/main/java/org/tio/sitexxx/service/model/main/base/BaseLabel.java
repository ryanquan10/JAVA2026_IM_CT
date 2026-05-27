package org.tio.sitexxx.service.model.main.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.sitexxx.service.jf.TioModel;

public abstract class BaseLabel<M extends BaseLabel<M>> extends TioModel<M> implements IBean {

    public void setId(Integer id) {
        set("id", id);
    }

    public Integer getId() {
        return getInt("id");
    }

    public void setUid(Integer uid) {
        set("uid", uid);
    }

    public Integer getUid() {
        return getInt("uid");
    }

    public void setLabelname(String labelname) {
        set("labelname", labelname);
    }

    public String getLabelname() {
        return getStr("labelname");
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

package org.tio.sitexxx.service.model.main.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.sitexxx.service.jf.TioModel;

public abstract class BaseDefaultGroup<M extends BaseDefaultGroup<M>> extends TioModel<M> implements IBean {
    public void setGroupid(Long groupid) {
        set("groupid", groupid);
    }

    public Long getGroupid() {
        return getLong("groupid");
    }

    /**
     * 是否打开
     */
    public void setIsopen(Integer isopen) {
        set("isopen", isopen);
    }

    /**
     * 是否打开
     */
    public Integer getIsopen() {
        return getInt("isopen");
    }

}

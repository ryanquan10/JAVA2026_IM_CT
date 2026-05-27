package org.tio.mg.service.model.mg.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.mg.service.jf.TioModel;

public abstract class BaseDefaultFriends<M extends BaseDefaultFriends<M>> extends TioModel<M> implements IBean {
    public void setId(Integer id) {
        set("id", id);
    }

    public Integer getId() {
        return getInt("id");
    }

    /**
     * 客服id
     */
    public void setUid(String uid) {
        set("uid", uid);
    }

    /**
     * 客服id
     */
    public String getUid() {
        return getStr("uid");
    }

    /**
     * 默认消息
     */
    public void setDefaultMsg(String defaultMsg) {
        set("defaultMsg", defaultMsg);
    }

    /**
     * 默认消息
     */
    public String getDefaultMsg() {
        return getStr("defaultMsg");
    }

    /**
     * 客服权值
     */
    public void setWeight(Integer weight) {
        set("weight", weight);
    }

    /**
     * 客服权值
     */
    public Integer getWeight() {
        return getInt("weight");
    }

}

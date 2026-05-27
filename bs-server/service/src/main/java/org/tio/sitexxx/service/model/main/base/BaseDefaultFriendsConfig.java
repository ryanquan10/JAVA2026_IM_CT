package org.tio.sitexxx.service.model.main.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.sitexxx.service.jf.TioModel;

public abstract class BaseDefaultFriendsConfig<M extends BaseDefaultFriendsConfig<M>> extends TioModel<M> implements IBean {
    public void setId(Integer id) {
        set("id", id);
    }

    public Integer getId() {
        return getInt("id");
    }

    /**
     * 开启轮询
     */
    public void setIsRotation(Integer isRotation) {
        set("isRotation", isRotation);
    }

    /**
     * 开启轮询
     */
    public Integer getIsRotation() {
        return getInt("isRotation");
    }

    /**
     * 轮询次数
     */
    public void setPoint(Integer point) {
        set("point", point);
    }

    /**
     * 轮询次数
     */
    public Integer getPoint() {
        return getInt("point");
    }



}

package org.tio.mg.service.model.mg.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.mg.service.jf.TioModel;

public abstract class BaseMgDiscoveryPage<M extends BaseMgDiscoveryPage<M>> extends TioModel<M> implements IBean {
    public void setId(java.lang.Integer id) {
        set("id", id);
    }

    public java.lang.Integer getId() {
        return getInt("id");
    }

    /**
     * 自定义网站名称
     */
    public void setName(java.lang.String name) {
        set("name", name);
    }

    /**
     * 自定义网站名称
     */
    public java.lang.String getName() {
        return getStr("name");
    }

    /**
     * 自定义网站url
     */
    public void setUrl(java.lang.String url) {
        set("url", url);
    }

    /**
     * 自定义网站url
     */
    public java.lang.String getUrl() {
        return getStr("url");
    }

    /**
     * 自定义网站logo
     */
    public void setLogo(java.lang.String logo) {
        set("logo", logo);
    }

    /**
     * 自定义网站logo
     */
    public java.lang.String getLogo() {
        return getStr("logo");
    }

}

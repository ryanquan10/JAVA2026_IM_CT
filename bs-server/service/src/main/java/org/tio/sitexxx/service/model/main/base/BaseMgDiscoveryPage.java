package org.tio.sitexxx.service.model.main.base;

import org.tio.jfinal.plugin.activerecord.IBean;
import org.tio.sitexxx.service.jf.TioModel;


public abstract class BaseMgDiscoveryPage<M extends BaseMgDiscoveryPage<M>> extends TioModel<M> implements IBean {
    public void setId(Integer id) {
        set("id", id);
    }

    public Integer getId() {
        return getInt("id");
    }

    /**
     * 自定义网站名称
     */
    public void setName(String name) {
        set("name", name);
    }

    /**
     * 自定义网站名称
     */
    public String getName() {
        return getStr("name");
    }

    /**
     * 自定义网站url
     */
    public void setUrl(String url) {
        set("url", url);
    }

    /**
     * 自定义网站url
     */
    public String getUrl() {
        return getStr("url");
    }

    /**
     * 自定义网站logo
     */
    public void setLogo(String logo) {
        set("logo", logo);
    }

    /**
     * 自定义网站logo
     */
    public String getLogo() {
        return getStr("logo");
    }

}

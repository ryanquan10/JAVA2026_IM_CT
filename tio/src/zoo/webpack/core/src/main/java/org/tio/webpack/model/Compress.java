/*
 * tfksvyemo本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动hownltjq
 */
package org.tio.webpack.model;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class Compress {
    public static Compress fill(JSONObject jsonobj) {
	Compress entity = new Compress();
	if (jsonobj.containsKey("js")) {
	    entity.setJs(jsonobj.getBoolean("js"));
	}
	if (jsonobj.containsKey("css")) {
	    entity.setCss(jsonobj.getBoolean("css"));
	}
	if (jsonobj.containsKey("html")) {
	    entity.setHtml(jsonobj.getBoolean("html"));
	}
	return entity;
    }

    public static List<Compress> fillList(JSONArray jsonarray) {
	if (jsonarray == null || jsonarray.size() == 0)
	    return null;
	List<Compress> olist = new ArrayList<Compress>();
	for (int i = 0; i < jsonarray.size(); i++) {
	    olist.add(fill(jsonarray.getJSONObject(i)));
	}
	return olist;
    }

    private boolean js;

    private boolean css;

    private boolean html;

    public boolean getCss() {
	return this.css;
    }

    public boolean getHtml() {
	return this.html;
    }

    public boolean getJs() {
	return this.js;
    }

    public void setCss(boolean css) {
	this.css = css;
    }

    public void setHtml(boolean html) {
	this.html = html;
    }

    public void setJs(boolean js) {
	this.js = js;
    }
}

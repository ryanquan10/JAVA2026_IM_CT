
/**
 *
 */
package org.tio.mg.im.common.bs.wx.webrtc.base;

import java.io.Serializable;

import org.tio.mg.service.model.main.WxCallItem;

/**
 * @author tanyaowu
 */
public class WxCallBase extends WxCallItem implements Serializable {

	private static final long serialVersionUID = 346711195550637522L;

	/**
	 * 透传填充
	 * @param wxCallItem
	 * @author tanyaowu
	 */
	public void fill(WxCallItem wxCallItem) {
		this._setAttrs(wxCallItem);
	}

	//	public static WxCallItem newIns(WxCallBase wxCallBase) {
	//		WxCallItem wxCallItem = new WxCallItem();
	//		wxCallItem._setAttrs(wxCallBase);
	//		return wxCallItem;
	//	}
	

	/**
	 * 
	 * @param wxCallBase
	 * @return
	 * @author tanyaowu
	 */
	public static boolean save(WxCallBase wxCallBase) {
		WxCallItem wxCallItem = new WxCallItem();
		wxCallItem._setAttrs(wxCallBase);
		boolean f = wxCallItem.save();
		if (f) {
			wxCallBase.setId(wxCallItem.getId());
		}
		return f;
	}


}

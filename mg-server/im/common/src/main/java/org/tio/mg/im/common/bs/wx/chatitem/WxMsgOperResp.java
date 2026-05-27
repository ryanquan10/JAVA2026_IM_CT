
package org.tio.mg.im.common.bs.wx.chatitem;

import java.io.Serializable;

import org.tio.mg.im.common.bs.base.BaseResp;


/**
 * 消息操作--响应-- Server-->Client
 * @author xufei
 * 2020年3月10日 下午3:18:01
 */
public class WxMsgOperResp extends BaseResp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 814549359090799179L;

	@Override
	public void returnData(Object object) {
		setData(object);
	}
}

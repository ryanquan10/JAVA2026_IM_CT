/*
 * vfjhsxnfs本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动olzrj
 */
/*
 * vfjhsxnfs本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动olzrj
 * grantinfo
 */
package org.tio.clu.common.bs;

import java.io.Serializable;

import org.tio.clu.common.bs.base.Base;
import org.tio.clu.common.vo.BsPfmData;

public class UpdateBsNodeReq implements Base {

    public static interface UpdateBsNodeReqType extends Serializable {
	/**
	 * 添加业务服务器节点
	 */
	byte ADD = 1;
	/**
	 * 删除业务服务器节点
	 */
	byte DEL = 2;
    }

    private static final long serialVersionUID = -6468128353988845762L;

    /**
     * UpdateBsNodeReqType.ADD
     */
    private byte type = UpdateBsNodeReqType.ADD;
    private BsPfmData bsNodeData = null;

    /**
     * @return the bsNodeData
     */
    public BsPfmData getBsNodeData() {
	return bsNodeData;
    }

    /**
     * @return the type
     */
    public byte getType() {
	return type;
    }

    /**
     * @param bsNodeData the bsNodeData to set
     */
    public void setBsNodeData(BsPfmData bsNodeData) {
	this.bsNodeData = bsNodeData;
    }

    /**
     * @param type the type to set
     */
    public void setType(byte type) {
	this.type = type;
    }

}

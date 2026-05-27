/*
 * nsiqlsz本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ivlywlyerpcq
 */
/*
 * nsiqlsz本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动ivlywlyerpcq
 * grantinfo
 */
package org.tio.clu.common.bs;

import org.tio.clu.common.bs.base.TransferBase;

public class TransferNtf extends TransferBase {
    private static final long serialVersionUID = 3738262557698086393L;

    public static TransferNtf fromReq(TransferReq transferReq) {
	return transferReq;
	// TransferNtf transferNtf = new TransferNtf();
	// transferNtf.setP(transferReq.getP());
	// transferNtf.setV(transferReq.getV());
	// transferNtf.setVs(transferReq.getVs());
	// transferNtf.setBt(transferReq.getBt());
	// return transferNtf;
    }
}

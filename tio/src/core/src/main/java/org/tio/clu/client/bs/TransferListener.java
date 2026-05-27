/*
 * lerncucaqe本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动pzliu
 */
package org.tio.clu.client.bs;

import org.tio.clu.common.BindType;
import org.tio.clu.common.bs.TransferNtf;
import org.tio.core.intf.Packet;

/**
 * @author talent
 *
 */
public interface TransferListener {

    /**
     * 
     * @param bsPacket
     * @param bindType
     * @param transferNtf
     * @author tanyaowu
     */
    void onAfterTransfer(Packet bsPacket, BindType bindType, TransferNtf transferNtf);

    /**
     * 
     * @param bsPacket
     * @param bindType
     * @param transferNtf
     * @return
     * @author tanyaowu
     */
    boolean onBeforeTransfer(Packet bsPacket, BindType bindType, TransferNtf transferNtf);

}

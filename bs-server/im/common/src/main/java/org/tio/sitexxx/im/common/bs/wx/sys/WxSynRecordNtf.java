
/**
 *
 */
package org.tio.sitexxx.im.common.bs.wx.sys;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.tio.sitexxx.service.vo.wx.SynRecordVo;

/**
 * 记录同步通知-- Server-->Client
 * 
 * @author lixinji
 * 2020年9月15日 下午5:54:33
 */
public class WxSynRecordNtf implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 723282137981331923L;

	/**
	 * 记录同步通知
	 */
	private List<SynRecordVo> syndata;

	/**
	 * @param syndata
	 */
	public WxSynRecordNtf(List<SynRecordVo> syndata) {
		this.syndata = syndata;
	}

	/**
	 * @param synRecordVo
	 */
	public WxSynRecordNtf(SynRecordVo synRecordVo) {
		this.syndata = new ArrayList<SynRecordVo>();
		syndata.add(synRecordVo);
	}

	/**
	 * 消息发送时间戳
	 */
	private Long t = System.currentTimeMillis();

	public List<SynRecordVo> getSyndata() {
		return syndata;
	}

	public void setSyndata(List<SynRecordVo> syndata) {
		this.syndata = syndata;
	}

	public Long getT() {
		return t;
	}

}

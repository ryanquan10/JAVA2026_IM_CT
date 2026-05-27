/*
 * rxgdsnttgfbyaa本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zzhufekoe
 */
package org.tio.clu.common.vo;

import java.util.Map;

import org.tio.core.Node;

/**
 * @author tanyaowu 2020年8月27日 上午11:30:37
 */
public class BsPfmData implements Comparable<BsPfmData>, java.io.Serializable {
    private static final long serialVersionUID = 5543342382938209322L;

    private String cgid = null;

    private Map<String, Node> nodeMap = null;
    private Long createTime = null;
    private PfmData pfm = new PfmData();
    /**
     * @param node
     * @author tanyaowu
     */
    public BsPfmData(Map<String, Node> nodeMap) {
	super();
	this.setNodeMap(nodeMap);
    }

    /**
     * @param other
     * @return
     * @author tanyaowu
     */
    @Override
    public int compareTo(BsPfmData other) {
	PfmData otherpfm = other.getPfm();
	PfmData curpfm = this.getPfm();

	int cur_to_first = -1;
	int cur_to_last = 1;
	if (otherpfm.getTcpCount() > curpfm.getTcpCount()) {
	    return cur_to_first;
	}
	if (otherpfm.getTcpCount() < curpfm.getTcpCount()) {
	    return cur_to_last;
	}

	if (createTime < other.getCreateTime()) {
	    return cur_to_first;
	}
	if (createTime > other.getCreateTime()) {
	    return cur_to_last;
	}

	return cgid.compareTo(other.getCgid());
    }

    /**
     * @param obj
     * @return
     * @author tanyaowu
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}

	BsPfmData other = (BsPfmData) obj;
	if (cgid == null) {
	    if (other.cgid != null) {
		return false;
	    }
	} else if (!cgid.equals(other.cgid)) {
	    return false;
	}
	return true;
    }

    /**
     * @return the cgid
     */
    public String getCgid() {
	return cgid;
    }

    /**
     * @return the createTime
     */
    public Long getCreateTime() {
	return createTime;
    }

    public Map<String, Node> getNodeMap() {
	return nodeMap;
    }

    /**
     * @return the pfm
     */
    public PfmData getPfm() {
	return pfm;
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((cgid == null) ? 0 : cgid.hashCode());
	return result;
    }

    /**
     * @param cgid the cgid to set
     */
    public void setCgid(String cgid) {
	this.cgid = cgid;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Long createTime) {
	this.createTime = createTime;
    }

    public void setNodeMap(Map<String, Node> nodeMap) {
	this.nodeMap = nodeMap;
    }

    /**
     * @param pfm the pfm to set
     */
    public void setPfm(PfmData pfm) {
	this.pfm = pfm;
    }

}

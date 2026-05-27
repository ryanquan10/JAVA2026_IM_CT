/*
 * lmutzuowfjl本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zbwhq
 */
/*
 * lmutzuowfjl本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动zbwhq
 * grantinfo
 */

package org.tio.clu.common.bs;

import org.tio.clu.common.bs.base.BaseResp;
import org.tio.core.Node;

public class BestNodeResp extends BaseResp {

    private static final long serialVersionUID = -2057601076448971658L;

    private Node node;

    // private Map<String, Node> nodeMap;
    //
    // public Map<String, Node> getNodeMap() {
    // return nodeMap;
    // }
    //
    // public void setNodeMap(Map<String, Node> nodeMap) {
    // this.nodeMap = nodeMap;
    // }

    public Node getNode() {
	return node;
    }

    public void setNode(Node node) {
	this.node = node;
    }

}

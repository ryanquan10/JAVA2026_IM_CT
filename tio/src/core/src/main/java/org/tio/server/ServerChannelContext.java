/*
 * mbaqoej本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动xkiyearsmnwo
 */
package org.tio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

import org.tio.core.ChannelContext;
import org.tio.core.Node;
import org.tio.core.TioConfig;

/**
 *
 * @author tanyaowu
 *
 */
public class ServerChannelContext extends ChannelContext {
    private static final long serialVersionUID = -8253503361755284315L;

    /**
     * 创建一个虚拟ChannelContext，主要用来模拟一些操作，真实场景中用得少
     * 
     * @param tioConfig
     */
    public ServerChannelContext(TioConfig tioConfig) {
	super(tioConfig);
    }

    /**
     * @param tioConfig
     * @param asynchronousSocketChannel
     *
     * @author tanyaowu 2016年12月6日 下午12:17:59
     *
     */
    public ServerChannelContext(TioConfig tioConfig, AsynchronousSocketChannel asynchronousSocketChannel) {
	super(tioConfig, asynchronousSocketChannel);
    }

    /**
     * 创建一个虚拟ChannelContext，主要用来模拟一些操作，譬如压力测试，真实场景中用得少
     * 
     * @param tioConfig
     * @param id        ChannelContext id
     * @author tanyaowu
     */
    public ServerChannelContext(TioConfig tioConfig, String id) {
	super(tioConfig, id);
    }

    /**
     * @see org.tio.core.ChannelContext#createClientNode(java.nio.channels.AsynchronousSocketChannel)
     *
     * @param asynchronousSocketChannel
     * @return
     * @throws IOException
     * @author tanyaowu 2016年12月6日 下午12:18:08
     *
     */
    @Override
    public Node createClientNode(AsynchronousSocketChannel asynchronousSocketChannel) throws IOException {
	Node clientNode = null;
	InetSocketAddress inetSocketAddress = null;
	if (asynchronousSocketChannel == null) {
		clientNode = createUnknowNode();
	} else {
		inetSocketAddress = (InetSocketAddress) asynchronousSocketChannel.getRemoteAddress();
	}

	if (inetSocketAddress == null) {
		clientNode = createUnknowNode();
	}

	if (clientNode == null) {
		clientNode = new Node(inetSocketAddress.getHostString(), inetSocketAddress.getPort());
	}
	return clientNode;
	}

    @Override
    public TioServerConfig getTioConfig() {
	return (TioServerConfig) tioConfig;
    }

    /**
     * @return
     * @author tanyaowu
     */
    @Override
    public boolean isServer() {
	return true;
    }

}

/*
 * tnpwwhpipbfxjl本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动aeelhiqezqic
 */
package org.tio.core;

import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.client.TioClientConfig;
import org.tio.core.intf.TioHandler;
import org.tio.core.intf.TioListener;
import org.tio.core.intf.GroupListener;
import org.tio.core.intf.Packet;
import org.tio.core.intf.TioUuid;
import org.tio.core.maintain.BsIds;
import org.tio.core.maintain.ClientNodes;
import org.tio.core.maintain.Groups;
import org.tio.core.maintain.Ids;
import org.tio.core.maintain.IpBlacklist;
import org.tio.core.maintain.IpStats;
import org.tio.core.maintain.Ips;
import org.tio.core.maintain.Tokens;
import org.tio.core.maintain.Users;
import org.tio.core.ssl.SslConfig;
import org.tio.core.stat.DefaultIpStatListener;
import org.tio.core.stat.GroupStat;
import org.tio.core.stat.IpStatListener;
import org.tio.core.task.CloseRunnable;
import org.tio.server.TioServerConfig;
import org.tio.utils.SystemTimer;
import org.tio.utils.Threads;
import org.tio.utils.hutool.CollUtil;
import org.tio.utils.lock.MapWithLock;
import org.tio.utils.lock.SetWithLock;
import org.tio.utils.prop.MapWithLockPropSupport;
import org.tio.utils.thread.pool.SynThreadPoolExecutor;

/**
 * 
 * @author tanyaowu 2016年10月10日 下午5:25:43
 */
public abstract class TioConfig extends MapWithLockPropSupport {
    private static final long serialVersionUID = -2742323940321079030L;
    static Logger log = LoggerFactory.getLogger(TioConfig.class);
    public static final String CLU_KEY = "abcdefgh";
    /**
     * 默认的接收数据的buffer size
     */
    public static final int READ_BUFFER_SIZE = Integer.getInteger("tio.default.read.buffer.size", 20480);
    private final static AtomicInteger ID_ATOMIC = new AtomicInteger();
    /**
     * 本jvm中所有的TioServerConfig对象
     */
    public static final Set<TioServerConfig> ALL_SERVER_GROUPCONTEXTS = new HashSet<>();
    /**
     * 本jvm中所有的TioClientConfig对象
     */
    public static final Set<TioClientConfig> ALL_CLIENT_GROUPCONTEXTS = new HashSet<>();
    /**
     * 本jvm中所有的TioConfig对象
     */
    public static final Set<TioConfig> ALL_GROUPCONTEXTS = new HashSet<>();
    private ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
    public boolean isShortConnection = false;
    public SslConfig sslConfig = null;
    public boolean debug = false;
    public GroupStat groupStat = null;
    public boolean statOn = true;
    public PacketConverter packetConverter = null;
    /**
     * 启动时间
     */
    public long startTime = SystemTimer.currTime;
    /**
     * 是否用队列发送
     */
    public boolean useQueueSend = true;
    /**
     * 是否用队列解码（系统初始化时确定该值，中途不要变更此值，否则在切换的时候可能导致消息丢失）
     */
    public boolean useQueueDecode = false;
    /**
     * 心跳超时时间(单位: 毫秒)，如果用户不希望框架层面做心跳相关工作，请把此值设为0或负数
     */
    public long heartbeatTimeout = 1000 * 120;
    /**
     * 解码出现异常时，是否打印异常日志
     */
    public boolean logWhenDecodeError = false;
    public PacketHandlerMode packetHandlerMode = PacketHandlerMode.SINGLE_THREAD; // .queue;
    /**
     * 接收数据的buffer size
     */
    private int readBufferSize = READ_BUFFER_SIZE;
    private GroupListener groupListener = null;
    private TioUuid tioUuid = DefaultTioUuid.me;
    public SynThreadPoolExecutor tioExecutor = null;
    public CloseRunnable closeRunnable;
    public ThreadPoolExecutor groupExecutor = null;
    public ClientNodes clientNodes = new ClientNodes();
    public SetWithLock<ChannelContext> connections = new SetWithLock<ChannelContext>(new HashSet<ChannelContext>());
    public Groups groups = new Groups();
    public Users users = new Users();
    public Tokens tokens = new Tokens();
    public Ids ids = new Ids();
    public BsIds bsIds = new BsIds();
    public Ips ips = new Ips();
    public IpStats ipStats = null;
    protected String id;
    /**
     * 解码异常多少次就把ip拉黑
     */
    protected int maxDecodeErrorCountForIp = 10;
    protected String name = "未命名";
    private IpStatListener ipStatListener = DefaultIpStatListener.me;
    private boolean isStopped = false;
    /**
     * ip黑名单
     */
    public IpBlacklist ipBlacklist = null;

    public OnOfflineListener onOfflineListener;

    public final MapWithLock<Integer, Packet> synNoMap = new MapWithLock<Integer, Packet>(
	    new HashMap<Integer, Packet>());
    public final AtomicInteger SEQNO_GEN = new AtomicInteger(); // 同步序列号

    public TioConfig() {
	this(null, null);
    }

    /**
     * 
     * @param tioExecutor
     * @param groupExecutor
     * @author: tanyaowu
     */
    public TioConfig(SynThreadPoolExecutor tioExecutor, ThreadPoolExecutor groupExecutor) {
	super();
	ALL_GROUPCONTEXTS.add(this);
	if (this instanceof TioServerConfig) {
	    ALL_SERVER_GROUPCONTEXTS.add((TioServerConfig) this);
	} else {
	    ALL_CLIENT_GROUPCONTEXTS.add((TioClientConfig) this);
	}

	if (ALL_GROUPCONTEXTS.size() > 20) {
	    log.warn("已经产生{}个TioConfig对象，t-io作者怀疑你在误用t-io", ALL_GROUPCONTEXTS.size());
	}
	this.id = ID_ATOMIC.incrementAndGet() + "";

	// this.ipStats = new IpStats(this, null);

	this.tioExecutor = tioExecutor;
	if (this.tioExecutor == null) {
	    this.tioExecutor = Threads.getTioExecutor();
	}

	this.groupExecutor = groupExecutor;
	if (this.groupExecutor == null) {
	    this.groupExecutor = Threads.getGroupExecutor();
	}

	closeRunnable = new CloseRunnable(this.tioExecutor);
    }

    // /**
    // *
    // * @param cluConfig
    // * @param tioExecutor
    // * @param groupExecutor
    // * @author: tanyaowu
    // */
    // public TioConfig(CluConfig cluConfig, SynThreadPoolExecutor tioExecutor,
    // ThreadPoolExecutor groupExecutor) {
    // this(tioExecutor, groupExecutor);
    // this.setCluConfig(cluConfig);
    // }

    /**
     * 获取TioHandler对象
     * 
     * @return
     * @author: tanyaowu
     */
    public abstract TioHandler getTioHandler();

    /**
     * 获取TioListener对象
     * 
     * @return
     * @author: tanyaowu
     */
    public abstract TioListener getTioListener();

    /**
     *
     * @return
     * @author tanyaowu
     */
    public ByteOrder getByteOrder() {
	return byteOrder;
    }

    /**
     * @return the groupListener
     */
    public GroupListener getGroupListener() {
	return groupListener;
    }

    // /**
    // * 获取GroupStat对象
    // * @return
    // * @author: tanyaowu
    // */
    // public abstract GroupStat groupStat;

    public GroupStat getGroupStat() {
	return groupStat;
    }

    /**
     *
     * @return
     * @author tanyaowu
     */
    public String getId() {
	return id;
    }

    public IpStatListener getIpStatListener() {
	return ipStatListener;
    }

    /**
     * @return the isEncodeCareWithChannelContext
     */
    // public boolean isEncodeCareWithChannelContext() {
    // return isEncodeCareWithChannelContext;
    // }

    // /**
    // * @return the isShortConnection
    // */
    // public boolean isShortConnection {
    // return isShortConnection;
    // }

    public String getName() {
	return name;
    }

    public int getReadBufferSize() {
	return readBufferSize;
    }

    /**
     * @param isEncodeCareWithChannelContext the isEncodeCareWithChannelContext to
     *                                       set
     */
    // public void setEncodeCareWithChannelContext(boolean
    // isEncodeCareWithChannelContext) {
    // this.isEncodeCareWithChannelContext = isEncodeCareWithChannelContext;
    // }

    /**
     * @return the tioUuid
     */
    public TioUuid getTioUuid() {
	return tioUuid;
    }

    /**
     * 是服务器端还是客户端
     * 
     * @return
     * @author tanyaowu
     */
    public abstract boolean isServer();

    public boolean isSsl() {
	return sslConfig != null;
    }

    /**
     * @return the isStop
     */
    public boolean isStopped() {
	return isStopped;
    }

    /**
     *
     * @param byteOrder
     * @author tanyaowu
     */
    public void setByteOrder(ByteOrder byteOrder) {
	this.byteOrder = byteOrder;
    }

    /**
     * @param groupListener the groupListener to set
     */
    public void setGroupListener(GroupListener groupListener) {
	this.groupListener = groupListener;
    }

    /**
     * @param heartbeatTimeout the heartbeatTimeout to set
     */
    public void setHeartbeatTimeout(long heartbeatTimeout) {
	this.heartbeatTimeout = heartbeatTimeout;
    }

    public void setIpStatListener(IpStatListener ipStatListener) {
	this.ipStatListener = ipStatListener;
	// this.ipStats.setIpStatListener(ipStatListener);
    }

    public void setName(String name) {
	this.name = name;
    }

    /**
     * @param packetHandlerMode the packetHandlerMode to set
     */
    public void setPacketHandlerMode(PacketHandlerMode packetHandlerMode) {
	this.packetHandlerMode = packetHandlerMode;
    }

    /**
     * @param readBufferSize the readBufferSize to set
     */
    public void setReadBufferSize(int readBufferSize) {
	this.readBufferSize = Math.min(readBufferSize, TcpConst.MAX_DATA_LENGTH);
    }

    /**
     * @param isShortConnection the isShortConnection to set
     */
    public void setShortConnection(boolean isShortConnection) {
	this.isShortConnection = isShortConnection;
    }

    public void setSslConfig(SslConfig sslConfig) {
	this.sslConfig = sslConfig;
    }
	public boolean isIpStatEnable() {
		return this.ipStats != null && CollUtil.isNotEmpty(this.ipStats.durationList);
	}

    /**
     * @param isStop the isStop to set
     */
    public void setStopped(boolean isStopped) {
	this.isStopped = isStopped;
    }

    /**
     * @param tioUuid the tioUuid to set
     */
    public void setTioUuid(TioUuid tioUuid) {
	this.tioUuid = tioUuid;
    }

    /**
     * 是否用队列解码（系统初始化时确定该值，中途不要变更此值，否则在切换的时候可能导致消息丢失
     * 
     * @param useQueueDecode
     * @author tanyaowu
     */
    public void setUseQueueDecode(boolean useQueueDecode) {
	this.useQueueDecode = useQueueDecode;
    }

    /**
     * 是否用队列发送，可以随时切换
     * 
     * @param useQueueSend
     * @author tanyaowu
     */
    public void setUseQueueSend(boolean useQueueSend) {
	this.useQueueSend = useQueueSend;
    }

    public void setOnOfflineListener(OnOfflineListener onOfflineListener) {
        this.onOfflineListener = onOfflineListener;
    }
}

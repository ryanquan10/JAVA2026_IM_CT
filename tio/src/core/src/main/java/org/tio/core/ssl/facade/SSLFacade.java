/*
 * jyokjzwleqnb本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动fbmuxjm
 */
package org.tio.core.ssl.facade;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.ChannelContext;
import org.tio.core.ssl.SslVo;

public class SSLFacade implements ISSLFacade {
	private static final Logger			log		= LoggerFactory.getLogger(SSLFacade.class);
	@SuppressWarnings("unused")
	private static final String			TAG		= "SSLFascade";
	private boolean						_clientMode;
	private Handshaker					_handshaker;
	private IHandshakeCompletedListener	_hcl;
	private final Worker				_worker;
	private ChannelContext				channelContext;
	private AtomicLong					sslSeq	= new AtomicLong();

	public SSLFacade(ChannelContext channelContext, SSLContext context, boolean client, boolean clientAuthRequired, ITaskHandler taskHandler) {
		this.channelContext = channelContext;
		final String who = client ? "client" : "server";
		SSLEngine engine = makeSSLEngine(context, client, clientAuthRequired);
		Buffers buffers = new Buffers(engine.getSession(), channelContext);
		_worker = new Worker(who, engine, buffers, channelContext);
		_handshaker = new Handshaker(client, _worker, taskHandler, channelContext);
		_clientMode = client;
	}

	private void attachCompletionListener() {
		_handshaker.addCompletedListener(new IHandshakeCompletedListener() {
			@Override
			public void onComplete() {
				if (_hcl != null) {
					_hcl.onComplete();
					_hcl = null;
				}
			}
		});
	}

	@Override
	public void beginHandshake() throws SSLException {
		_handshaker.begin();
	}

	@Override
	public void encrypt(SslVo sslVo) throws SSLException {
		long seq = sslSeq.incrementAndGet();

		ByteBuffer src = sslVo.getByteBuffer();
		ByteBuffer[] byteBuffers = org.tio.core.utils.ByteBufferUtils.split(src, 200);
		if (byteBuffers == null) {
			log.debug("{}, 准备, SSL加密{}, 明文:{}", channelContext, channelContext.getId() + "_" + seq, sslVo);
			SSLEngineResult result = _worker.wrap(sslVo, sslVo.getByteBuffer());
			log.debug("{}, 完成, SSL加密{}, 明文:{}, 结果:{}", channelContext, channelContext.getId() + "_" + seq, sslVo, result);

		} else {
			log.debug("{}, 准备, SSL加密{}, 包过大，被拆成了[{}]个包进行发送, 明文:{}", channelContext, channelContext.getId() + "_" + seq, byteBuffers.length, sslVo);
			ByteBuffer[] encryptedByteBuffers = new ByteBuffer[byteBuffers.length];
			int alllen = 0;
			for (int i = 0; i < byteBuffers.length; i++) {
				SslVo sslVo1 = new SslVo(byteBuffers[i], sslVo.getObj());
				SSLEngineResult result = _worker.wrap(sslVo1, byteBuffers[i]);
				ByteBuffer encryptedByteBuffer = sslVo1.getByteBuffer();
				encryptedByteBuffers[i] = encryptedByteBuffer;
				alllen += encryptedByteBuffer.limit();
				log.debug("{}, 完成, SSL加密{}, 明文:{}, 拆包[{}]的结果:{}", channelContext, channelContext.getId() + "_" + seq, sslVo, i + 1, result);
			}

			ByteBuffer encryptedByteBuffer = ByteBuffer.allocate(alllen);
			for (ByteBuffer encryptedByteBuffer2 : encryptedByteBuffers) {
				encryptedByteBuffer.put(encryptedByteBuffer2);
			}
			encryptedByteBuffer.flip();
			sslVo.setByteBuffer(encryptedByteBuffer);
		}
	}

	@Override
	public void close() {
		_worker.close(true);
	}

	@Override
	public void decrypt(ByteBuffer byteBuffer) throws SSLException {
		long seq = sslSeq.incrementAndGet();
		log.debug("{}, 准备, SSL解密{}, 密文:{}", channelContext, channelContext.getId() + "_" + seq, byteBuffer);
		SSLEngineResult result = _worker.unwrap(byteBuffer);
		log.debug("{}, 完成, SSL解密{}, 密文:{}, 结果:{}", channelContext, channelContext.getId() + "_" + seq, byteBuffer, result);
		_handshaker.handleUnwrapResult(result);
	}

	@Override
	public boolean isClientMode() {
		return _clientMode;
	}

	@Override
	public boolean isCloseCompleted() {
		return _worker.isCloseCompleted();
	}

	@Override
	public boolean isHandshakeCompleted() {
		return _handshaker == null || _handshaker.isFinished();
	}

	private SSLEngine makeSSLEngine(SSLContext context, boolean client, boolean clientAuthRequired) {
		SSLEngine engine = context.createSSLEngine();
		engine.setUseClientMode(client);
		engine.setNeedClientAuth(clientAuthRequired);
		return engine;
	}

	@Override
	public void setCloseListener(ISessionClosedListener l) {
		_worker.setSessionClosedListener(l);
	}

	@Override
	public void setHandshakeCompletedListener(IHandshakeCompletedListener hcl) {
		_hcl = hcl;
		attachCompletionListener();
	}

	@Override
	public void setSSLListener(ISSLListener l) {
		_worker.setSSLListener(l);
	}

	@Override
	public void terminate() {
		/* Called if peer closed connection unexpectedly */
		_worker.close(false);
	}

}

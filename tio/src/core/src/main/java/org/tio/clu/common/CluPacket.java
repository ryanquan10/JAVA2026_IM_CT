/*
 * kcyrs本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vxzsstu
 */
/*
 * kcyrs本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动vxzsstu
 * grantinfo
 */
package org.tio.clu.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tio.clu.common.bs.BindReq;
import org.tio.clu.common.bs.base.Base;
import org.tio.core.intf.Packet;
import org.tio.utils.json.Json;

/**
 *
 * @author tanyaowu
 *
 */
public class CluPacket extends Packet {

	private static final long serialVersionUID = 2011243642394454841L;

	@SuppressWarnings("unused")
	private static Logger		log				= LoggerFactory.getLogger(CluPacket.class);
	public static final byte	HEARTBEAT_BYTE	= -128;
	public static final byte	HANDSHAKE_BYTE	= -127;
	public final static byte	VERSION			= 1;

	public static CluPacket from(Command command, Base body) {
		CluPacket cluPacket = new CluPacket();
		cluPacket.setCommand(command);
		cluPacket.setBody(body);
		return cluPacket;

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// "x".getBytes("utf-8");

		String str = String.format("%05d", (byte) 1);
		System.out.println("" + str);

		BindReq bodyobj = new BindReq();
		bodyobj.setV("55555");
		bodyobj.setVs(new String[] { "3333", "9999" });
		CluPacket cluPacket = CluPacket.from(Command.BestNodeReq, bodyobj);
		CluPacket c1 = (CluPacket) cluPacket.clone();
		System.out.println(Json.toFormatedJson(cluPacket));
		System.out.println(Json.toFormatedJson(c1));
	}

	/**
	 * 
	 */
	private Object body;

	// private byte[] body;

	private Command command = null;

	public CluPacket() {
	}

	public CluPacket(Command command) {
		this();
		this.setCommand(command);
	}

	public CluPacket(Command command, Object body) {
		this(command);
		this.body = body;
	}

	/**
	 * @return the body
	 */
	public Object getBody() {
		return body;
	}

	/**
	 * @return the body
	 */
	// public byte[] getBody() {
	// return body;
	// }

	public Command getCommand() {
		return command;
	}

	/**
	 * @see org.tio.core.intf.Packet#logstr()
	 *
	 * @return
	 * @author tanyaowu 2016年2月22日 下午3:15:18
	 *
	 */
	@Override
	public String logstr() {
		if (this.command != null) {
			return "packet: command【" + this.command.name() + "】";
		}
		return null;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(Base body) {
		this.body = body;
	}

	/**
	 * @param body the body to set
	 */
	// public void setBody(byte[] body) {
	// this.body = body;
	// }

	public void setCommand(Command command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "CluPacket [command=" + command + "]";
	}
}


/**
 * 
 */
package org.tio.sitexxx.im.server;

import org.tio.core.ChannelContext;
import org.tio.core.PacketConverter;
import org.tio.core.intf.Packet;
import org.tio.sitexxx.im.common.ImPacket;

/**
 * @author tanyaowu
 *
 */
public class ImToWsPacketConverter implements PacketConverter {

	public static final ImToWsPacketConverter me = new ImToWsPacketConverter();

	/**
	 * 
	 */
	private ImToWsPacketConverter() {
	}

	@Override
	public Packet convert(Packet packet, ChannelContext channelContext) {
		if (packet instanceof ImPacket) {
			ImPacket imPacket = (ImPacket) packet;

			//			ImSessionContext imSessionContext = ImUtils.getImSessionContext(channelContext);
			//			if (imSessionContext.isSuper()) {
			//				if (imPacket.getCommand() == Command.JoinGroupNtf) {
			//					JoinGroupNtf joinGroupNtf = (JoinGroupNtf) imPacket.getBodyObj();
			//					if (joinGroupNtf.getLastLoginSimpleUser() != null && (joinGroupNtf.getU() == null || joinGroupNtf.getU().getI() == null)) {
			//						JoinGroupNtf joinGroupNtf1 = new JoinGroupNtf();
			//
			//						//把joinGroupNtf的属性复制给joinGroupNtf1
			//						BeanUtil.copyProperties(joinGroupNtf, joinGroupNtf1);
			//						joinGroupNtf1.setU(joinGroupNtf.getLastLoginSimpleUser());
			//						imPacket = new ImPacket(Command.JoinGroupNtf, joinGroupNtf1);
			//					}
			//				}
			//			}

			return imPacket.toWs();
		}
		return packet;
	}
}

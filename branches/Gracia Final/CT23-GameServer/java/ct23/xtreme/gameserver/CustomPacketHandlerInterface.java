package ct23.xtreme.gameserver;

import java.nio.ByteBuffer;

import ct23.xtreme.gameserver.network.L2GameClient;
import ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * This interface can be implemented by custom extensions to BR Xtreme to get packets
 * before the normal processing of PacketHandler
 *
 * @version $Revision: $ $Date: $
 * @author  galun
 */
public interface CustomPacketHandlerInterface
{

    /**
     * interface for a custom packethandler to ckeck received packets
     * PacketHandler will take care of the packet if this function returns null.
     * @param data the packet
     * @param client the ClientThread
     * @return a ClientBasePacket if the packet has been processed, null otherwise
     */
    public L2GameClientPacket handlePacket(ByteBuffer data, L2GameClient client);
}

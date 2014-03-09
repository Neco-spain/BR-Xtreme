/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ct23.xtreme.gameserver.network.clientpackets;

import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2World;
import ct23.xtreme.gameserver.network.L2GameClient;
import ct23.xtreme.gameserver.network.serverpackets.ExRpItemLink;

/**
 *
 * @author  KenM
 */
public class RequestExRqItemLink extends L2GameClientPacket
{
    private int _objectId;
    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#getType()
     */
    @Override
    public String getType()
    {
        return "[C] DO:1E RequestExRqItemLink";
    }

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
     */
    @Override
    protected void readImpl()
    {
        _objectId = readD();
    }

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        L2GameClient client = this.getClient();
        if (client != null)
        {
            L2Object object = L2World.getInstance().findObject(_objectId);
            if (object instanceof L2ItemInstance)
            {
                L2ItemInstance item = (L2ItemInstance)object;
                client.sendPacket(new ExRpItemLink(item));
            }
        }
    }
    
}

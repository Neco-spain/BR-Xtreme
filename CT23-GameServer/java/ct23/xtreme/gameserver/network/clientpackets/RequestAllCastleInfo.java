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

import ct23.xtreme.gameserver.network.L2GameClient;
import ct23.xtreme.gameserver.network.serverpackets.ExShowCastleInfo;

/**
 *
 * @author  KenM
 */
public class RequestAllCastleInfo extends L2GameClientPacket
{

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#getType()
     */
    @Override
    public String getType()
    {
        return "[C] D0:3F RequestAllCastleInfo";
    }

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
     */
    @Override
    protected void readImpl()
    {
        // trigger
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
            client.sendPacket(new ExShowCastleInfo());
        }
    }
    
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}

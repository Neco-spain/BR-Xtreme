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
package ct25.xtreme.gameserver.network.clientpackets;

import ct25.xtreme.gameserver.network.L2GameClient;
import ct25.xtreme.gameserver.network.serverpackets.ExShowFortressInfo;

/**
 *
 * @author  KenM
 */
public class RequestAllFortressInfo extends L2GameClientPacket
{
	
	/**
	 * @see ct25.xtreme.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:3D RequestAllFortressInfo";
	}
	
	/**
	 * @see ct25.xtreme.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
	 */
	@Override
	protected void readImpl()
	{
		// trigger packet
	}
	
	/**
	 * @see ct25.xtreme.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2GameClient client = this.getClient();
		if (client != null)
		{
			client.sendPacket(new ExShowFortressInfo());
		}
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}

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
package ct25.xtreme.gameserver.network.gameserverpackets;

import java.io.IOException;

import javolution.util.FastList;

import ct25.xtreme.util.network.BaseSendablePacket;

/**
 * @author -Wooden-
 *
 */
public class PlayerInGame extends BaseSendablePacket
{
	public PlayerInGame (String player)
	{
		writeC(0x02);
		writeH(1);
		writeS(player);
	}
	
	public PlayerInGame (FastList<String> players)
	{
		writeC(0x02);
		writeH(players.size());
		for(String pc : players)
			writeS(pc);
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.gameserverpackets.GameServerBasePacket#getContent()
	 */
	@Override
	public byte[] getContent() throws IOException
	{
		return getBytes();
	}
	
}
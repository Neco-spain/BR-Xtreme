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

/**
 * Format ch
 * c: (id) 0x39
 * h: (subid) 0x02
 * @author -Wooden-
 *
 */
public final class SuperCmdServerStatus extends L2GameClientPacket
{
	private static final String _C__39_02_SUPERCMDSERVERSTATUS = "[C] 39:02 SuperCmdServerStatus";


	@Override
	protected void readImpl()
	{
		// trigger packet
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__39_02_SUPERCMDSERVERSTATUS;
	}

}
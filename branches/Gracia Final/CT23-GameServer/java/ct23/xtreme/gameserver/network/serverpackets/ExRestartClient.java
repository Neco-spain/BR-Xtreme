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
package ct23.xtreme.gameserver.network.serverpackets;

/**
 * Format: (ch)
 *
 * @author  -Wooden-
 */
public class ExRestartClient extends L2GameServerPacket
{
	private static final String _S__FE_47_EXRESTARTCLIENT = "[S] FE:48 ExRestartClient";

	/**
	 * @see ct23.xtreme.util.network.BaseSendablePacket.ServerBasePacket#writeImpl()
	 */
	@Override
	protected
	void writeImpl()
	{
		writeC(0xfe);
		writeH(0x48);
	}

	/**
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_47_EXRESTARTCLIENT;
	}

}
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
package ct25.xtreme.gameserver.network.serverpackets;

import ct25.xtreme.gameserver.model.actor.L2Character;

/**
 * @author Kerberos
 * 
 */
public class VehicleStarted extends L2GameServerPacket
{
	private int _objectId;
	private int _state;
	
	/**
	 * @param instance
	 */
	public VehicleStarted(L2Character boat, int state)
	{
		_objectId = boat.getObjectId();
		_state = state;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ct25.xtreme.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xC0);
		writeD(_objectId);
		writeD(_state);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ct25.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] C0 VehicleStarted";
	}
	
}

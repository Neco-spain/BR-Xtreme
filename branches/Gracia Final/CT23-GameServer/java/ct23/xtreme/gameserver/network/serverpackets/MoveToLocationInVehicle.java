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

import ct23.xtreme.gameserver.model.L2CharPosition;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Maktakien
 *
 */
public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatId;
	private L2CharPosition _destination;
	private L2CharPosition _origin;
	/**
	 * @param actor
	 * @param destination
	 * @param origin
	 */
	public MoveToLocationInVehicle(L2Character actor, L2CharPosition destination, L2CharPosition origin)
	{
		if (!(actor instanceof L2PcInstance)) return;

		L2PcInstance player = (L2PcInstance)actor;

		if (player.getBoat() == null) return;

		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	/*	_pci.sendMessage("_destination : x " + x +" y " + y + " z " + z);
		_pci.sendMessage("_boat : x " + _pci.getBoat().getX() +" y " + _pci.getBoat().getY() + " z " + _pci.getBoat().getZ());
		_pci.sendMessage("-----------");*/
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x7e);
        writeD(_charObjId);
        writeD(_boatId);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] 7e MoveToLocationInVehicle";
	}

}

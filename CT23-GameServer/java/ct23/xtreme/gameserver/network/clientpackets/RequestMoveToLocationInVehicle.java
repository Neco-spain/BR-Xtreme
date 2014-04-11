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

import ct23.xtreme.gameserver.TaskPriority;
import ct23.xtreme.gameserver.instancemanager.BoatManager;
import ct23.xtreme.gameserver.model.actor.instance.L2BoatInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct23.xtreme.gameserver.network.serverpackets.MoveToLocationInVehicle;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.templates.item.L2WeaponType;
import ct23.xtreme.util.Point3D;


public final class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private static final String _C__75_MOVETOLOCATIONINVEHICLE = "[C] 75 RequestMoveToLocationInVehicle";

	private int _boatId;
	private Point3D _pos;
	private Point3D _origin_pos;

	public TaskPriority getPriority() { return TaskPriority.PR_HIGH; }

	@Override
	protected void readImpl()
	{
		int _x, _y, _z;
		_boatId  = readD();   //objectId of boat
		_x = readD();
		_y = readD();
		_z = readD();
		_pos = new Point3D(_x, _y, _z);
		_x = readD();
		_y = readD();
		_z = readD();
		_origin_pos = new Point3D(_x, _y, _z);
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected
	void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		if (activeChar.isAttackingNow()
				&& activeChar.getActiveWeaponItem() != null
				&& (activeChar.getActiveWeaponItem().getItemType() == L2WeaponType.BOW))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isSitting() || activeChar.isMovementDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.getPet() != null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.RELEASE_PET_ON_BOAT));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (activeChar.isTransformed())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_POLYMORPH_ON_BOAT));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		final L2BoatInstance boat;
		if (activeChar.isInBoat())
		{
			boat = activeChar.getBoat();
			if (boat.getObjectId() != _boatId)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else
		{
			boat = BoatManager.getInstance().getBoat(_boatId);
			if (boat == null)
			{
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			activeChar.setVehicle(boat);
		}

		activeChar.setInVehiclePosition(_pos);
		activeChar.broadcastPacket(new MoveToLocationInVehicle(activeChar, _pos, _origin_pos));
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__75_MOVETOLOCATIONINVEHICLE;
	}
}
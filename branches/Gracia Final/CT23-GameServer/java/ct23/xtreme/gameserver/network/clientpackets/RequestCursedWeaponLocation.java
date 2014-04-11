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

import java.util.List;

import ct23.xtreme.gameserver.instancemanager.CursedWeaponsManager;
import ct23.xtreme.gameserver.model.CursedWeapon;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.network.serverpackets.ExCursedWeaponLocation;
import ct23.xtreme.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo;
import ct23.xtreme.util.Point3D;
import javolution.util.FastList;

/**
 * Format: (ch)
 * @author  -Wooden-
 */
public final class RequestCursedWeaponLocation extends L2GameClientPacket
{
	private static final String _C__D0_23_REQUESTCURSEDWEAPONLOCATION = "[C] D0:23 RequestCursedWeaponLocation";

	@Override
	protected void readImpl()
	{
		//nothing to read it's just a trigger
	}

	/**
	 * @see ct23.xtreme.util.network.BaseRecievePacket.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if (activeChar == null)
			return;

		List<CursedWeaponInfo> list = new FastList<CursedWeaponInfo>();
		for (CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if (!cw.isActive())
				continue;

			Point3D pos = cw.getWorldPosition();
			if (pos != null)
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}

		//send the ExCursedWeaponLocation
		if (!list.isEmpty())
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}

	/**
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_23_REQUESTCURSEDWEAPONLOCATION;
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}

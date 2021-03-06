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
package handlers.bypasshandlers;

import ct25.xtreme.gameserver.handler.IBypassHandler;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.ExShowVariationCancelWindow;
import ct25.xtreme.gameserver.network.serverpackets.ExShowVariationMakeWindow;

public class Augment implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Augment"
	};

	@Override
	public boolean useBypass(final String command, final L2PcInstance activeChar, final L2Character target)
	{
		if (!(target instanceof L2Npc))
			return false;

		try
		{
			switch (Integer.parseInt(command.substring(8, 9).trim()))
			{
				case 1:
					activeChar.sendPacket(new ExShowVariationMakeWindow());
					return true;
				case 2:
					activeChar.sendPacket(new ExShowVariationCancelWindow());
					return true;
			}
		}
		catch (final Exception e)
		{
			_log.info("Exception in " + getClass().getSimpleName());
		}
		return false;
	}

	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
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
package handlers.admincommandhandlers;

import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

public class AdminDebug implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_debug"
	};

	@Override
	public final boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		final String[] commandSplit = command.split(" ");
		if (ADMIN_COMMANDS[0].equalsIgnoreCase(commandSplit[0]))
		{
			L2Object target;
			if (commandSplit.length > 1)
			{
				target = L2World.getInstance().getPlayer(commandSplit[1].trim());
				if (target == null)
				{
					activeChar.sendMessage("Player not found.");
					return true;
				}
			}
			else
				target = activeChar.getTarget();

			if (target instanceof L2Character)
				setDebug(activeChar, (L2Character) target);
			else
				setDebug(activeChar, activeChar);
		}
		return true;
	}

	@Override
	public final String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private final void setDebug(final L2PcInstance activeChar, final L2Character target)
	{
		if (target.isDebug())
		{
			target.setDebug(null);
			activeChar.sendMessage("Stop debugging " + target.getName());
		}
		else
		{
			target.setDebug(activeChar);
			activeChar.sendMessage("Start debugging " + target.getName());
		}
	}
}
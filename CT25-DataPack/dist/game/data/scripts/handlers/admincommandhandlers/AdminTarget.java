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
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following admin commands: - target name = sets player with respective name as target
 * @version $Revision: 1.2.4.3 $ $Date: 2005/04/11 10:05:56 $
 */
public class AdminTarget implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_target"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.startsWith("admin_target"))
			handleTarget(command, activeChar);
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleTarget(final String command, final L2PcInstance activeChar)
	{
		try
		{
			final String targetName = command.substring(13);
			final L2PcInstance player = L2World.getInstance().getPlayer(targetName);
			if (player != null)
				player.onAction(activeChar);
			else
				activeChar.sendMessage("Player " + targetName + " not found");
		}
		catch (final IndexOutOfBoundsException e)
		{
			activeChar.sendMessage("Please specify correct name.");
		}
	}
}

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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ct25.xtreme.Config;
import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - changelvl = change a character's access level Can be used for character ban (as opposed to regular //ban that affects accounts) or to grant mod/GM privileges ingame
 * @version $Revision: 1.1.2.2.2.3 $ $Date: 2005/04/11 10:06:00 $ con.close() change by Zoey76 24/02/2011
 */
public class AdminChangeAccessLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_changelvl"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		handleChangeLevel(command, activeChar);
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	/**
	 * If no character name is specified, tries to change GM's target access level. Else if a character name is provided, will try to reach it either from L2World or from a database connection.
	 * @param command
	 * @param activeChar
	 */
	@SuppressWarnings("resource")
	private void handleChangeLevel(final String command, final L2PcInstance activeChar)
	{
		final String[] parts = command.split(" ");
		if (parts.length == 2)
			try
			{
				final int lvl = Integer.parseInt(parts[1]);
				if (activeChar.getTarget() instanceof L2PcInstance)
					onLineChange(activeChar, (L2PcInstance) activeChar.getTarget(), lvl);
				else
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
			}
		else if (parts.length == 3)
		{
			final String name = parts[1];
			final int lvl = Integer.parseInt(parts[2]);
			final L2PcInstance player = L2World.getInstance().getPlayer(name);
			if (player != null)
				onLineChange(activeChar, player, lvl);
			else
			{
				Connection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					final PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
					statement.setInt(1, lvl);
					statement.setString(2, name);
					statement.execute();
					final int count = statement.getUpdateCount();
					statement.close();
					if (count == 0)
						activeChar.sendMessage("Character not found or access level unaltered.");
					else
						activeChar.sendMessage("Character's access level is now set to " + lvl);
				}
				catch (final SQLException se)
				{
					activeChar.sendMessage("SQLException while changing character's access level");
					if (Config.DEBUG)
						se.printStackTrace();
				}
				finally
				{
					L2DatabaseFactory.close(con);
				}
			}
		}
	}

	/**
	 * @param activeChar
	 * @param player
	 * @param lvl
	 */
	private void onLineChange(final L2PcInstance activeChar, final L2PcInstance player, final int lvl)
	{
		player.setAccessLevel(lvl);
		if (lvl >= 0)
			player.sendMessage("Your access level has been changed to " + lvl);
		else
		{
			player.sendMessage("Your character has been banned. Bye.");
			player.logout();
		}
		activeChar.sendMessage("Character's access level is now set to " + lvl + ". Effects won't be noticeable until next session.");
	}
}

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

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.LoginServerThread;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class handles following commands:<BR>
 * <BR>
 * <LI>admin_blockip</LI>
 * <LI>admin_unblockip</LI>
 * @author RiZe
 */
public class AdminBlockIp implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_blockip",
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.IAdminCommandHandler#useAdminCommand
	 */
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		String address;
		long expiration = 0;
		final StringTokenizer st = new StringTokenizer(command);
		st.nextToken();

		if (command.startsWith("admin_blockip"))
		{
			DateFormat formatter;
			final StringBuilder date = new StringBuilder();

			if (st.hasMoreTokens())
			{
				address = st.nextToken();

				try
				{
					if (st.hasMoreTokens())
					{
						date.append(st.nextToken());

						if (st.hasMoreTokens())
						{
							formatter = new SimpleDateFormat(Config.BLOCK_DATE_FORMAT + " " + Config.BLOCK_HOUR_FORMAT);
							date.append(" ").append(st.nextToken());
						}
						else
							formatter = new SimpleDateFormat(Config.BLOCK_DATE_FORMAT);

						expiration = formatter.parse(date.toString()).getTime();
					}
					
					InetAddress.getByName(address);
					LoginServerThread.getInstance().sendBlockAddress(address, expiration);
				}
				catch (final Exception e)
				{
					activeChar.sendMessage("Usage mode: //blockip <ip> [MM/dd/yy] [hh:mm]");
				}
			}
			else
				activeChar.sendMessage("Usage mode: //blockip <ip> [MM/dd/yy] [hh:mm]");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
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

import java.util.StringTokenizer;
import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.instance.L2ControllableMobInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - kill = kills target L2Character - kill_monster = kills target non-player - kill <radius> = If radius is specified, then ALL players only in that radius will be killed. - kill_monster <radius> = If radius is specified, then ALL non-players only in
 * that radius will be killed.
 * @version $Revision: 1.2.4.5 $ $Date: 2007/07/31 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminKill.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kill",
		"admin_kill_monster"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.startsWith("admin_kill"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command

			if (st.hasMoreTokens())
			{
				final String firstParam = st.nextToken();
				final L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);
				if (plyr != null)
				{
					if (st.hasMoreTokens())
						try
						{
							final int radius = Integer.parseInt(st.nextToken());
							for (final L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
							{
								if (knownChar instanceof L2ControllableMobInstance || knownChar == activeChar)
									continue;

								kill(activeChar, knownChar);
							}

							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
							return true;
						}
						catch (final NumberFormatException e)
						{
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					kill(activeChar, plyr);
				}
				else
					try
					{
						final int radius = Integer.parseInt(firstParam);

						for (final L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if (knownChar instanceof L2ControllableMobInstance || knownChar == activeChar)
								continue;
							kill(activeChar, knownChar);
						}

						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
						return true;
					}
					catch (final NumberFormatException e)
					{
						activeChar.sendMessage("Usage: //kill <player_name | radius>");
						return false;
					}
			}
			else
			{
				final L2Object obj = activeChar.getTarget();
				if (obj instanceof L2ControllableMobInstance || !(obj instanceof L2Character))
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
				else
					kill(activeChar, (L2Character) obj);
			}
		}
		return true;
	}

	private void kill(final L2PcInstance activeChar, final L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			if (!((L2PcInstance) target).isGM())
				target.stopAllEffects(); // e.g. invincibility effect
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar, null);
		}
		else if (Config.L2JMOD_CHAMPION_ENABLE && target.isChampion())
			target.reduceCurrentHp(target.getMaxHp() * Config.L2JMOD_CHAMPION_HP + 1, activeChar, null);
		else
		{
			boolean targetIsInvul = false;
			if (target.isInvul())
			{
				targetIsInvul = true;
				target.setIsInvul(false);
			}

			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar, null);

			if (targetIsInvul)
				target.setIsInvul(true);
		}
		if (Config.DEBUG)
			_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ")" + " killed character " + target.getObjectId());
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}

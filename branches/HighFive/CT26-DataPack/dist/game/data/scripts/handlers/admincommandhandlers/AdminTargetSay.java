/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import ct26.xtreme.gameserver.handler.IAdminCommandHandler;
import ct26.xtreme.gameserver.model.L2Object;
import ct26.xtreme.gameserver.model.actor.L2Character;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2StaticObjectInstance;
import ct26.xtreme.gameserver.network.SystemMessageId;
import ct26.xtreme.gameserver.network.clientpackets.Say2;
import ct26.xtreme.gameserver.network.serverpackets.CreatureSay;

/**
 * This class handles following admin commands: - targetsay <message> = makes talk a L2Character
 * @author nonom
 */
public class AdminTargetSay implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_targetsay"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_targetsay"))
		{
			try
			{
				final L2Object obj = activeChar.getTarget();
				if ((obj instanceof L2StaticObjectInstance) || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
				
				final String message = command.substring(16);
				final L2Character target = (L2Character) obj;
				target.broadcastPacket(new CreatureSay(target.getObjectId(), (target.isPlayer() ? Say2.ALL : Say2.NPC_ALL), target.getName(), message));
			}
			catch (StringIndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Usage: //targetsay <text>");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
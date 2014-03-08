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
package handlers.usercommandhandlers;

import ct26.xtreme.gameserver.handler.IUserCommandHandler;
import ct26.xtreme.gameserver.instancemanager.SiegeManager;
import ct26.xtreme.gameserver.model.L2Clan;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.entity.Siege;
import ct26.xtreme.gameserver.model.zone.type.L2SiegeZone;
import ct26.xtreme.gameserver.network.SystemMessageId;
import ct26.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * @author Tryskell
 */
public class SiegeStatus implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		99
	};
	
	private static final String INSIDE_SIEGE_ZONE = "Castle Siege in Progress";
	private static final String OUTSIDE_SIEGE_ZONE = "No Castle Siege Area";
	
	@Override
	public boolean useUserCommand(int id, L2PcInstance activeChar)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		if (!activeChar.isNoble() || !activeChar.isClanLeader())
		{
			activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW);
			return false;
		}
		
		for (Siege siege : SiegeManager.getInstance().getSieges())
		{
			if (!siege.getIsInProgress())
			{
				continue;
			}
			
			final L2Clan clan = activeChar.getClan();
			if (!siege.checkIsAttacker(clan) && !siege.checkIsDefender(clan))
			{
				continue;
			}
			
			final L2SiegeZone siegeZone = siege.getCastle().getZone();
			final StringBuilder sb = new StringBuilder();
			for (L2PcInstance member : clan.getOnlineMembers(0))
			{
				sb.append("<tr><td width=170>");
				sb.append(member.getName());
				sb.append("</td><td width=100>");
				sb.append(siegeZone.isInsideZone(member) ? INSIDE_SIEGE_ZONE : OUTSIDE_SIEGE_ZONE);
				sb.append("</td></tr>");
			}
			
			final NpcHtmlMessage html = new NpcHtmlMessage();
			html.setFile(activeChar.getHtmlPrefix(), "data/html/siege/siege_status.htm");
			html.replace("%kill_count%", clan.getSiegeKills());
			html.replace("%death_count%", clan.getSiegeDeaths());
			html.replace("%member_list%", sb.toString());
			activeChar.sendPacket(html);
			
			return true;
		}
		
		activeChar.sendPacket(SystemMessageId.ONLY_NOBLESSE_LEADER_CAN_VIEW_SIEGE_STATUS_WINDOW);
		
		return false;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
	
}

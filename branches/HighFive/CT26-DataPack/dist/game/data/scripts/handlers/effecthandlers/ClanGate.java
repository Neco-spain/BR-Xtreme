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
package handlers.effecthandlers;

import ct26.xtreme.gameserver.model.L2Clan;
import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.effects.L2EffectType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.network.SystemMessageId;
import ct26.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * Clan Gate effect implementation.
 * @author ZaKaX
 */
public final class ClanGate extends AbstractEffect
{
	public ClanGate(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CLAN_GATE;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			final L2Clan clan = info.getEffected().getActingPlayer().getClan();
			if (clan != null)
			{
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL);
				clan.broadcastToOtherOnlineMembers(msg, info.getEffected().getActingPlayer());
			}
		}
	}
}

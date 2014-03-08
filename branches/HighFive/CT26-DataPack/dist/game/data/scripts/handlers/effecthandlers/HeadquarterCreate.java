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

import ct26.xtreme.gameserver.datatables.NpcData;
import ct26.xtreme.gameserver.idfactory.IdFactory;
import ct26.xtreme.gameserver.instancemanager.CHSiegeManager;
import ct26.xtreme.gameserver.instancemanager.CastleManager;
import ct26.xtreme.gameserver.instancemanager.FortManager;
import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2SiegeFlagInstance;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.entity.Castle;
import ct26.xtreme.gameserver.model.entity.Fort;
import ct26.xtreme.gameserver.model.entity.clanhall.SiegableHall;
import ct26.xtreme.gameserver.model.skills.BuffInfo;

/**
 * Headquarter Create effect implementation.
 * @author Adry_85
 */
public class HeadquarterCreate extends AbstractEffect
{
	private static final int HQ_NPC_ID = 35062;
	private final boolean _isAdvanced;
	
	public HeadquarterCreate(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_isAdvanced = params.getBoolean("isAdvanced", false);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance player = info.getEffector().getActingPlayer();
		if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()))
		{
			return;
		}
		
		final L2SiegeFlagInstance flag = new L2SiegeFlagInstance(player, IdFactory.getInstance().getNextId(), NpcData.getInstance().getTemplate(HQ_NPC_ID), _isAdvanced, false);
		flag.setTitle(player.getClan().getName());
		flag.setCurrentHpMp(flag.getMaxHp(), flag.getMaxMp());
		flag.setHeading(player.getHeading());
		flag.spawnMe(player.getX(), player.getY(), player.getZ() + 50);
		final Castle castle = CastleManager.getInstance().getCastle(player);
		final Fort fort = FortManager.getInstance().getFort(player);
		final SiegableHall hall = CHSiegeManager.getInstance().getNearbyClanHall(player);
		if (castle != null)
		{
			castle.getSiege().getFlag(player.getClan()).add(flag);
		}
		else if (fort != null)
		{
			fort.getSiege().getFlag(player.getClan()).add(flag);
		}
		else
		{
			hall.getSiege().getFlag(player.getClan()).add(flag);
		}
	}
}

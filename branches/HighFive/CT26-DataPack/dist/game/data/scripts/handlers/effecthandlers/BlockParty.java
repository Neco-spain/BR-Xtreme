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

import ct26.xtreme.gameserver.instancemanager.PunishmentManager;
import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.punishment.PunishmentAffect;
import ct26.xtreme.gameserver.model.punishment.PunishmentTask;
import ct26.xtreme.gameserver.model.punishment.PunishmentType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;

/**
 * Block Party effect implementation.
 * @author BiggBoss
 */
public final class BlockParty extends AbstractEffect
{
	public BlockParty(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return (info.getEffected() != null) && info.getEffected().isPlayer();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		PunishmentManager.getInstance().stopPunishment(info.getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		PunishmentManager.getInstance().startPunishment(new PunishmentTask(0, info.getEffected().getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN, 0, "Party banned by bot report", "system", true));
	}
}

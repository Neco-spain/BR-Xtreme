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

import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.effects.L2EffectType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.network.SystemMessageId;

/**
 * Mana Damage Over Time effect implementation.
 */
public final class ManaDamOverTime extends AbstractEffect
{
	private final double _power;
	
	public ManaDamOverTime(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANA_DMG_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead())
		{
			return false;
		}
		
		final double manaDam = _power * getTicksMultiplier();
		if ((manaDam > info.getEffected().getCurrentMp()) && info.getSkill().isToggle())
		{
			info.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		info.getEffected().reduceCurrentMp(manaDam);
		return info.getSkill().isToggle();
	}
}
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
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.model.skills.Skill;

/**
 * Focus Max Energy effect implementation.
 * @author Adry_85
 */
public final class FocusMaxEnergy extends AbstractEffect
{
	public FocusMaxEnergy(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			final Skill sonicMastery = info.getEffected().getSkills().get(992);
			final Skill focusMastery = info.getEffected().getSkills().get(993);
			int maxCharge = (sonicMastery != null) ? sonicMastery.getLevel() : (focusMastery != null) ? focusMastery.getLevel() : 0;
			if (maxCharge != 0)
			{
				int count = maxCharge - info.getEffected().getActingPlayer().getCharges();
				info.getEffected().getActingPlayer().increaseCharges(count, maxCharge);
			}
		}
	}
}
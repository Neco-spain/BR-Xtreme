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
import ct26.xtreme.gameserver.model.actor.L2Summon;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.effects.L2EffectType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.model.stats.Formulas;
import ct26.xtreme.gameserver.network.SystemMessageId;
import ct26.xtreme.util.Rnd;

/**
 * Unsummon effect implementation.
 * @author Adry_85
 */
public final class Unsummon extends AbstractEffect
{
	private final int _chance;
	
	public Unsummon(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_chance = params.getInt("chance", 100);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		int magicLevel = info.getSkill().getMagicLevel();
		if ((magicLevel <= 0) || ((info.getEffected().getLevel() - 9) <= magicLevel))
		{
			double chance = _chance * Formulas.calcAttributeBonus(info.getEffector(), info.getEffected(), info.getSkill()) * Formulas.calcGeneralTraitBonus(info.getEffector(), info.getEffected(), info.getSkill().getTraitType(), false);
			if (chance > (Rnd.nextDouble() * 100))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return info.getEffected().isSummon();
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Summon summon = info.getEffected().getSummon();
		if (summon.isPhoenixBlessed() || summon.isNoblesseBlessed())
		{
			summon.stopEffects(L2EffectType.NOBLESSE_BLESSING);
		}
		else
		{
			summon.stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		summon.abortAttack();
		summon.abortCast();
		final L2PcInstance summonOwner = info.getEffected().getActingPlayer();
		summon.unSummon(summonOwner);
		summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
	}
}

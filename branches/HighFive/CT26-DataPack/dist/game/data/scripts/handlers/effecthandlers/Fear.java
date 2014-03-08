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

import ct26.xtreme.Config;
import ct26.xtreme.gameserver.GeoData;
import ct26.xtreme.gameserver.ai.CtrlEvent;
import ct26.xtreme.gameserver.ai.CtrlIntention;
import ct26.xtreme.gameserver.model.Location;
import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.actor.instance.L2DefenderInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2FortCommanderInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2NpcInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2ServitorInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2SiegeFlagInstance;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.effects.EffectFlag;
import ct26.xtreme.gameserver.model.effects.L2EffectType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.util.Util;

/**
 * Fear effect implementation.
 * @author littlecrow
 */
public final class Fear extends AbstractEffect
{
	public static final int FEAR_RANGE = 500;
	
	public Fear(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		if ((info.getEffected() instanceof L2NpcInstance) || (info.getEffected() instanceof L2DefenderInstance) || (info.getEffected() instanceof L2FortCommanderInstance) || (info.getEffected() instanceof L2SiegeFlagInstance) || (info.getEffected() instanceof L2ServitorInstance))
		{
			return false;
		}
		return !info.getEffected().isAfraid();
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}
	
	@Override
	public int getTicks()
	{
		return 5;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		fearAction(info, false);
		return false;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isCastingNow() && info.getEffected().canAbortCast())
		{
			info.getEffected().abortCast();
		}
		
		info.getEffected().getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		fearAction(info, true);
	}
	
	private void fearAction(BuffInfo info, boolean start)
	{
		double radians = Math.toRadians(start ? Util.calculateAngleFrom(info.getEffector(), info.getEffected()) : Util.convertHeadingToDegree(info.getEffected().getHeading()));
		
		int posX = (int) (info.getEffected().getX() + (FEAR_RANGE * Math.cos(radians)));
		int posY = (int) (info.getEffected().getY() + (FEAR_RANGE * Math.sin(radians)));
		int posZ = info.getEffected().getZ();
		
		if (Config.GEODATA > 0)
		{
			Location destiny = GeoData.getInstance().moveCheck(info.getEffected().getX(), info.getEffected().getY(), info.getEffected().getZ(), posX, posY, posZ, info.getEffected().getInstanceId());
			posX = destiny.getX();
			posY = destiny.getY();
		}
		
		if (!info.getEffected().isPet())
		{
			info.getEffected().setRunning();
		}
		
		info.getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ));
	}
}

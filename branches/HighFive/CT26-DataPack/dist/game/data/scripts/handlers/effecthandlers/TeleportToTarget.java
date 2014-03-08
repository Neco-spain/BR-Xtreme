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
import ct26.xtreme.gameserver.ai.CtrlIntention;
import ct26.xtreme.gameserver.model.Location;
import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.actor.L2Character;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.effects.L2EffectType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.network.serverpackets.FlyToLocation;
import ct26.xtreme.gameserver.network.serverpackets.FlyToLocation.FlyType;
import ct26.xtreme.gameserver.network.serverpackets.ValidateLocation;
import ct26.xtreme.gameserver.util.Util;

/**
 * Teleport To Target effect implementation.
 * @author Didldak, Adry_85
 */
public final class TeleportToTarget extends AbstractEffect
{
	public TeleportToTarget(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TELEPORT_TO_TARGET;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		L2Character activeChar = info.getEffector();
		L2Character target = info.getEffected();
		if (target == null)
		{
			return;
		}
		
		int px = target.getX();
		int py = target.getY();
		double ph = Util.convertHeadingToDegree(target.getHeading());
		
		ph += 180;
		if (ph > 360)
		{
			ph -= 360;
		}
		
		ph = (Math.PI * ph) / 180;
		int x = (int) (px + (25 * Math.cos(ph)));
		int y = (int) (py + (25 * Math.sin(ph)));
		int z = target.getZ();
		Location loc = new Location(x, y, z);
		if (Config.GEODATA > 0)
		{
			loc = GeoData.getInstance().moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), x, y, z, activeChar.getInstanceId());
		}
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, loc.getX(), loc.getY(), loc.getZ(), FlyType.DUMMY));
		activeChar.abortAttack();
		activeChar.abortCast();
		activeChar.setXYZ(loc);
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
	}
}

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
package handlers.targethandlers;

import ct26.xtreme.gameserver.handler.ITargetTypeHandler;
import ct26.xtreme.gameserver.model.L2Object;
import ct26.xtreme.gameserver.model.actor.L2Character;
import ct26.xtreme.gameserver.model.skills.Skill;
import ct26.xtreme.gameserver.model.skills.targets.L2TargetType;

/**
 * @author UnAfraid
 */
public class Self implements ITargetTypeHandler
{
	
	@Override
	public L2Object[] getTargetList(Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		return new L2Character[]
		{
			activeChar
		};
	}
	
	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.SELF;
	}
}
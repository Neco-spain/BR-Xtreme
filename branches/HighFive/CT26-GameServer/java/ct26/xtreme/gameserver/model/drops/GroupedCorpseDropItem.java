/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ct26.xtreme.gameserver.model.drops;

import ct26.xtreme.Config;
import ct26.xtreme.gameserver.model.actor.L2Character;

/**
 * @author Nos
 */
public class GroupedCorpseDropItem extends GroupedGeneralDropItem
{
	/**
	 * @param chance the chance of this drop item.
	 */
	public GroupedCorpseDropItem(double chance)
	{
		super(chance);
	}
	
	/*
	 * (non-Javadoc)
	 * @see ct26.xtreme.gameserver.model.drops.GroupedGeneralDropItem#getChance(ct26.xtreme.gameserver.model.actor.L2Character, ct26.xtreme.gameserver.model.actor.L2Character)
	 */
	@Override
	public double getChance(L2Character victim, L2Character killer)
	{
		return super.getChance(victim, killer) * Config.RATE_CORPSE_DROP_CHANCE_MULTIPLIER;
	}
}

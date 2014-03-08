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
package ct26.xtreme.gameserver.model.conditions;

import ct26.xtreme.gameserver.datatables.PetDataTable;
import ct26.xtreme.gameserver.model.actor.instance.L2PetInstance;
import ct26.xtreme.gameserver.model.items.L2Item;
import ct26.xtreme.gameserver.model.stats.Env;

/**
 * @author JIV
 */
public class ConditionPetType extends Condition
{
	private final int petType;
	
	public ConditionPetType(int petType)
	{
		this.petType = petType;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if (!(env.getCharacter() instanceof L2PetInstance))
		{
			return false;
		}
		
		int npcId = env.getCharacter().getId();
		if (PetDataTable.isStrider(npcId) && (petType == L2Item.STRIDER))
		{
			return true;
		}
		else if (PetDataTable.isGrowUpWolfGroup(npcId) && (petType == L2Item.GROWN_UP_WOLF_GROUP))
		{
			return true;
		}
		else if (PetDataTable.isHatchlingGroup(npcId) && (petType == L2Item.HATCHLING_GROUP))
		{
			return true;
		}
		else if (PetDataTable.isAllWolfGroup(npcId) && (petType == L2Item.ALL_WOLF_GROUP))
		{
			return true;
		}
		else if (PetDataTable.isBabyPetGroup(npcId) && (petType == L2Item.BABY_PET_GROUP))
		{
			return true;
		}
		else if (PetDataTable.isUpgradeBabyPetGroup(npcId) && (petType == L2Item.UPGRADE_BABY_PET_GROUP))
		{
			return true;
		}
		else if (PetDataTable.isItemEquipPetGroup(npcId) && (petType == L2Item.ITEM_EQUIP_PET_GROUP))
		{
			return true;
		}
		return false;
	}
	
}

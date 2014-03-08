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
package handlers.itemhandlers;

import ct26.xtreme.gameserver.datatables.PetDataTable;
import ct26.xtreme.gameserver.model.L2PetData;
import ct26.xtreme.gameserver.model.actor.L2Playable;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.entity.TvTEvent;
import ct26.xtreme.gameserver.model.holders.PetItemHolder;
import ct26.xtreme.gameserver.model.items.instance.L2ItemInstance;
import ct26.xtreme.gameserver.network.SystemMessageId;

/**
 * @author FBIagent, UnAfraid
 */
public class SummonItems extends ItemSkillsTemplate
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
			return false;
		}
		
		if (!TvTEvent.onItemSummon(playable.getObjectId()))
		{
			return false;
		}
		
		final L2PcInstance activeChar = playable.getActingPlayer();
		if (!activeChar.getFloodProtectors().getItemPetSummon().tryPerformAction("summon items") || (activeChar.getBlockCheckerArena() != -1) || activeChar.inObserverMode() || activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
		{
			return false;
		}
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return false;
		}
		
		if (activeChar.hasSummon() || activeChar.isMounted())
		{
			activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
			return false;
		}
		
		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
			return false;
		}
		
		final L2PetData petData = PetDataTable.getInstance().getPetDataByItemId(item.getId());
		if ((petData == null) || (petData.getNpcId() == -1))
		{
			return false;
		}
		
		activeChar.addScript(new PetItemHolder(item));
		return super.useItem(playable, item, forceUse);
	}
}

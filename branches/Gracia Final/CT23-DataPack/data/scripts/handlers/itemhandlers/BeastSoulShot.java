/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.itemhandlers;

import ct23.xtreme.gameserver.handler.IItemHandler;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.L2Summon;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PetInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.util.Broadcast;

/**
 * Beast SoulShot Handler
 *
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler
{
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.IItemHandler#useItem(ct23.xtreme.gameserver.model.actor.L2Playable, ct23.xtreme.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (playable == null)
			return;
		
		L2PcInstance activeOwner = null;
		if (playable instanceof L2Summon)
		{
			activeOwner = ((L2Summon) playable).getOwner();
			activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_USE_ITEM));
			return;
		}
		else if (playable instanceof L2PcInstance)
			activeOwner = (L2PcInstance) playable;
		
		if (activeOwner == null)
			return;
		L2Summon activePet = activeOwner.getPet();
		
		if (activePet == null)
		{
			activeOwner.sendPacket(new SystemMessage(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME));
			return;
		}
		
		if (activePet.isDead())
		{
			activeOwner.sendPacket(new SystemMessage(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET));
			return;
		}
		
		int itemId = item.getItemId();
		short shotConsumption = activePet.getSoulShotsPerHit();
		long shotCount = item.getCount();
		
		if (!(shotCount > shotConsumption))
		{
			// Not enough Soulshots to use.
			if (!activeOwner.disableAutoShot(itemId))
				activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
			return;
		}
		
		L2ItemInstance weaponInst = null;
		
		if (activePet instanceof L2PetInstance)
			weaponInst = ((L2PetInstance) activePet).getActiveWeaponInstance();	
		
		if (weaponInst == null)
		{
			if (activePet.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
				return;
			
			activePet.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT);
		}
		else
		{
			if (weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			{
				// SoulShots are already active.
				return;
			}
			weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		}
		
		// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
		if (!activeOwner.destroyItemWithoutTrace("Consume", item.getObjectId(), shotConsumption, null, false))
		{
			if (!activeOwner.disableAutoShot(itemId))
				activeOwner.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET));
			return;
		}
		
		// Pet uses the power of spirit.
		activeOwner.sendPacket(new SystemMessage(SystemMessageId.PET_USE_THE_POWER_OF_SPIRIT));
		
		Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(activePet, activePet, itemId == 6645 ? 2033 : 22036, 1, 0, 0), 360000/*600*/);
	}
}

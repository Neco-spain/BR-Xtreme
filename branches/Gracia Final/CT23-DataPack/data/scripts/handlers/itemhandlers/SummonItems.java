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

/**
 *
 * @author FBIagent
 *
 */

package handlers.itemhandlers;

import java.util.logging.Level;

import ct23.xtreme.gameserver.ThreadPoolManager;
import ct23.xtreme.gameserver.datatables.NpcTable;
import ct23.xtreme.gameserver.datatables.SummonItemsData;
import ct23.xtreme.gameserver.handler.IItemHandler;
import ct23.xtreme.gameserver.idfactory.IdFactory;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Spawn;
import ct23.xtreme.gameserver.model.L2SummonItem;
import ct23.xtreme.gameserver.model.L2World;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PetInstance;
import ct23.xtreme.gameserver.model.entity.TvTEvent;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.MagicSkillLaunched;
import ct23.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct23.xtreme.gameserver.network.serverpackets.PetItemList;
import ct23.xtreme.gameserver.network.serverpackets.SetupGauge;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct23.xtreme.gameserver.util.Broadcast;

public class SummonItems implements IItemHandler
{
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.IItemHandler#useItem(ct23.xtreme.gameserver.model.actor.L2Playable, ct23.xtreme.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;

		if (!TvTEvent.onItemSummon(playable.getObjectId()))
			return;

		final L2PcInstance activeChar = (L2PcInstance) playable;

		if (!activeChar.getFloodProtectors().getItemPetSummon().
                        tryPerformAction("summon items"))
                {
                    return;
                }

		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}

		if (activeChar.inObserverMode())
			return;

		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		if (activeChar.isAllSkillsDisabled() || activeChar.isCastingNow())
			return;

		final L2SummonItem sitem = SummonItemsData.getInstance().getSummonItem(item.getItemId());

		if ((activeChar.getPet() != null || activeChar.isMounted()) && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_ALREADY_HAVE_A_PET));
			return;
		}

		if (activeChar.isAttackingNow())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT));
			return;
		}

		if (activeChar.isCursedWeaponEquipped() && sitem.isPetSummon())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE));
			return;
		}

		final int npcID = sitem.getNpcId();
		if (npcID == 0)
			return;

		final L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcID);
		if (npcTemplate == null)
			return;

		activeChar.stopMove(null, false);

		switch (sitem.getType())
		{
			case 0: // static summons (like Christmas tree)
				try
				{
					final L2Spawn spawn = new L2Spawn(npcTemplate);

					spawn.setId(IdFactory.getInstance().getNextId());
					spawn.setLocx(activeChar.getX());
					spawn.setLocy(activeChar.getY());
					spawn.setLocz(activeChar.getZ());
					L2World.getInstance().storeObject(spawn.spawnOne(true));
					activeChar.destroyItem("Summon", item.getObjectId(), 1, null, false);
					activeChar.sendMessage("Created " + npcTemplate.name + " at x: " + spawn.getLocx() + " y: " + spawn.getLocy() + " z: " + spawn.getLocz());
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Target is not ingame.");
				}
				break;
			case 1: // pet summons
				final L2Object oldtarget = activeChar.getTarget();
				activeChar.setTarget(activeChar);
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, 2046, 1, 5000, 0), 2000);
				activeChar.setTarget(oldtarget);
				activeChar.sendPacket(new SetupGauge(0, 5000));
				activeChar.sendPacket(new SystemMessage(SystemMessageId.SUMMON_A_PET));
				activeChar.setIsCastingNow(true);

				ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFinalizer(activeChar, npcTemplate, item), 5000);
				break;
			case 2: // wyvern
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), true);
				break;
			case 3: // Great Wolf
				activeChar.mount(sitem.getNpcId(), item.getObjectId(), false);
				break;
		}
	}

	static class PetSummonFeedWait implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2PetInstance _petSummon;

		PetSummonFeedWait(L2PcInstance activeChar, L2PetInstance petSummon)
		{
			_activeChar = activeChar;
			_petSummon = petSummon;
		}

		public void run()
		{
			try
			{
				if (_petSummon.getCurrentFed() <= 0)
					_petSummon.unSummon(_activeChar);
				else
					_petSummon.startFeed();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}

	// TODO: this should be inside skill handler
	static class PetSummonFinalizer implements Runnable
	{
		private final L2PcInstance _activeChar;
		private final L2ItemInstance _item;
		private final L2NpcTemplate _npcTemplate;

		PetSummonFinalizer(L2PcInstance activeChar, L2NpcTemplate npcTemplate, L2ItemInstance item)
		{
			_activeChar = activeChar;
			_npcTemplate = npcTemplate;
			_item = item;
		}

		public void run()
		{
			try
			{
				_activeChar.sendPacket(new MagicSkillLaunched(_activeChar, 2046, 1));
				_activeChar.setIsCastingNow(false);

				// check for summon item validity
				if (_item == null
						|| _item.getOwnerId() != _activeChar.getObjectId()
						|| _item.getLocation() != L2ItemInstance.ItemLocation.INVENTORY)
					return;

				final L2PetInstance petSummon = L2PetInstance.spawnPet(_npcTemplate, _activeChar, _item);
				if (petSummon == null)
					return;

				petSummon.setTitle(_activeChar.getName());

				if (!petSummon.isRespawned())
				{
					petSummon.setCurrentHp(petSummon.getMaxHp());
					petSummon.setCurrentMp(petSummon.getMaxMp());
					petSummon.getStat().setExp(petSummon.getExpForThisLevel());
					petSummon.setCurrentFed(petSummon.getMaxFed());
				}

				petSummon.setRunning();

				if (!petSummon.isRespawned())
					petSummon.store();

				_activeChar.setPet(petSummon);

				L2World.getInstance().storeObject(petSummon);
				petSummon.spawnMe(_activeChar.getX() + 50, _activeChar.getY() + 100, _activeChar.getZ());
				petSummon.startFeed();
				_item.setEnchantLevel(petSummon.getLevel());

				if (petSummon.getCurrentFed() <= 0)
					ThreadPoolManager.getInstance().scheduleGeneral(new PetSummonFeedWait(_activeChar, petSummon), 60000);
				else
					petSummon.startFeed();

				petSummon.setFollowStatus(true);
				petSummon.setShowSummonAnimation(false); // shouldn't be this always true?
				final int weaponId = petSummon.getWeapon();
				final int armorId = petSummon.getArmor();
				final int jewelId = petSummon.getJewel();
				if (weaponId > 0 && petSummon.getOwner().getInventory().getItemByItemId(weaponId)!= null)
				{
					final L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(weaponId);
					final L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon); 
					if (newItem == null)
					{
						_log.warning("Invalid item transfer request: " + petSummon.getName() + "(pet) --> " + petSummon.getOwner().getName());
						petSummon.setWeapon(0);
					}
					else
						petSummon.getInventory().equipItem(newItem);
				}
				else
					petSummon.setWeapon(0);
				if (armorId > 0 && petSummon.getOwner().getInventory().getItemByItemId(armorId)!= null)
				{
					final L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(armorId);
					final L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon); 
					if (newItem == null)
					{
						_log.warning("Invalid item transfer request: " + petSummon.getName() + "(pet) --> " + petSummon.getOwner().getName());
						petSummon.setArmor(0);
					}
					else
						petSummon.getInventory().equipItem(newItem);
				}
				else
					petSummon.setArmor(0);
				if (jewelId > 0 && petSummon.getOwner().getInventory().getItemByItemId(jewelId)!= null)
				{
					final L2ItemInstance item = petSummon.getOwner().getInventory().getItemByItemId(jewelId);
					final L2ItemInstance newItem = petSummon.getOwner().transferItem("Transfer", item.getObjectId(), 1, petSummon.getInventory(), petSummon); 
					if (newItem == null)
					{
						_log.warning("Invalid item transfer request: " + petSummon.getName() + "(pet) --> " + petSummon.getOwner().getName());
						petSummon.setJewel(0);
					}
					else
						petSummon.getInventory().equipItem(newItem);
				}
				else
					petSummon.setJewel(0);
				petSummon.getOwner().sendPacket(new PetItemList(petSummon));
				petSummon.broadcastStatusUpdate();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
}
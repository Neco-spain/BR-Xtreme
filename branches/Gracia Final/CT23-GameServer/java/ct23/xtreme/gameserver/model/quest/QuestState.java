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
package ct23.xtreme.gameserver.model.quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastMap;

import ct23.xtreme.Config;
import ct23.xtreme.L2DatabaseFactory;
import ct23.xtreme.gameserver.GameTimeController;
import ct23.xtreme.gameserver.cache.HtmCache;
import ct23.xtreme.gameserver.datatables.ItemTable;
import ct23.xtreme.gameserver.instancemanager.QuestManager;
import ct23.xtreme.gameserver.model.L2DropData;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest.QuestSound;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ExShowQuestMark;
import ct23.xtreme.gameserver.network.serverpackets.InventoryUpdate;
import ct23.xtreme.gameserver.network.serverpackets.PlaySound;
import ct23.xtreme.gameserver.network.serverpackets.QuestList;
import ct23.xtreme.gameserver.network.serverpackets.StatusUpdate;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.network.serverpackets.TutorialCloseHtml;
import ct23.xtreme.gameserver.network.serverpackets.TutorialEnableClientEvent;
import ct23.xtreme.gameserver.network.serverpackets.TutorialShowHtml;
import ct23.xtreme.gameserver.network.serverpackets.TutorialShowQuestionMark;
import ct23.xtreme.gameserver.skills.Stats;
import ct23.xtreme.gameserver.templates.item.L2EtcItemType;
import ct23.xtreme.util.Rnd;

/**
 * @author Luis Arias
 */
public final class QuestState
{
	protected static final Logger _log = Logger.getLogger(Quest.class.getName());

	
	public static final byte DROP_DIVMOD = 0;
	public static final byte DROP_FIXED_RATE = 1;
	public static final byte DROP_FIXED_COUNT = 2;
	public static final byte DROP_FIXED_BOTH = 3;
	
	private final String _questName;
	private final L2PcInstance _player;
	private byte _state;
	private Map<String, String> _vars;
	private boolean _isExitQuestOnCleanUp = false;

	/**
	 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR/><BR/>
	 *
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
	 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
	 * <LI>Add drops gotten by the quest</LI>
	 * <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : L2PcInstance pointing out the player
	 * @param state : state of the quest
	 * @param completed : boolean for completion of the quest
	 */
	QuestState(Quest quest, L2PcInstance player, byte state)
	{
		_questName = quest.getName();
		_player = player;
		
		// Save the state of the quest for the player in the player's list of quest onwed
		getPlayer().setQuestState(this);
		
		// set the state of the quest
		_state = state;
	}

	public String getQuestName()
	{
		return _questName;
	}

	/**
	 * Return the quest
	 * @return Quest
	 */
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}

	/**
	 * Return the L2PcInstance
	 * @return L2PcInstance
	 */
	public L2PcInstance getPlayer()
	{
		return _player;
	}

	/**
	 * Return the state of the quest
	 * @return State
	 */
	public byte getState()
	{
		return _state;
	}

	/**
	 * Return true if quest just created, false otherwise
	 * @return
	 */
	public boolean isCreated()
	{
		return (getState() == State.CREATED);
	}

	/**
	 * Return true if quest completed, false otherwise
	 * @return boolean
	 */
	public boolean isCompleted()
	{
		return (getState() == State.COMPLETED);
	}

	/**
	 * Return true if quest started, false otherwise
	 * @return boolean
	 */
	public boolean isStarted()
	{
		return (getState() == State.STARTED);
	}

	/**
	 * Return state of the quest after its initialization.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Remove drops from previous state</LI>
	 * <LI>Set new state of the quest</LI>
	 * <LI>Add drop for new state</LI>
	 * <LI>Update information in database</LI>
	 * <LI>Send packet QuestList to client</LI>
	 * @param state
	 * @return object
	 */
	public Object setState(byte state)
	{
		// set new state if it is not already in that state
		if (_state != state)
		{
			final boolean newQuest = isCreated();
			_state = state;

			if (newQuest)
				Quest.createQuestInDb(this);
			else
				Quest.updateQuestInDb(this);

			getPlayer().sendPacket(new QuestList());
		}
		return state;
	}

	public Object setStateAndNotSave(byte state)
	{
		// set new state if it is not already in that state
		if (_state != state)
		{
			_state = state;
			getPlayer().sendPacket(new QuestList());
		}
		return state;
	}

	/**
	 * Add parameter used in quests.
	 * @param var : String pointing out the name of the variable for quest
	 * @param val : String pointing out the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String setInternal(String var, String val)
	{
		if (_vars == null)
			_vars = new FastMap<String, String>();
		
		if (val == null)
			val = "";
		
		_vars.put(var, val);
		return val;
	}

	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Initialize class variable "vars" if is null</LI>
	 * <LI>Initialize parameter "val" if is null</LI>
	 * <LI>Add/Update couple (var,val) in class variable FastMap "vars"</LI>
	 * <LI>If the key represented by "var" exists in FastMap "vars", the couple (var,val) is updated in the database. The key is known as
	 * existing if the preceding value of the key (given as result of function put()) is not null.<BR>
	 * If the key doesn't exist, the couple is added/created in the database</LI>
	 * @param var : String indicating the name of the variable for quest
	 * @param val : String indicating the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String set(String var, String val)
	{
		if (_vars == null)
			_vars = new FastMap<String, String>();

		if (val == null)
			val = "";

		// FastMap.put() returns previous value associated with specified key, or null if there was no mapping for key.
		String old = _vars.put(var, val);

		if (old != null)
			Quest.updateQuestVarInDb(this, var, val);
		else
			Quest.createQuestVarInDb(this, var, val);

		if ("cond".equals(var))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch (Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, getPlayer().getName() + ", " + getQuestName() + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e.getMessage(), e);
			}
		}

		return val;
	}

	/**
	 * Internally handles the progression of the quest so that it is ready for sending
	 * appropriate packets to the client<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI>
	 * <LI>If not, check if quest progress steps have been skipped</LI>
	 * <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI>
	 * <LI>If no steps were skipped, flags do not need to be prepared...</LI>
	 * <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not
	 * considered, while skipped steps before the parameter, if any, maintain their info</LI>
	 * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
	 * @param old : int indicating the previously noted step
	 *
	 * For more info on the variable communicating the progress steps to the client, please see
	 * @link ct23.xtreme.loginserver.serverpacket.QuestList
	 */
	private void setCond(int cond, int old)
	{
		int completedStateFlags = 0; // initializing...

		// if there is no change since last setting, there is nothing to do here
		if (cond == old)
			return;

		// cond 0 and 1 do not need completedStateFlags.  Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped).  So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if (cond < 3 || cond > 31)
		{
			unset("__compltdStateFlags");
		}
		else
			completedStateFlags = getInt("__compltdStateFlags");

		// case 1: No steps have been skipped so far...
		if (completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything.  If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...

			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if (cond > (old + 1))
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;

				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= ((1 << old) - 1);

				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else
		{
			// if this is a push back to a previous step, clear all completion flags ahead
			if (cond < old)
			{
				completedStateFlags &= ((1 << cond) - 1); // note, this also unsets the flag indicating that there exist skips

				//now, check if this resulted in no steps being skipped any more
				if (completedStateFlags == ((1 << cond) - 1))
					unset("__compltdStateFlags");
				else
				{
					// set the most significant bit back to 1 again, to correctly indicate that this skips states.
					// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
					// what the cond says)
					completedStateFlags |= 0x80000001;
					set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}
			}
			// if this moves forward, it changes nothing on previously skipped steps...so just mark this
			// state and we are done
			else
			{
				completedStateFlags |= (1 << (cond - 1));
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}

		// send a packet to the client to inform it of the quest progress (step change)
		QuestList ql = new QuestList();
		getPlayer().sendPacket(ql);

		int questId = getQuest().getQuestIntId();
		if (questId > 0 && questId < 19999 && cond > 0)
			getPlayer().sendPacket(new ExShowQuestMark(questId));
	}

	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR><BR>
	 * <U><I>Concept : </I></U>
	 * Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(String var)
	{
		if (_vars == null)
			return null;

		String old = _vars.remove(var);

		if (old != null)
			Quest.deleteQuestVarInDb(this, var);

		return old;
	}

	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this player after a reboot.
	 * This function is for storage of values that do not related to a specific quest but are
	 * global for all quests.  For example, player's can get only once the adena and XP reward for  
	 * the first class quests, but they can make more than one first class quest.
	 * @param var : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public final void saveGlobalQuestVar(String var, String value)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("REPLACE INTO character_quest_global_data (charId,var,value) VALUES (?,?,?)");
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not insert player's global quest variable: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Read from the database a previously saved variable for this quest.
	 * Due to performance considerations, this function should best be used only when the quest is first loaded.
	 * Subclasses of this class can define structures into which these loaded values can be saved.
	 * However, on-demand usage of this function throughout the script is not prohibited, only not recommended. 
	 * Values read from this function were entered by calls to "saveGlobalQuestVar"
	 * @param var : String designating the name of the variable for the quest
	 * @return String : String representing the loaded value for the passed var, or an empty string if the var was invalid
	 */
	public final String getGlobalQuestVar(String var)
	{
		String result = "";
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT value FROM character_quest_global_data WHERE charId = ? AND var = ?");
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			ResultSet rs = statement.executeQuery();
			if (rs.first())
				result = rs.getString(1);
			rs.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Could not load player's global quest variable: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		return result;
	}

	/**
	 * Permanently delete from the database one of the player's global quest variable that was previously saved.
	 * @param var : String designating the name of the variable
	 */
	public final void deleteGlobalQuestVar(String var)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId = ? AND var = ?");
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.executeUpdate();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete player's global quest variable: " + e.getMessage(), e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
	}

	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : name of the variable of quest
	 * @return String
	 */
	public String get(String var)
	{
		if (_vars == null)
			return null;

		return _vars.get(var);
	}

	/**
	 * Return the value of the variable of quest represented by "var"
	 * @param var : String designating the variable for the quest
	 * @return int
	 */
	public int getInt(String var)
	{
		if (_vars == null)
			return 0;

		final String variable = _vars.get(var);
		if (variable == null || variable.length() == 0)
			return 0;

		int varint = 0;
		try
		{
			varint = Integer.parseInt(variable);
		}
		catch (Exception e)
		{
			_log.log(Level.FINER, getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint + " ! " + e.getMessage(), e);
			//	    if (Config.AUTODELETE_INVALID_QUEST_DATA)
			//		exitQuest(true);
		}

		return varint;
	}
	
	/**
	 * Add player to get notification of characters death
	 * @param character : L2Character of the character to get notification of death
	 */
	public void addNotifyOfDeath(L2Character character)
	{
		if (character == null || !(character instanceof L2PcInstance))
			return;

		((L2PcInstance)character).addNotifyQuestOfDeath(this);
	}

	/**
	 * Return the quantity of one sort of item hold by the player
	 * @param itemId : ID of the item wanted to be count
	 * @return long
	 */
	public int getQuestItemsCount(int itemId)
	{
		int count = 0;

		for (L2ItemInstance item : getPlayer().getInventory().getItems())
			if (item != null && item.getItemId() == itemId)
				count += item.getCount();

		return count;
	}

	/**
	 * @param int : ID of the item you're looking for
	 * @return true if item exists in player's inventory, false - if not
	 */
	public boolean hasQuestItems(int itemId)
	{
		return getPlayer().getInventory().getItemByItemId(itemId) != null;
	}

	/**
	 * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 * @param itemId : ID of the item to check enchantment
	 * @return int
	 */
	public int getEnchantLevel(int itemId)
	{
		L2ItemInstance enchanteditem = getPlayer().getInventory().getItemByItemId(itemId);

		if (enchanteditem == null)
			return 0;

		return enchanteditem.getEnchantLevel();
	}

	/**
	 * Give adena to the player
	 * @param count
	 * @param applyRates
	 */
	public void giveAdena(long count, boolean applyRates)
	{
		giveItems(57, count, applyRates ? 0 : 1);
	}

	/**
	 * Give reward to player using multiplier's
	 * @param itemId
	 * @param count
	 */
	public void rewardItems(int itemId, long count)
	{
		if (count <= 0)
			return;

		L2ItemInstance _tmpItem = ItemTable.getInstance().createDummyItem(itemId);

		if (_tmpItem == null)
			return;
		
		if (itemId == 57)
		{
			count = (long) (count * Config.RATE_QUEST_REWARD_ADENA);
		}
		else if (Config.RATE_QUEST_REWARD_USE_MULTIPLIERS)
		{
			if(_tmpItem.isEtcItem())
			{
				L2EtcItemType _type = _tmpItem.getEtcItem().getItemType();
				
				if (_type == L2EtcItemType.POTION)
					count = (long) (count * Config.RATE_QUEST_REWARD_POTION);
				else if (_type == L2EtcItemType.SCROLL)
					count = (long) (count * Config.RATE_QUEST_REWARD_SCROLL);
				else if (_type == L2EtcItemType.RECEIPE)
					count = (long) (count * Config.RATE_QUEST_REWARD_RECIPE);
				else if (_type == L2EtcItemType.MATERIAL)
					count = (long) (count * Config.RATE_QUEST_REWARD_MATERIAL);
				else
					count = (long) (count * Config.RATE_QUEST_REWARD);
			}
		}
		else
		{
			count = (long) (count * Config.RATE_QUEST_REWARD);
		}
		
		// Add items to player's inventory
		L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());

		if (item == null)
			return;

		// If item for reward is gold, send message of gold reward to client
		if (itemId == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ADENA);
			smsg.addItemNumber(count);
			getPlayer().sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				getPlayer().sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				getPlayer().sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(getPlayer());
		su.addAttribute(StatusUpdate.CUR_LOAD, getPlayer().getCurrentLoad());
		getPlayer().sendPacket(su);
	}
	
	/**
	 * Gives single item to the player. No rates are applied to the amount.
	 * 
	 * @param itemId id of the item
	 */
	public void giveItems(final int itemId)
	{
		giveItems(itemId, 1, 0);
	}
	
	/**
	 * Give item/reward to the player
	 * @param itemId
	 * @param count
	 */
	public void giveItems(int itemId, long count)
	{
		giveItems(itemId, count, 0);
	}

	public void giveItems(int itemId, long count, int enchantlevel)
	{
		if (count <= 0)
			return;

		// If item for reward is adena (ID=57), modify count with rate for quest reward if rates available
		if (itemId == 57 && !(enchantlevel > 0))
			count = (long) (count * Config.RATE_QUEST_REWARD_ADENA);

		// Add items to player's inventory
		L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());

		if (item == null)
			return;

		// set enchant level for item if that item is not adena
		if (enchantlevel > 0 && itemId != 57)
			item.setEnchantLevel(enchantlevel);

		// If item for reward is gold, send message of gold reward to client
		if (itemId == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ADENA);
			smsg.addItemNumber(count);
			getPlayer().sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				getPlayer().sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				getPlayer().sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(getPlayer());
		su.addAttribute(StatusUpdate.CUR_LOAD, getPlayer().getCurrentLoad());
		getPlayer().sendPacket(su);
	}

	public void giveItems(int itemId, long count, byte attributeId, int attributeLevel)
	{
		if (count <= 0)
			return;

		// Add items to player's inventory
		L2ItemInstance item = getPlayer().getInventory().addItem("Quest", itemId, count, getPlayer(), getPlayer().getTarget());

		if (item == null)
			return;

		// set enchant level for item if that item is not adena
		if (attributeId >= 0 && attributeLevel > 0)
		{
			item.setElementAttr(attributeId, attributeLevel);
			if (item.isEquipped())
				item.updateElementAttrBonus(getPlayer());

			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			getPlayer().sendPacket(iu);
		}

		// If item for reward is gold, send message of gold reward to client
		if (itemId == 57)
		{
			SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ADENA);
			smsg.addItemNumber(count);
			getPlayer().sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if (count > 1)
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				getPlayer().sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = new SystemMessage(SystemMessageId.EARNED_ITEM);
				smsg.addItemName(item);
				getPlayer().sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(getPlayer());
		su.addAttribute(StatusUpdate.CUR_LOAD, getPlayer().getCurrentLoad());
		getPlayer().sendPacket(su);
	}
	/**
	 * Drop items to the player's inventory. Rate is 100%, amount is affected by Config.RATE_QUEST_DROP.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItemsAlways(int itemId, int count, int neededCount)
	{
		return dropItems(itemId, count, neededCount, L2DropData.MAX_CHANCE, DROP_FIXED_RATE);
	}
	
	/**
	 * Drop items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @param dropChance : Item drop rate (100% chance is defined by the L2DropData.MAX_CHANCE = 1.000.000).
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance)
	{
		return dropItems(itemId, count, neededCount, dropChance, DROP_DIVMOD);
	}
	
	/**
	 * Drop items to the player's inventory.
	 * @param itemId : Identifier of the item to be dropped.
	 * @param count : Quantity of items to be dropped.
	 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
	 * @param dropChance : Item drop rate (100% chance is defined by the L2DropData.MAX_CHANCE = 1.000.000).
	 * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
	 * @return boolean : Indicating whether item quantity has been reached.
	 */
	public boolean dropItems(int itemId, int count, int neededCount, int dropChance, byte type)
	{
		// Get current amount of item.
		final int currentCount = getQuestItemsCount(itemId);
		
		// Required amount reached already?
		if (neededCount > 0 && currentCount >= neededCount)
			return true;
		
		int amount = 0;
		switch (type)
		{
			case DROP_DIVMOD:
				dropChance *= Config.RATE_QUEST_DROP;
				amount = count * (dropChance / L2DropData.MAX_CHANCE);
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance % L2DropData.MAX_CHANCE)
					amount += count;
				break;
			
			case DROP_FIXED_RATE:
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance)
					amount = (int) (count * Config.RATE_QUEST_DROP);
				break;
			
			case DROP_FIXED_COUNT:
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance * Config.RATE_QUEST_DROP)
					amount = count;
				break;
			
			case DROP_FIXED_BOTH:
				if (Rnd.get(L2DropData.MAX_CHANCE) < dropChance)
					amount = count;
				break;
		}
		
		boolean reached = false;
		if (amount > 0)
		{
			// Limit count to reach required amount.
			if (neededCount > 0)
			{
				reached = (currentCount + amount) >= neededCount;
				amount = (reached) ? neededCount - currentCount : amount;
			}
			
			// Inventory slot check.
			if (!_player.getInventory().validateCapacityByItemId(itemId))
				return false;
			
			// Give items to the player.
			giveItems(itemId, amount, 0);
			
			// Play the sound.
			playSound(reached ? "Itemsound.quest_itemget" : "Itemsound.quest_middle");
		}
		
		return neededCount > 0 && reached;
	}

	//TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED
	public void addRadar(int x, int y, int z)
	{
		getPlayer().getRadar().addMarker(x, y, z);
	}

	public void removeRadar(int x, int y, int z)
	{
		getPlayer().getRadar().removeMarker(x, y, z);
	}

	public void clearRadar()
	{
		getPlayer().getRadar().removeAllMarkers();
	}

	// END STUFF THAT WILL PROBABLY BE CHANGED

	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Destroy quantity of items wanted</LI>
	 * <LI>Send new inventory list to player</LI>
	 * @param itemId : Identifier of the item
	 * @param count : Quantity of items to destroy
	 */
	public void takeItems(int itemId, long count)
	{
		// Get object item from player's inventory list
		L2ItemInstance item = getPlayer().getInventory().getItemByItemId(itemId);
		if (item == null)
			return;

		// Tests on count value in order not to have negative value
		if (count < 0 || count > item.getCount())
			count = item.getCount();

		// Destroy the quantity of items wanted
		if (item.isEquipped())
		{
			L2ItemInstance[] unequiped = getPlayer().getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm: unequiped)
				iu.addModifiedItem(itm);
			getPlayer().sendPacket(iu);
			getPlayer().broadcastUserInfo();
		}
		getPlayer().destroyItemByItemId("Quest", itemId, count, getPlayer(), true);
	}

	/**
	 * Send a packet in order to play sound at client terminal
	 * @param sound
	 */
	public void playSound(String sound)
	{
		getPlayer().sendPacket(new PlaySound(sound));
	}
	
	/**
	 * Send a packet in order to play a sound to the player.
	 * @param sound the {@link QuestSound} object of the sound to play
	 */
	public void playSound(QuestSound sound)
	{
		getQuest().playSound(getPlayer(), sound);
	}
	
	/**
	 * Add XP and SP as quest reward
	 * @param exp
	 * @param sp
	 */
	public void addExpAndSp(int exp, int sp)
	{
		getPlayer().addExpAndSp((int) getPlayer().calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) getPlayer().calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}

	/**
	 * Return random value
	 * @param max : max value for randomisation
	 * @return int
	 */
	public int getRandom(int max)
	{
		return Rnd.get(max);
	}

	/**
	 * Return number of ticks from GameTimeController
	 * @return int
	 */
	public int getItemEquipped(int loc)
	{
		return getPlayer().getInventory().getPaperdollItemId(loc);
	}

	/**
	 * Return the number of ticks from the GameTimeController
	 * @return int
	 */
	public int getGameTicks()
	{
		return GameTimeController.getGameTicks();
	}

	/**
	 * Return true if quest is to exited on clean up by QuestStateManager
	 * @return boolean
	 */
	public final boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}

	/**
	 * Return the QuestTimer object with the specified name
	 * @return QuestTimer<BR> Return null if name does not exist
	 */
	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}

	/**
	 * Start a timer for quest.<BR><BR>
	 * @param name<BR> The name of the timer. Will also be the value for event of onEvent
	 * @param time<BR> The milisecond value the timer will elapse
	 */
	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), false);
	}

	public void startQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), false);
	}

	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, getPlayer(), true);
	}

	public void startRepeatingQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, getPlayer(), true);
	}

	/**
	 * Return the QuestTimer object with the specified name
	 * @return QuestTimer<BR> Return null if name does not exist
	 */
	public final QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, getPlayer());
	}

	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, 0);
	}

	public L2Npc addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, false, despawnDelay);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}

	/**
	 * Add spawn for player instance
	 * Will despawn after the spawn length expires
	 * Uses player's coords and heading.
	 * Adds a little randomization in the x y coords
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha, true, 0);
	}

	public L2Npc addSpawn(int npcId, L2Character cha, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay);
	}

	/**
	 * Add spawn for player instance
	 * Will despawn after the spawn length expires
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
	}

	/**
	 * Add spawn for player instance
	 * Inherits coords and heading from specified L2Character instance.
	 * It could be either the player, or any killed/attacked mob
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay);
	}

	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}

	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn);
	}

	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(getPlayer(), fileName);
	}

	/**
	 * Destroy element used by quest when quest is exited
	 * @param repeatable
	 * @return QuestState
	 */
	public QuestState exitQuest(boolean repeatable)
	{
		// remove this quest from the notifyDeath list of this character if its on this list
		_player.removeNotifyQuestOfDeath(this);

		if (isCompleted() || isCreated())
			return this;

		// Say quest is completed
		setState(State.COMPLETED);

		// Clean registered quest items
		int[] itemIdList = getQuest().getRegisteredItemIds();
		if (itemIdList != null)
		{
			for (int i = 0; i < itemIdList.length; i++)
				takeItems(itemIdList[i], -1);
		}

		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		if (repeatable)
		{
			getPlayer().delQuestState(getQuestName());
			Quest.deleteQuestInDb(this);

			_vars = null;
		}
		else
		{
			// Otherwise, delete variables for quest and update database (quest CANNOT be created again => not repeatable)
			if (_vars != null)
			{
				for (String var : _vars.keySet())
					unset(var);
			}

			Quest.updateQuestInDb(this);
		}

		return this;
	}

	public void showQuestionMark(int number)
	{
		getPlayer().sendPacket(new TutorialShowQuestionMark(number));
	}

	public void playTutorialVoice(String voice)
	{
		getPlayer().sendPacket(new PlaySound(2, voice, 0, 0, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ()));
	}

	public void showTutorialHTML(String html)
	{
		String text = HtmCache.getInstance().getHtm(getPlayer().getHtmlPrefix(), "data/scripts/quests/255_Tutorial/" + html);
		if (text == null)
		{
			_log.warning("missing html page data/scripts/quests/255_Tutorial/" + html);
			text = "<html><body>File data/scripts/quests/255_Tutorial/" + html + " not found or file is empty.</body></html>";
		}
		getPlayer().sendPacket(new TutorialShowHtml(text));
	}

	public void closeTutorialHtml()
	{
		getPlayer().sendPacket(new TutorialCloseHtml());
	}

	public void onTutorialClientEvent(int number)
	{
		getPlayer().sendPacket(new TutorialEnableClientEvent(number));
	}

	public void dropItem(L2MonsterInstance npc, L2PcInstance player, int itemId, int count)
	{
		npc.dropItem(player, itemId, count);
	}
}
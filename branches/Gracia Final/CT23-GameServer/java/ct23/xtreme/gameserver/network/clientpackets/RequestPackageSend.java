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
package ct23.xtreme.gameserver.network.clientpackets;

import static ct23.xtreme.gameserver.model.itemcontainer.PcInventory.ADENA_ID;

import java.util.logging.Logger;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.itemcontainer.ItemContainer;
import ct23.xtreme.gameserver.model.itemcontainer.PcFreight;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.InventoryUpdate;
import ct23.xtreme.gameserver.network.serverpackets.ItemList;
import ct23.xtreme.gameserver.network.serverpackets.StatusUpdate;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 *
 * @author  -Wooden-
 */
public final class RequestPackageSend extends L2GameClientPacket
{
	private static final String _C_9F_REQUESTPACKAGESEND = "[C] 9F RequestPackageSend";
	private static Logger _log = Logger.getLogger(RequestPackageSend.class.getName());

	private static final int BATCH_LENGTH = 12; // length of the one item

	private Item[] _items = null;
	private int _objectID;

	@Override
	protected void readImpl()
	{
		_objectID = readD();
		int count = readD();
		if (count < 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}
		_items = new Item[count];
		for(int i = 0; i < count; i++)
		{
			int id = readD(); //this is some id sent in PackageSendableList
			long cnt = readQ();
			_items[i] = new Item(id, cnt);
		}
	}

	/**
	 * @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected
	void runImpl()
	{
		if (_items == null || !Config.ALLOW_FREIGHT)
			return;

		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if (!player.getFloodProtectors().getTransaction().tryPerformAction("freight"))
		{
			player.sendMessage("You using freight too fast.");
			return;
		}

		// Alt game - Karma punishment
		if (!Config.ALT_GAME_KARMA_PLAYER_CAN_USE_WAREHOUSE && player.getKarma() > 0)
			return;

		// player attempts to send freight to the different account
		if (!player.getAccountChars().containsKey(_objectID))
			return;

		PcFreight freight = player.getDepositedFreight(_objectID);

		player.setActiveWarehouse(freight);
		ItemContainer warehouse = player.getActiveWarehouse();
		if (warehouse == null)
			return;

		L2Npc manager = player.getLastFolkNPC();
		if ((manager == null
				|| !manager.isWarehouse()
				|| !manager.canInteract(player)) && !player.isGM())
			return;

		if (warehouse instanceof PcFreight && !player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			return;
		}

		// get current tradelist if any
		if (player.getActiveTradeList() != null)
			return;

    	// Freight price from config or normal price per item slot (30)
		long fee = _items.length * Config.ALT_GAME_FREIGHT_PRICE;
		long currentAdena = player.getAdena();
		int slots = 0;

		for (Item i : _items)
		{
			// Check validity of requested item
			L2ItemInstance item = player.checkItemManipulation(i.getObjectId(), i.getCount(), "deposit");
			if (item == null)
			{
				_log.warning("Error depositing a warehouse object for char "+player.getName()+" (validity check)");
				return;
			}

			// Calculate needed adena and slots
			if (item.getItemId() == ADENA_ID)
				currentAdena -= i.getCount();
			if (!item.isStackable())
				slots += i.getCount();
			else if (warehouse.getItemByItemId(item.getItemId()) == null)
				slots++;
		}

    	// Item Max Limit Check
		if (!warehouse.validateCapacity(slots))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
			return;
		}

		// Check if enough adena and charge the fee
		if (currentAdena < fee || !player.reduceAdena(warehouse.getName(), fee, manager, false))
		{
			sendPacket(new SystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
			return;
		}

		// Proceed to the transfer
		InventoryUpdate playerIU = Config.FORCE_INVENTORY_UPDATE ? null : new InventoryUpdate();
		for (Item i : _items)
		{
			L2ItemInstance oldItem = player.getInventory().getItemByObjectId(i.getObjectId());
			if (oldItem == null)
			{
				_log.warning("Error depositing a warehouse object for char "+player.getName()+" (olditem == null)");
				continue;
			}

			if (!oldItem.isDepositable(false) || !oldItem.isAvailable(player, true, false))
				continue;

			L2ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getObjectId(), i.getCount(), warehouse, player, manager);
			if (newItem == null)
			{
				_log.warning("Error depositing a warehouse object for char "+player.getName()+" (newitem == null)");
				continue;
			}

			if (playerIU != null)
			{
				if (oldItem.getCount() > 0 && oldItem != newItem)
					playerIU.addModifiedItem(oldItem);
				else
					playerIU.addRemovedItem(oldItem);
			}
		}

		// Send updated item list to the player
		if (playerIU != null)
			player.sendPacket(playerIU);
		else
			player.sendPacket(new ItemList(player, false));

		// Update current load status on player
		StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.CUR_LOAD, player.getCurrentLoad());
		player.sendPacket(su);
	}

	/**
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C_9F_REQUESTPACKAGESEND;
	}

	private class Item
	{
		private final int _objectId;
		private final long _count;

		public Item(int i, long c)
		{
			_objectId = i;
			_count = c;
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public long getCount()
		{
			return _count;
		}
	}
}
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

import static ct23.xtreme.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;

import java.util.logging.Logger;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.model.ItemRequest;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2World;
import ct23.xtreme.gameserver.model.TradeList;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;

/**
 * This class ...
 *
 * @version $Revision: 1.2.2.1.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPrivateStoreSell extends L2GameClientPacket
{
	private static final String _C__96_REQUESTPRIVATESTORESELL = "[C] 96 RequestPrivateStoreSell";
	private static Logger _log = Logger.getLogger(RequestPrivateStoreSell.class.getName());

	private static final int BATCH_LENGTH = 28; // length of the one item

	private int _storePlayerId;
	private ItemRequest[] _items = null;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if (count <= 0
				|| count > Config.MAX_ITEM_IN_PACKET
				|| count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}
		_items = new ItemRequest[count];

		for (int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readH(); //TODO analyse this
			readH(); //TODO analyse this
			long cnt = readQ();
			long price = readQ();

			if (objectId < 1 || itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, cnt, price);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;

		if(_items == null)
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!player.getFloodProtectors().getTransaction().tryPerformAction("privatestoresell"))
		{
			player.sendMessage("You selling items too fast");
			return;
		}

		L2Object object = L2World.getInstance().findObject(_storePlayerId);
		if (!(object instanceof L2PcInstance))
			return;

		L2PcInstance storePlayer = (L2PcInstance)object;
		if (!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
			return;

		if (player.getInstanceId() != storePlayer.getInstanceId()
				&& player.getInstanceId() != -1)
			return;

		if (storePlayer.getPrivateStoreType() != L2PcInstance.STORE_PRIVATE_BUY)
			return;

		if(player.isCursedWeaponEquipped())
			return;

		TradeList storeList = storePlayer.getBuyList();
		if (storeList == null)
			return;

		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}

		if (!storeList.privateStoreSell(player, _items))
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			_log.warning("PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}

		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}

	@Override
	public String getType()
	{
		return _C__96_REQUESTPRIVATESTORESELL;
	}
}
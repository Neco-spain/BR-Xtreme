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

import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.handler.IItemHandler;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.instance.L2FeedableBeastInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;

public class BeastSpice implements IItemHandler
{
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.IItemHandler#useItem(ct23.xtreme.gameserver.model.actor.L2Playable, ct23.xtreme.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (!(activeChar.getTarget() instanceof L2FeedableBeastInstance))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return;
		}
		
		int itemId = item.getItemId();
		if (itemId == 6643)
		{ // Golden Spice
			activeChar.useMagic(SkillTable.getInstance().getInfo(2188, 1), false, false);
		}
		else if (itemId == 6644)
		{ // Crystal Spice
			activeChar.useMagic(SkillTable.getInstance().getInfo(2189, 1), false, false);
		}
	}
}
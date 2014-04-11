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

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;

public class ManaPotion extends ItemSkills
{
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.IItemHandler#useItem(ct23.xtreme.gameserver.model.actor.L2Playable, ct23.xtreme.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!Config.L2JMOD_ENABLE_MANA_POTIONS_SUPPORT)
		{
			playable.sendPacket(new SystemMessage(SystemMessageId.NOTHING_HAPPENED));
			return;
		}
		super.useItem(playable, item);
	}
}
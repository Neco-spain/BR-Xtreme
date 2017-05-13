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
package hellbound.Budenka;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class Budenka extends Quest
{
	// Npc
	private static final int BUDENKA = 32294;

	// Items (Certificats)
	private static final int STANDART_CERT = 9851;
	private static final int PREMIUM_CERT = 9852;

	@Override
	public final String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (player.getInventory().getInventoryItemCount(PREMIUM_CERT, -1, false) > 0)
			return "32294-premium.htm";
		if (player.getInventory().getInventoryItemCount(STANDART_CERT, -1, false) > 0)
			return "32294-standart.htm";

		npc.showChatWindow(player);
		return null;
	}

	public Budenka(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(BUDENKA);
	}

	public static void main(final String[] args)
	{
		new Budenka(-1, Budenka.class.getSimpleName(), "hellbound");
		
	}
}
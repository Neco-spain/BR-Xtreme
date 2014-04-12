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
package teleports.Survivor;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;

public class Survivor extends Quest
{
	private final static int survivor = 32632;

	public Survivor(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(survivor);
		addTalkId(survivor);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		if (player.getLevel() >= 75)
		{
			if (st.getQuestItemsCount(57) >= 150000)
			{
				player.teleToLocation(-149406, 255247, -80);
				st.takeItems(57, 150000);
			}
			else
				htmltext = "32632-2.htm";
		}
		else
			htmltext = "32632-3.htm";

		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Survivor(-1, "Survivor", "teleports");
	}
}
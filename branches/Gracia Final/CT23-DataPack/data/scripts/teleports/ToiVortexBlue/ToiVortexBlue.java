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
package teleports.ToiVortexBlue;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;

public class ToiVortexBlue extends Quest
{
	private final static int DIMENSION_VORTEX_1 = 30952;
	private final static int DIMENSION_VORTEX_3 = 30954;
	private final static int BLUE_DIMENSION_STONE = 4402;

	public ToiVortexBlue(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(DIMENSION_VORTEX_1,DIMENSION_VORTEX_3);
		addTalkId(DIMENSION_VORTEX_1,DIMENSION_VORTEX_3);;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		if (npcId == DIMENSION_VORTEX_1 || npcId == DIMENSION_VORTEX_3)
		{
			if (st.getQuestItemsCount(BLUE_DIMENSION_STONE) >= 1)
			{
				st.takeItems(BLUE_DIMENSION_STONE, 1);
				player.teleToLocation(114097, 19935, 935);
			}
			else
				htmltext = "1.htm";
		}
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ToiVortexBlue(-1, "ToiVortexBlue", "teleports");
	}
}
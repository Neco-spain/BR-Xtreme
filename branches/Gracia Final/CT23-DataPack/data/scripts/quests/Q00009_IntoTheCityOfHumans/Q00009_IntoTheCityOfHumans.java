/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00009_IntoTheCityOfHumans;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q00009_IntoTheCityOfHumans extends Quest
{
	// NPCs
	public final int PETUKAI = 30583;
	public final int TANAPI = 30571;
	public final int TAMIL = 30576;
	
	// Rewards
	public final int MARK_OF_TRAVELER = 7570;
	public final int SOE_GIRAN = 7126;
	
	public Q00009_IntoTheCityOfHumans()
	{
		super(9, Q00009_IntoTheCityOfHumans.class.getSimpleName(), "Into the City of Humans");
		
		addStartNpc(PETUKAI);
		addTalkId(PETUKAI, TANAPI, TAMIL);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg();
		}
		
		String htmltext = event;
		switch (event)
		{
			case "30583-01.htm":
				st.startQuest();
				break;
				
			case "30571-01.htm":
				st.set("cond", "2");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
				
			case "30576-01.htm":
				st.giveItems(MARK_OF_TRAVELER, 1);
				st.rewardItems(SOE_GIRAN, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(true);
				break;
				
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 3 && player.getRace() == Race.Orc)
					htmltext = "30583-00.htm";
				else
					htmltext = "30583-00a.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case PETUKAI:
						if (cond == 1)
							htmltext = "30583-01a.htm";
						break;
					
					case TANAPI:
						if (cond == 1)
							htmltext = "30571-00.htm";
						else if (cond == 2)
							htmltext = "30571-01a.htm";
						break;
					
					case TAMIL:
						if (cond == 2)
							htmltext = "30576-00.htm";
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00009_IntoTheCityOfHumans();
	}
}
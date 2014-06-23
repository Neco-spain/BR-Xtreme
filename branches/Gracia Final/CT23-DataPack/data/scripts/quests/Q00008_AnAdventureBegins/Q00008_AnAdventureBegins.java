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
package quests.Q00008_AnAdventureBegins;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q00008_AnAdventureBegins extends Quest
{
	private static final String qn = "Q00008_AnAdventureBegins";
	
	// NPCs
	private static final int JASMINE = 30134;
	private static final int ROSELYN = 30355;
	private static final int HARNE = 30144;
	
	// Items
	private static final int ROSELYN_NOTE = 7573;
	
	// Rewards
	private static final int SOE_GIRAN = 7559;
	private static final int MARK_TRAVELER = 7570;
	
	public Q00008_AnAdventureBegins()
	{
		super(8, qn, "An Adventure Begins");
		
		registerQuestItems(ROSELYN_NOTE);
		addStartNpc(JASMINE);
		addTalkId(JASMINE, ROSELYN, HARNE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg();
		}
		
		String htmltext = event;
		switch (event)
		{
			case "30134-03.htm":
				st.startQuest();
				break;
				
			case "30355-02.htm":
				st.set("cond", "2");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(ROSELYN_NOTE, 1);
				break;
				
			case "30144-02.htm":
				st.set("cond", "3");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(ROSELYN_NOTE, 1);
				break;
				
			case "30134-06.htm":
				st.giveItems(MARK_TRAVELER, 1);
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
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getLevel() >= 3 && player.getRace() == Race.DarkElf)
					htmltext = "30134-02.htm";
				else
					htmltext = "30134-01.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case JASMINE:
						if (cond == 1 || cond == 2)
							htmltext = "30134-04.htm";
						else if (cond == 3)
							htmltext = "30134-05.htm";
						break;
					
					case ROSELYN:
						if (cond == 1)
							htmltext = "30355-01.htm";
						else if (cond == 2)
							htmltext = "30355-03.htm";
						break;
					
					case HARNE:
						if (cond == 2)
							htmltext = "30144-01.htm";
						else if (cond == 3)
							htmltext = "30144-03.htm";
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
		new Q00008_AnAdventureBegins();
	}
}
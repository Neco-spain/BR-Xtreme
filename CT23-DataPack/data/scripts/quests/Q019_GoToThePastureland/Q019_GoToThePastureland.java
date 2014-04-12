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
package quests.Q019_GoToThePastureland;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q019_GoToThePastureland extends Quest
{
	private static final String qn = "Q019_GoToThePastureland";
	
	// Items
	private static final int YOUNG_WILD_BEAST_MEAT = 7547;
	
	// NPCs
	private static final int VLADIMIR = 31302;
	private static final int TUNATUN = 31537;
	
	public Q019_GoToThePastureland()
	{
		super(19, qn, "Go to the Pastureland!");
		
		questItemIds = new int[]
		{
			YOUNG_WILD_BEAST_MEAT
		};
		
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR, TUNATUN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("31302-01.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
			st.giveItems(YOUNG_WILD_BEAST_MEAT, 1);
		}
		else if (event.equalsIgnoreCase("019_finish"))
		{
			if (st.hasQuestItems(YOUNG_WILD_BEAST_MEAT))
			{
				htmltext = "31537-01.htm";
				st.takeItems(YOUNG_WILD_BEAST_MEAT, 1);
				st.rewardItems(57, 30000);
				st.addExpAndSp(136766,12688);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(false);
			}
			else
				htmltext = "31537-02.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				htmltext = (player.getLevel() < 63) ? "31302-03.htm" : "31302-00.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case VLADIMIR:
						htmltext = "31302-02.htm";
						break;
					
					case TUNATUN:
						htmltext = "31537-00.htm";
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
		new Q019_GoToThePastureland();
	}
}
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
package quests.Q00019_GoToThePastureland;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q00019_GoToThePastureland extends Quest
{
	// Items
	private static final int YOUNG_WILD_BEAST_MEAT = 7547;
	
	// NPCs
	private static final int VLADIMIR = 31302;
	private static final int TUNATUN = 31537;
	
	public Q00019_GoToThePastureland()
	{
		super(19, Q00019_GoToThePastureland.class.getSimpleName(), "Go to the Pastureland!");
		
		registerQuestItems(YOUNG_WILD_BEAST_MEAT);
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR, TUNATUN);
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
			case "31302-01.htm":
				st.startQuest();
				st.giveItems(YOUNG_WILD_BEAST_MEAT, 1);
				break;
			
			case "31537-01.htm":
				if (st.hasQuestItems(YOUNG_WILD_BEAST_MEAT))
				{
					st.takeItems(YOUNG_WILD_BEAST_MEAT, 1);
					st.rewardItems(57, 50000);
					st.addExpAndSp(136766,12688);
					st.exitQuest(false,true);
				}
				else
					htmltext = "31537-02.htm";
				break;
		}	
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(getName());
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
		new Q00019_GoToThePastureland();
	}
}
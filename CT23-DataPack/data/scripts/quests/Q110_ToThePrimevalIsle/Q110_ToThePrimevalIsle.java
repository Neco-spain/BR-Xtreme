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
package quests.Q110_ToThePrimevalIsle;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * To The Primeval Isle (110)
 * @author Ethernaly Pyton
 * @author BossForever Java
 */
public class Q110_ToThePrimevalIsle extends Quest
{
	private static final String qn = "Q110_ToThePrimevalIsle";
	
	// Reward
	private static final int ANCIENT_BOOK = 8777;
	
	// NPCs
	private static final int ANTON = 31338;
	private static final int MARQUEZ = 32113;
	
	public Q110_ToThePrimevalIsle()
	{
		super(110, qn, "To The Primeval Isle!");
		addStartNpc(ANTON);
		addTalkId(ANTON, MARQUEZ);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("1"))
		{
			htmltext = "1.htm";
			st.set("cond", "1");
			st.giveItems(ANCIENT_BOOK, 1);
			st.setState(State.STARTED);
			st.playSound("ItemSound.quest_accept");
		}
		if (event.equalsIgnoreCase("2") && st.hasQuestItems(ANCIENT_BOOK))
		{
			htmltext = "3.htm";
			st.playSound("ItemSound.quest_finish");
			st.giveItems(57, 191678);
			st.addExpAndSp(251602, 25245);
			st.takeItems(ANCIENT_BOOK, -1);
			st.exitQuest(false);
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
				if (st.getPlayer().getLevel() <= 75)
					htmltext = "0.htm";
				else
					htmltext = "<html><body>This quest can only be taken by characters that have a minimum level of 75. Return when you are more experienced.</body></html>";
				st.exitQuest(true);
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case ANTON:
						htmltext = "0c.htm";
						break;
					
					case MARQUEZ:
						if (st.isCond(1))
							htmltext = "2.htm";
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
		new Q110_ToThePrimevalIsle();
	}
}

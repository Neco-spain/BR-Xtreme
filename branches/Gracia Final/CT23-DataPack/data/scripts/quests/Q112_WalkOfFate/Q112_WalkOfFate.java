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
package quests.Q112_WalkOfFate;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Walk of Fate (112)
 * @author Eyerobot Pyton
 * @author BossForever Java
 */
public class Q112_WalkOfFate extends Quest
{
	private static final String qn = "Q112_WalkOfFate";
	
	// Items
	private static final int EnchantD = 956;
	
	// NPCs
	private static final int Livina = 30572;
	private static final int Karuda = 32017;

	public Q112_WalkOfFate()
	{
		super(112, qn, "Walk of Fate!");


		addStartNpc(Livina);
		addTalkId(Livina, Karuda);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
	    QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
	    	    
		int cond = st.getInt("cond");
	    	    
		if (event.equalsIgnoreCase("32017-02.htm") && cond == 1)
		{
			st.giveItems(57,22308);
			st.giveItems(EnchantD,1);
			st.addExpAndSp(112876,5774);
			st.exitQuest(false);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		else if (event.equalsIgnoreCase("30572-02.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
			st.setState(State.STARTED);
			st.set("cond","1");
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
				htmltext = (player.getLevel() >= 20) ? "30572-01.htm" : "30572-00.htm";
				break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case Livina:
						htmltext = "30572-03.htm";
						break;
					
					case Karuda:
						htmltext = "32017-01.htm";
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
		new Q112_WalkOfFate();
	}
}
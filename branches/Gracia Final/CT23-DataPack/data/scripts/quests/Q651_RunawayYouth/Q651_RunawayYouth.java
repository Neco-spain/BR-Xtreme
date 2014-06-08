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
package quests.Q651_RunawayYouth;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Runaway Youth (651)
 * @author Polo & DrLecter(Python)
 * @author BossForever & Malefic (Java)
 */
public class Q651_RunawayYouth extends Quest
{
	private static final String qn = "Q651_RunawayYouth";
	
	// Items
	private static final int SOE = 736;
	
	// NPCs
	private static final int IVAN = 32014;
	private static final int BATIDAE = 31989;

	public Q651_RunawayYouth()
	{
		super(651, qn, "Runaway Youth!");

		addStartNpc(IVAN);
		addTalkId(IVAN, BATIDAE);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
	    QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
	    	    
	    if (event.equalsIgnoreCase("32014-04.htm"))
	    {
	    	if (!st.hasQuestItems(SOE))
	    	{
	    		st.set("cond","1");
	    		st.setState(State.STARTED);
	    		st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
	    		st.takeItems(SOE,1);
	    		htmltext = "32014-03.htm";
	    		npc.deleteMe();
	    	}
	    }
	    else if (event.equalsIgnoreCase("32014-04a.htm"))
	    {
	    	st.exitQuest(true);
	    	st.playSound(QuestSound.ITEMSOUND_QUEST_GIVEUP);
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
		
		switch (npc.getNpcId())
		{
			case IVAN:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = (player.getLevel() >=26) ? "32014-02.htm" : "32014-01.htm";
						break;
					case State.STARTED:
						htmltext = "32014-02.htm";
						break;
				}
				break;
			case BATIDAE:
				if (st.isStarted())
				{
					st.giveAdena(2883, true);
					st.exitQuest(true);
					htmltext = "31989-01.htm";
				}
				break;
		}
		
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q651_RunawayYouth();
	}
}
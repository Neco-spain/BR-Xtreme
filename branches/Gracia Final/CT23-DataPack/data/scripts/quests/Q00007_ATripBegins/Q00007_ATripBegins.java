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
package quests.Q00007_ATripBegins;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q00007_ATripBegins extends Quest
{
	// NPCs
	private static final int MIRABEL = 30146;
	private static final int ARIEL = 30148;
	private static final int ASTERIOS = 30154;
	
	// Items
	private static final int ARIEL_RECO = 7572;
	
	// Rewards
	private static final int MARK_TRAVELER = 7570;
	private static final int SOE_GIRAN = 7559;
	
	public Q00007_ATripBegins()
	{
		super(7, Q00007_ATripBegins.class.getSimpleName(), "A Trip Begins");
		
		registerQuestItems(ARIEL_RECO);
		addStartNpc(MIRABEL);
		addTalkId(MIRABEL, ARIEL, ASTERIOS);
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
			case "30146-03.htm":
				st.startQuest();
				
			case "30148-02.htm":
				st.set("cond", "2");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(ARIEL_RECO, 1);
				break;
				
			case "30154-02.htm":
				st.set("cond", "3");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(ARIEL_RECO, 1);
				break;
				
			case "30146-06.htm":
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
		QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				if (player.getRace() != Race.Elf)
					htmltext = "30146-01.htm";
				else if (player.getLevel() < 3)
					htmltext = "30146-01a.htm";
				else
					htmltext = "30146-02.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case MIRABEL:
						if (cond == 1 || cond == 2)
							htmltext = "30146-04.htm";
						else if (cond == 3)
							htmltext = "30146-05.htm";
						break;
					
					case ARIEL:
						if (cond == 1)
							htmltext = "30148-01.htm";
						else if (cond == 2)
							htmltext = "30148-03.htm";
						break;
					
					case ASTERIOS:
						if (cond == 2)
							htmltext = "30154-01.htm";
						else if (cond == 3)
							htmltext = "30154-03.htm";
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
		new Q00007_ATripBegins();
	}
}
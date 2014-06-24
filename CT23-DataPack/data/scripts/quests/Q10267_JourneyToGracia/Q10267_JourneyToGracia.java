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
package quests.Q10267_JourneyToGracia;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Journey To Gracia (10267)<br>
 * Original Jython script by Kerberos.
 * @author nonom
 */
public class Q10267_JourneyToGracia extends Quest
{
	// NPCs
	private static final int ORVEN = 30857;
	private static final int KEUCEREUS = 32548;
	private static final int PAPIKU = 32564;
	
	// Item
	private static final int LETTER = 13810;
	
	// Misc
	private static final int MIN_LV = 75;
	
	public Q10267_JourneyToGracia(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(ORVEN);
		addTalkId(ORVEN, KEUCEREUS, PAPIKU);
		questItemIds = new int[]{LETTER};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg();
		}
		
		switch (event)
		{
			case "30857-06.htm":
				st.startQuest();
				st.giveItems(LETTER, 1);
				break;
			case "32564-02.htm":
				st.setCond(2, true);
				break;
			case "32548-02.htm":
				st.giveAdena(92500, true);
				st.addExpAndSp(75480, 7570);
				st.exitQuest(false, true);
				break;
		}
		return event;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getNpcId())
		{
			case ORVEN:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = (player.getLevel() < MIN_LV) ? "30857-00.htm" : "30857-01.htm";
						break;
					case State.STARTED:
						htmltext = "30857-07.htm";
						break;
					case State.COMPLETED:
						htmltext = "30857-0a.htm";
						break;
				}
				break;
			case PAPIKU:
				if (st.isStarted())
				{
					htmltext = st.isCond(1) ? "32564-01.htm" : "32564-03.htm";
				}
				break;
			case KEUCEREUS:
				if (st.isStarted() && st.isCond(2))
				{
					htmltext = "32548-01.htm";
				}
				else if (st.isCompleted())
				{
					htmltext = "32548-03.htm";
				}
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q10267_JourneyToGracia(10267, Q10267_JourneyToGracia.class.getSimpleName(), "Journey to Gracia");
	}
}

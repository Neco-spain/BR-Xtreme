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
package quests.Q015_SweetWhispers;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q015_SweetWhispers extends Quest
{
	private static final String qn = "Q015_SweetWhispers";
	
	// NPCs
	private static final int VLADIMIR = 31302;
	private static final int HIERARCH = 31517;
	private static final int MYSTERIOUS_NECRO = 31518;
	
	public Q015_SweetWhispers()
	{
		super(15, qn, "Sweet Whispers");
		
		addStartNpc(VLADIMIR);
		addTalkId(VLADIMIR, HIERARCH, MYSTERIOUS_NECRO);
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
		}
		else if (event.equalsIgnoreCase("31518-01.htm"))
		{
			st.set("cond", "2");
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if (event.equalsIgnoreCase("31517-01.htm"))
		{
			st.addExpAndSp(350531,28204);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
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
				htmltext = (player.getLevel() < 60) ? "31302-00a.htm" : "31302-00.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case VLADIMIR:
						htmltext = "31302-01a.htm";
						break;
					
					case MYSTERIOUS_NECRO:
						if (cond == 1)
							htmltext = "31518-00.htm";
						else if (cond == 2)
							htmltext = "31518-01a.htm";
						break;
					
					case HIERARCH:
						if (cond == 2)
							htmltext = "31517-00.htm";
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
		new Q015_SweetWhispers();
	}
}
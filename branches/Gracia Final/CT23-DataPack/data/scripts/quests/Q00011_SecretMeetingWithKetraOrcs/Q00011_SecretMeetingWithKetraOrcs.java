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
package quests.Q00011_SecretMeetingWithKetraOrcs;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q00011_SecretMeetingWithKetraOrcs extends Quest
{
	// Npcs
	private static final int CADMON = 31296;
	private static final int LEON = 31256;
	private static final int WAHKAN = 31371;
	
	// Items
	private static final int MUNITIONS_BOX = 7231;
	
	public Q00011_SecretMeetingWithKetraOrcs()
	{
		super(11, Q00011_SecretMeetingWithKetraOrcs.class.getSimpleName(), "Secret Meeting With Ketra Orcs");
		
		registerQuestItems(MUNITIONS_BOX);
		addStartNpc(CADMON);
		addTalkId(CADMON, LEON, WAHKAN);
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
			case "31296-03.htm":
				st.startQuest();
				break;
				
			case "31256-02.htm":
				st.giveItems(MUNITIONS_BOX, 1);
				st.set("cond", "2");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
				
			case "31371-02.htm":
				st.takeItems(MUNITIONS_BOX, 1);
				st.addExpAndSp(82045, 6047);
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
				htmltext = (player.getLevel() < 74) ? "31296-02.htm" : "31296-01.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case CADMON:
						if (cond == 1)
							htmltext = "31296-04.htm";
						break;
					
					case LEON:
						if (cond == 1)
							htmltext = "31256-01.htm";
						else if (cond == 2)
							htmltext = "31256-03.htm";
						break;
					
					case WAHKAN:
						if (cond == 2)
							htmltext = "31371-01.htm";
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
		new Q00011_SecretMeetingWithKetraOrcs();
	}
}
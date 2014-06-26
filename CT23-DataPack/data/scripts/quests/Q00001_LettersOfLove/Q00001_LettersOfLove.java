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
package quests.Q00001_LettersOfLove;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

public class Q00001_LettersOfLove extends Quest
{
	// Npcs
	private static final int DARIN = 30048;
	private static final int ROXXY = 30006;
	private static final int BAULRO = 30033;
	
	// Items
	private static final int DARINGS_LETTER = 687;
	private static final int RAPUNZELS_KERCHIEF = 688;
	private static final int DARINGS_RECEIPT = 1079;
	private static final int BAULROS_POTION = 1080;
	
	// Reward
	private static final int NECKLACE = 906;
	
	public Q00001_LettersOfLove()
	{
		super(1, Q00001_LettersOfLove.class.getSimpleName(), "Letters of Love");
		
		registerQuestItems(DARINGS_LETTER,RAPUNZELS_KERCHIEF,DARINGS_RECEIPT,BAULROS_POTION);
		addStartNpc(DARIN);
		addTalkId(DARIN, ROXXY, BAULRO);
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
			case "30048-06.htm":
				st.startQuest();
				st.giveItems(DARINGS_LETTER, 1);
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
				htmltext = (player.getLevel() < 2) ? "30048-01.htm" : "30048-02.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case DARIN:
						if (cond == 1)
							htmltext = "30048-07.htm";
						else if (cond == 2)
						{
							htmltext = "30048-08.htm";
							st.set("cond", "3");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(RAPUNZELS_KERCHIEF, 1);
							st.giveItems(DARINGS_RECEIPT, 1);
						}
						else if (cond == 3)
							htmltext = "30048-09.htm";
						else if (cond == 4)
						{
							htmltext = "30048-10.htm";
							st.takeItems(BAULROS_POTION, 1);
							st.giveItems(57,2466);
							st.giveItems(NECKLACE, 1);
							st.addExpAndSp(5672, 446);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case ROXXY:
						if (cond == 1)
						{
							htmltext = "30006-01.htm";
							st.set("cond", "2");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(DARINGS_LETTER, 1);
							st.giveItems(RAPUNZELS_KERCHIEF, 1);
						}
						else if (cond == 2)
							htmltext = "30006-02.htm";
						else if (cond > 2)
							htmltext = "30006-03.htm";
						break;
					
					case BAULRO:
						if (cond == 3)
						{
							htmltext = "30033-01.htm";
							st.set("cond", "4");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(DARINGS_RECEIPT, 1);
							st.giveItems(BAULROS_POTION, 1);
						}
						else if (cond == 4)
							htmltext = "30033-02.htm";
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
		new Q00001_LettersOfLove();
	}
}
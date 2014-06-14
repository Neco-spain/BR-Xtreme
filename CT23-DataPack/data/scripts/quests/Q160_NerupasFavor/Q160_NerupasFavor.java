/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q160_NerupasFavor;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Nerupas Favor (160)
 * @author Tryskell
 */
public class Q160_NerupasFavor extends Quest
{
	private static final String qn = "Q160_NerupasFavor";
	
	// Items
	private static final int SILVERY_SPIDERSILK = 1026;
	private static final int UNOREN_RECEIPT = 1027;
	private static final int CREAMEES_TICKET = 1028;
	private static final int NIGHTSHADE_LEAF = 1029;
	
	// Reward
	private static final int LESSER_HEALING_POTION = 1060;
	
	// NPCs
	private static final int NERUPA = 30370;
	private static final int UNOREN = 30147;
	private static final int CREAMEES = 30149;
	private static final int JULIA = 30152;
	
	public Q160_NerupasFavor()
	{
		super(160, qn, "Nerupas Favor");
		questItemIds = new int[] { SILVERY_SPIDERSILK, UNOREN_RECEIPT, CREAMEES_TICKET, NIGHTSHADE_LEAF };
		
		addStartNpc(NERUPA);
		addTalkId(NERUPA, UNOREN, CREAMEES, JULIA);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("30370-04.htm"))
		{
			st.setState(State.STARTED);
			st.set("cond", "1");
			st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
			st.giveItems(SILVERY_SPIDERSILK, 1);
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
		{
			return htmltext;
		}
		switch (st.getState())
		{
			case State.CREATED:
			{
				if (player.getRace() != Race.Elf)
					htmltext = "30370-00.htm";
				else if (player.getLevel() < 3)
					htmltext = "30370-02.htm";
				else
					htmltext = "30370-03.htm";
				break;
			}
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case NERUPA:
						if (cond < 4)
							htmltext = "30370-05.htm";
						else if (cond == 4)
						{
							htmltext = "30370-06.htm";
							st.takeItems(NIGHTSHADE_LEAF, 1);
							st.rewardItems(LESSER_HEALING_POTION, 5);
							st.addExpAndSp(1000, 0);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(false);
						}
						break;
					
					case UNOREN:
						if (cond == 1)
						{
							htmltext = "30147-01.htm";
							st.set("cond", "2");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(SILVERY_SPIDERSILK, 1);
							st.giveItems(UNOREN_RECEIPT, 1);
						}
						else if (cond == 2)
							htmltext = "30147-02.htm";
						else if (cond == 4)
							htmltext = "30147-03.htm";
						break;
					
					case CREAMEES:
						if (cond == 2)
						{
							htmltext = "30149-01.htm";
							st.set("cond", "3");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(UNOREN_RECEIPT, 1);
							st.giveItems(CREAMEES_TICKET, 1);
						}
						else if (cond == 3)
							htmltext = "30149-02.htm";
						else if (cond == 4)
							htmltext = "30149-03.htm";
						break;
					
					case JULIA:
						if (cond == 3)
						{
							htmltext = "30152-01.htm";
							st.set("cond", "4");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(CREAMEES_TICKET, 1);
							st.giveItems(NIGHTSHADE_LEAF, 1);
						}
						else if (cond == 4)
							htmltext = "30152-02.htm";
						break;
				}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q160_NerupasFavor();
	}
}

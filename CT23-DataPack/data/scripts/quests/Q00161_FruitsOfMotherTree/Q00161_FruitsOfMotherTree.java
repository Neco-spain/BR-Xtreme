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
package quests.Q00161_FruitsOfMotherTree;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Fruits Of MotherTree (161)
 * @author Tryskell
 */
public class Q00161_FruitsOfMotherTree extends Quest
{
	private static final String qn = "Q00161_FruitsOfMotherTree";
	
	// Items
    private static final int MOTHERTREE_FRUIT = 1036;
    private static final int ANDELLRIAS_LETTER = 1037;
	
    //NPCs
    private static final int ANDELLIA = 30362;
    private static final int THALIA = 30371;
    
	public Q00161_FruitsOfMotherTree()
	{
		super(161, qn, "Fruits Of MotherTree");
		registerQuestItems(MOTHERTREE_FRUIT,ANDELLRIAS_LETTER);
		addStartNpc(ANDELLIA);
		addTalkId(ANDELLIA, THALIA);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg();
		}
		String htmltext = event;
		
		switch (event)
		{
			case "30362-04.htm":
				st.startQuest();
				st.giveItems(ANDELLRIAS_LETTER, 1);
				break;
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
					htmltext = "30362-00.htm";
				else if (player.getLevel() < 3)
					htmltext = "30362-02.htm";
				else
					htmltext = "30362-03.htm";
				break;
			}
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ANDELLIA:
						if (cond == 1)
							htmltext = "30362-05.htm";
						else if (cond == 2)
						{
							htmltext = "30362-06.htm";
							st.takeItems(MOTHERTREE_FRUIT, 1);
							st.rewardItems(57, 1000);
							st.addExpAndSp(1000, 0);
							st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
							st.exitQuest(false);
						}
						break;
						
					case THALIA:
						if (cond == 1)
						{
							htmltext = "30371-01.htm";
							st.set("cond", "2");
							st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
							st.takeItems(ANDELLRIAS_LETTER, 1);
							st.giveItems(MOTHERTREE_FRUIT, 1);
						}
						else if (cond == 2)
							htmltext = "30371-02.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00161_FruitsOfMotherTree();
	}
}

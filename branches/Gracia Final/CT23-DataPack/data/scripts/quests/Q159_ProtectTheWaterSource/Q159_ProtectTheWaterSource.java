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
package quests.Q159_ProtectTheWaterSource;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Protect The Water Source (159)
 * @author BossForever
 */
public class Q159_ProtectTheWaterSource extends Quest
{
	private static final String qn = "Q159_ProtectTheWaterSource";
	
	// Items
	private static final int PLAGUE_DUST = 1035;
	private static final int HYACINTH_CHARM1 = 1071;
	private static final int HYACINTH_CHARM2 = 1072;
	// Monster
	private static final int PLAGUE_ZOMBIE = 27017;
	// NPC
	private static final int ASTERIOS = 30154;
	
	public Q159_ProtectTheWaterSource()
	{
		super(159, qn, "Protect The Water Source");
		questItemIds = new int[] { PLAGUE_DUST, HYACINTH_CHARM1, HYACINTH_CHARM2 };
		
		addStartNpc(ASTERIOS);
		addTalkId(ASTERIOS);
		addKillId(PLAGUE_ZOMBIE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		if (event.equalsIgnoreCase("1"))
			st.set("cond", "1");
		st.setState(State.STARTED);
		st.playSound(QuestSound.ITEMSOUND_QUEST_ACCEPT);
		if (st.getQuestItemsCount(HYACINTH_CHARM1) == 0)
			st.giveItems(HYACINTH_CHARM1, 1);
		htmltext = "30154-04.htm";
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
					htmltext = "30154-00.htm";
				else if (player.getLevel() >= 12)
					htmltext = "30154-03.htm";
				break;
			}
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				
			case State.STARTED:
				int cond = st.getInt("cond");
				{
					if (cond == 1)
					{
						htmltext = "30154-05.htm";
					}
					else if (cond == 2 && st.hasQuestItems(PLAGUE_DUST))
					{
						st.takeItems(PLAGUE_DUST, -1);
						st.takeItems(HYACINTH_CHARM1, -1);
						if (st.getQuestItemsCount(HYACINTH_CHARM2) == 0)
							st.giveItems(HYACINTH_CHARM2, 1);
						st.set("cond", "3");
						htmltext = "30154-06.htm";
					}
					else if (cond == 3)
					{
						htmltext = "30154-07.htm";
					}
					else if (cond == 4 && st.getQuestItemsCount(PLAGUE_DUST) >= 5)
					{
						st.takeItems(PLAGUE_DUST, -1);
						st.takeItems(HYACINTH_CHARM2, -1);
						st.giveItems(57, 18250);
						htmltext = "30154-08.htm";
						st.unset("cond");
						st.exitQuest(false);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					}
					break;
				}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState st = killer.getQuestState(qn);
		int cond = st.getInt("cond");
		
		if (cond == 1 && st.getRandom(100) < 40 && !st.hasQuestItems(PLAGUE_DUST))
		{
			st.giveItems(PLAGUE_DUST, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.set("cond", "2");
		}
		else if (cond == 3 && st.getRandom(100) < 40 && st.getQuestItemsCount(PLAGUE_DUST) < 5)
		{
			if (st.getQuestItemsCount(PLAGUE_DUST) == 4)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("cond", "4");
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.giveItems(PLAGUE_DUST, 1);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Q159_ProtectTheWaterSource();
	}
}

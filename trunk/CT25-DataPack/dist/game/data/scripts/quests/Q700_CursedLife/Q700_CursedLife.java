/*
 * Copyright (C) 2004-2013 L2J DataPack
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
package quests.Q700_CursedLife;

import java.util.HashMap;
import java.util.Map;

import quests.Q10273_GoodDayToFly.Q10273_GoodDayToFly;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.util.Rnd;

/**
 * Cursed Life (700)
 * @author xban1x
 */
public class Q700_CursedLife extends Quest
{
	// NPC
	private static final int ORBYU = 32560;
	// Monsters
	private static final int ROK = 25624;
	private static final Map<Integer, Integer[]> MONSTERS = new HashMap<>();
	//@formatter:off
	static
	{
		MONSTERS.put(22602, new Integer[] { 15, 139, 965}); // Mutant Bird lvl 1
		MONSTERS.put(22603, new Integer[] { 15, 143, 999}); // Mutant Bird lvl 2
		MONSTERS.put(25627, new Integer[] { 14, 125, 993}); // Mutant Bird lvl 3
		MONSTERS.put(22604, new Integer[] { 5, 94, 994}); // Dra Hawk lvl 1
		MONSTERS.put(22605, new Integer[] { 5, 99, 993}); // Dra Hawk lvl 2
		MONSTERS.put(25628, new Integer[] { 3, 73, 991}); // Dra Hawk lvl 3
	}
	//@formatter:on
	// Items
	private static final int SWALLOWED_BONES = 13874;
	private static final int SWALLOWED_STERNUM = 13873;
	private static final int SWALLOWED_SKULL = 13872;
	// Misc
	private static final int MIN_LVL = 75;
	private static final int SWALLOWED_BONES_ADENA = 500;
	private static final int SWALLOWED_STERNUM_ADENA = 5000;
	private static final int SWALLOWED_SKULL_ADENA = 50000;
	private static final int BONUS = 16670;
	
	public Q700_CursedLife(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(ORBYU);
		addTalkId(ORBYU);
		addKillId(ROK);
		for (int id : MONSTERS.keySet())
			super.addKillId(id);
		questItemIds = new int[] {SWALLOWED_BONES, SWALLOWED_STERNUM, SWALLOWED_SKULL};
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st != null)
		{
			switch (event)
			{
				case "32560-02.htm":
				{
					st = player.getQuestState(Q10273_GoodDayToFly.class.getSimpleName());
					htmltext = ((player.getLevel() < MIN_LVL) || (st == null) || (!st.isCompleted())) ? "32560-03.htm" : event;
					break;
				}
				case "32560-04.htm":
				case "32560-09.html":
				{
					htmltext = event;
					break;
				}
				case "32560-05.htm":
				{
					st.startQuest();
					htmltext = event;
					break;
				}
				case "32560-10.html":
				{
					st.exitQuest(true, true);
					htmltext = event;
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st != null)
		{
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = "32560-01.htm";
					break;
				}
				case State.STARTED:
				{
					long bones = st.getQuestItemsCount(SWALLOWED_BONES);
					long ribs = st.getQuestItemsCount(SWALLOWED_STERNUM);
					long skulls = st.getQuestItemsCount(SWALLOWED_SKULL);
					long sum = bones + ribs + skulls;
					if (sum > 0)
					{
						st.giveAdena(((bones * SWALLOWED_BONES_ADENA) + (ribs * SWALLOWED_STERNUM_ADENA) + (skulls * SWALLOWED_SKULL_ADENA) + (sum >= 10 ? BONUS : 0)), true);
						takeItems(player, -1, SWALLOWED_BONES, SWALLOWED_STERNUM, SWALLOWED_SKULL);
						htmltext = sum < 10 ? "32560-07.html" : "32560-08.html";
					}
					else
					{
						htmltext = "32560-06.html";
					}
					break;
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null)
		{
			if (npc.getNpcId() == ROK)
			{
				int amount = 0, chance = Rnd.get(1000);
				if (chance < 700)
				{
					amount = 1;
				}
				else if (chance < 885)
				{
					amount = 2;
				}
				else if (chance < 949)
				{
					amount = 3;
				}
				else if (chance < 966)
				{
					amount = Rnd.get(5) + 4;
				}
				else if (chance < 985)
				{
					amount = Rnd.get(9) + 4;
				}
				else if (chance < 993)
				{
					amount = Rnd.get(7) + 13;
				}
				else if (chance < 997)
				{
					amount = Rnd.get(15) + 9;
				}
				else if (chance < 999)
				{
					amount = Rnd.get(23) + 53;
				}
				else
				{
					amount = Rnd.get(49) + 76;
				}
				st.giveItems(SWALLOWED_BONES, amount);
				chance = Rnd.get(1000);
				if (chance < 520)
				{
					amount = 1;
				}
				else if (chance < 771)
				{
					amount = 2;
				}
				else if (chance < 836)
				{
					amount = 3;
				}
				else if (chance < 985)
				{
					amount = Rnd.get(2) + 4;
				}
				else if (chance < 995)
				{
					amount = Rnd.get(4) + 5;
				}
				else
				{
					amount = Rnd.get(8) + 6;
				}
				st.giveItems(SWALLOWED_STERNUM, amount);
				chance = Rnd.get(1000);
				if (chance < 185)
				{
					amount = Rnd.get(2) + 1;
				}
				else if (chance < 370)
				{
					amount = Rnd.get(6) + 2;
				}
				else if (chance < 570)
				{
					amount = Rnd.get(6) + 7;
				}
				else if (chance < 850)
				{
					amount = Rnd.get(6) + 12;
				}
				else
				{
					amount = Rnd.get(6) + 17;
				}
				st.giveItems(SWALLOWED_SKULL, amount);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				Integer[] chances = MONSTERS.get(npc.getNpcId());
				int chance = Rnd.get(1000);
				if (chance < chances[0])
				{
					st.giveItems(SWALLOWED_BONES, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if (chance < chances[1])
				{
					st.giveItems(SWALLOWED_STERNUM, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else if (chance < chances[2])
				{
					st.giveItems(SWALLOWED_SKULL, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new Q700_CursedLife(700, Q700_CursedLife.class.getSimpleName(), "Cursed Life");
	}
}

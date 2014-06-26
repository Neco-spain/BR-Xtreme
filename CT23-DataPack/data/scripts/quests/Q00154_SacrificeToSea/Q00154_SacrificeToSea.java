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
package quests.Q00154_SacrificeToSea;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Sacrifice To Sea (154)
 * @author BossForever
 */
public class Q00154_SacrificeToSea extends Quest
{
	// Items
	private static final int FOX_FUR_ID = 1032;
	private static final int FOX_FUR_YARN_ID = 1033;
	private static final int MAIDEN_DOLL_ID = 1034;
	private static final int EARING_ID = 113;
	
	// NPCs
	private static final int Rockswell = 30312;
	private static final int Cristel = 30051;
	private static final int Rolfe = 30055;
	
	//Kill Mobs
	private static final int Bearded_Keltir = 20481;
	private static final int Young_Keltir = 20545;
	
	public Q00154_SacrificeToSea()
	{
		super(154, Q00154_SacrificeToSea.class.getSimpleName(), "Sacrifice To Sea!");
		
		registerQuestItems(FOX_FUR_ID, FOX_FUR_YARN_ID, MAIDEN_DOLL_ID);
		addStartNpc(Rockswell);
		addTalkId(Cristel, Rolfe, Rockswell);
		addKillId(Bearded_Keltir, Young_Keltir);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg();
		}
		String htmltext = event;
		
		switch (event)
		{
			case "30312-04.htm":
				st.startQuest();
				st.set("id", "0");
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
		int npcId = npc.getNpcId();
		byte id = st.getState();
		if (npcId == Rockswell && st.getInt("cond") == 0 && st.getInt("onlyone") == 0)
		{
			if (player.getLevel() >= 2)
			{
				htmltext = "30312-03.htm";
				return htmltext;
			}
			else
			{
				htmltext = "30312-02.htm";
				st.exitQuest(true);
			}
		}
		else if (npcId == Rockswell && st.getInt("cond") == 0 && st.getInt("onlyone") == 1)
		{
			htmltext = getAlreadyCompletedMsg();
		}
		
		if (id == State.STARTED)
		{
			if (npcId == Rockswell && st.getInt("cond") >= 1 && (st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 0) && st.getQuestItemsCount(FOX_FUR_ID) < 10)
			{
				htmltext = "30312-05.htm";
			}
			else if (npcId == Rockswell && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_ID) >= 10)
			{
				htmltext = "30312-08.htm";
			}
			else if (npcId == Cristel && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_ID) < 10 && st.getQuestItemsCount(FOX_FUR_ID) > 0)
			{
				htmltext = "30051-01.htm";
			}
			else if (npcId == Cristel && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_ID) >= 10 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) < 10)
			{
				htmltext = "30051-02.htm";
				st.giveItems(FOX_FUR_YARN_ID, 1);
				st.takeItems(FOX_FUR_ID, st.getQuestItemsCount(FOX_FUR_ID));
				st.set("cond", "3");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if (npcId == Cristel && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) >= 1)
			{
				htmltext = "30051-03.htm";
			}
			else if (npcId == Cristel && st.getInt("cond") >= 1 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 1)
			{
				htmltext = "30051-04.htm";
			}
			else if (npcId == Rockswell && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) >= 1)
			{
				htmltext = "30312-06.htm";
			}
			else if (npcId == Rolfe && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) >= 1)
			{
				htmltext = "30055-01.htm";
				st.giveItems(MAIDEN_DOLL_ID, 1);
				st.takeItems(FOX_FUR_YARN_ID, st.getQuestItemsCount(FOX_FUR_YARN_ID));
				st.set("cond", "4");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if (npcId == Rolfe && st.getInt("cond") >= 1 && st.getQuestItemsCount(MAIDEN_DOLL_ID) >= 1)
			{
				htmltext = "30055-02.htm";
			}
			else if (npcId == Rolfe && st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0 && st.getQuestItemsCount(MAIDEN_DOLL_ID) == 0)
			{
				htmltext = "30055-03.htm";
			}
			else if (npcId == Rockswell && st.getInt("cond") >= 1 && st.getQuestItemsCount(MAIDEN_DOLL_ID) >= 1 && st.getInt("onlyone") == 0)
				if (st.getInt("id") != 154)
				{
					st.set("id", "154");
					htmltext = "30312-07.htm";
					st.giveItems(EARING_ID, 1);
					st.takeItems(MAIDEN_DOLL_ID, -1);
					st.addExpAndSp(1000, 0);
					st.set("cond", "0");
					st.exitQuest(false);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.set("onlyone", "1");
				}
		}
		
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getName());
		
		int npcId = npc.getNpcId();
		if (npcId == Bearded_Keltir)
			st.set("id", "0");
		if (st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_ID) < 10 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0)
		{
			if (st.getRandom(10) < 4)
			{
				st.giveItems(FOX_FUR_ID, 1);
			}
			if (st.getQuestItemsCount(FOX_FUR_ID) == 10)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("cond", "2");
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		else if (npcId == Young_Keltir)
			st.set("id", "0");
		if (st.getInt("cond") >= 1 && st.getQuestItemsCount(FOX_FUR_ID) < 10 && st.getQuestItemsCount(FOX_FUR_YARN_ID) == 0)
		{
			if (st.getRandom(10) < 4)
			{
				st.giveItems(FOX_FUR_ID, 1);
			}
			if (st.getQuestItemsCount(FOX_FUR_ID) == 10)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.set("cond", "2");
			}
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
	}
	
	public static void main(String[] args)
	{
		new Q00154_SacrificeToSea();
	}
}

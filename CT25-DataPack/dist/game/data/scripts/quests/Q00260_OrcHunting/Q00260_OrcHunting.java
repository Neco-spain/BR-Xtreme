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
package quests.Q00260_OrcHunting;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00281_HeadForTheHills.Q00281_HeadForTheHills;

/**
 * Orc Hunting (260)
 * @author xban1x
 */
public final class Q00260_OrcHunting extends Quest
{
	// NPC
	private static final int RAYEN = 30221;

	// Items
	private static final int ORC_AMULET = 1114;
	private static final int ORC_NECKLACE = 1115;

	// Monsters
	private static final Map<Integer, Integer> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(20468, ORC_AMULET); // Kaboo Orc
		MONSTERS.put(20469, ORC_AMULET); // Kaboo Orc Archer
		MONSTERS.put(20470, ORC_AMULET); // Kaboo Orc Grunt
		MONSTERS.put(20471, ORC_NECKLACE); // Kaboo Orc Fighter
		MONSTERS.put(20472, ORC_NECKLACE); // Kaboo Orc Fighter Leader
		MONSTERS.put(20473, ORC_NECKLACE); // Kaboo Orc Fighter Lieutenant
	}
	// Misc
	private static final int MIN_LVL = 6;

	private Q00260_OrcHunting(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(RAYEN);
		addTalkId(RAYEN);
		for (final int id : MONSTERS.keySet())
			super.addKillId(id);
		registerQuestItems(ORC_AMULET, ORC_NECKLACE);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = null;
		if (st == null)
			return htmltext;

		switch (event)
		{
			case "30221-04.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "30221-07.html":
			{
				st.exitQuest(true, true);
				htmltext = event;
				break;
			}
			case "30221-08.html":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && getRandom(10) > 4)
		{
			st.giveItems(MONSTERS.get(npc.getId()), 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getRace() == Race.Elf ? player.getLevel() >= MIN_LVL ? "30221-03.htm" : "30221-02.html" : "30221-01.html";
				break;
			}
			case State.STARTED:
			{
				if (hasAtLeastOneQuestItem(player, getRegisteredItemIds()))
				{
					final long amulets = st.getQuestItemsCount(ORC_AMULET);
					final long necklaces = st.getQuestItemsCount(ORC_NECKLACE);
					st.giveAdena(amulets * 12 + necklaces * 30 + (amulets + necklaces >= 10 ? 1000 : 0), true);
					takeItems(player, -1, getRegisteredItemIds());
					Q00281_HeadForTheHills.giveNewbieReward(player);
					htmltext = "30221-06.html";
				}
				else
					htmltext = "30221-05.html";
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00260_OrcHunting(260, Q00260_OrcHunting.class.getSimpleName(), "Orc Hunting");
	}
}

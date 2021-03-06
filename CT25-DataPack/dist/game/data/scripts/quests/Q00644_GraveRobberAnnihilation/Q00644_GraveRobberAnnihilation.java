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
package quests.Q00644_GraveRobberAnnihilation;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.holders.ItemHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Grave Robber Annihilation (644)
 * @author netvirus
 */
public final class Q00644_GraveRobberAnnihilation extends Quest
{
	// NPC
	private static final int KARUDA = 32017;
	// Item
	private static final int ORC_GOODS = 8088;
	// Misc
	private static final int MIN_LVL = 20;
	private static final int ORC_GOODS_REQUIRED_COUNT = 120;
	// Monsters
	private static final Map<Integer, Double> MONSTER_DROP_CHANCES = new HashMap<>();
	// Rewards
	private static final Map<String, ItemHolder> REWARDS = new HashMap<>();

	static
	{
		MONSTER_DROP_CHANCES.put(22003, 0.714); // Grave Robber Scout
		MONSTER_DROP_CHANCES.put(22004, 0.841); // Grave Robber Lookout
		MONSTER_DROP_CHANCES.put(22005, 0.778); // Grave Robber Ranger
		MONSTER_DROP_CHANCES.put(22006, 0.746); // Grave Robber Guard
		MONSTER_DROP_CHANCES.put(22008, 0.810); // Grave Robber Fighter

		REWARDS.put("varnish", new ItemHolder(1865, 30)); // Varnish
		REWARDS.put("animalskin", new ItemHolder(1867, 40)); // Animal Skin
		REWARDS.put("animalbone", new ItemHolder(1872, 40)); // Animal Bone
		REWARDS.put("charcoal", new ItemHolder(1871, 30)); // Charcoal
		REWARDS.put("coal", new ItemHolder(1870, 30)); // Coal
		REWARDS.put("ironore", new ItemHolder(1869, 30)); // Iron Ore
	}

	private Q00644_GraveRobberAnnihilation()
	{
		super(644, Q00644_GraveRobberAnnihilation.class.getSimpleName(), "Grave Robber Annihilation");
		addStartNpc(KARUDA);
		addTalkId(KARUDA);
		for (final int id : MONSTER_DROP_CHANCES.keySet())
			super.addKillId(id);
		registerQuestItems(ORC_GOODS);
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
			case "32017-03.htm":
			{
				if (st.isCreated())
				{
					st.startQuest();
					htmltext = event;
				}
				break;
			}
			case "32017-06.html":
			{
				if (st.isCond(2) && st.getQuestItemsCount(ORC_GOODS) >= ORC_GOODS_REQUIRED_COUNT)
					htmltext = event;
				break;
			}
			case "varnish":
			case "animalskin":
			case "animalbone":
			case "charcoal":
			case "coal":
			case "ironore":
			{
				if (st.isCond(2))
				{
					final ItemHolder reward = REWARDS.get(event);
					st.rewardItems(reward.getId(), reward.getCount());
					st.exitQuest(true, true);
					htmltext = "32017-07.html";
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState qs = getRandomPartyMemberState(killer, 1, 3, npc);
		if (qs != null && qs.giveItemRandomly(npc, ORC_GOODS, 1, ORC_GOODS_REQUIRED_COUNT, MONSTER_DROP_CHANCES.get(npc.getId()), true))
			qs.setCond(2, true);
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
				htmltext = player.getLevel() >= MIN_LVL ? "32017-01.htm" : "32017-02.htm";
				break;
			}
			case State.STARTED:
			{
				if (st.isCond(2) && st.getQuestItemsCount(ORC_GOODS) >= ORC_GOODS_REQUIRED_COUNT)
					htmltext = "32017-04.html";
				else
					htmltext = "32017-05.html";
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00644_GraveRobberAnnihilation();
	}
}
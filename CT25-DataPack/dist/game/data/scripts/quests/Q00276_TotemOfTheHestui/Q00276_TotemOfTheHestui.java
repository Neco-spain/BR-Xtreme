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
package quests.Q00276_TotemOfTheHestui;

import java.util.ArrayList;
import java.util.List;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.holders.ItemHolder;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import ct25.xtreme.gameserver.util.Util;
import quests.Q00261_CollectorsDream.Q00261_CollectorsDream;

/**
 * Totem of the Hestui (276)
 * @author xban1x
 */
public final class Q00276_TotemOfTheHestui extends Quest
{
	// Npc
	private static final int TANAPI = 30571;
	// Items
	private static final int KASHA_PARASITE = 1480;
	private static final int KASHA_CRYSTAL = 1481;
	// Monsters
	private static final int KASHA_BEAR = 20479;
	private static final int KASHA_BEAR_TOTEM = 27044;
	// Rewards
	private static final int[] REWARDS = new int[]
	{
		29,
		1500,
	};
	// Misc
	private static final List<ItemHolder> SPAWN_CHANCES = new ArrayList<>();
	private static final int MIN_LVL = 15;

	static
	{
		SPAWN_CHANCES.add(new ItemHolder(79, 100));
		SPAWN_CHANCES.add(new ItemHolder(69, 20));
		SPAWN_CHANCES.add(new ItemHolder(59, 15));
		SPAWN_CHANCES.add(new ItemHolder(49, 10));
		SPAWN_CHANCES.add(new ItemHolder(39, 2));
	}

	private Q00276_TotemOfTheHestui()
	{
		super(276, Q00276_TotemOfTheHestui.class.getSimpleName(), "Totem of the Hestui");
		addStartNpc(TANAPI);
		addTalkId(TANAPI);
		addKillId(KASHA_BEAR, KASHA_BEAR_TOTEM);
		registerQuestItems(KASHA_PARASITE, KASHA_CRYSTAL);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equals("30571-03.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isCond(1) && Util.checkIfInRange(1500, killer, npc, true))
			switch (npc.getId())
			{
				case KASHA_BEAR:
				{
					final long chance1 = st.getQuestItemsCount(KASHA_PARASITE);
					final int chance2 = getRandom(100);
					boolean chance3 = true;
					for (final ItemHolder spawnChance : SPAWN_CHANCES)
						if (chance1 >= spawnChance.getId() && chance2 <= spawnChance.getCount())
						{
							st.addSpawn(KASHA_BEAR_TOTEM);
							st.takeItems(KASHA_PARASITE, -1);
							chance3 = false;
							break;
						}
					if (chance3)
						st.giveItemRandomly(KASHA_PARASITE, 1, 0, 1, true);
					break;
				}
				case KASHA_BEAR_TOTEM:
				{
					if (st.giveItemRandomly(KASHA_CRYSTAL, 1, 1, 1, true))
						st.setCond(2);
					break;
				}
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
				htmltext = player.getRace() == Race.Orc ? player.getLevel() >= MIN_LVL ? "30571-02.htm" : "30571-01.htm" : "30571-00.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						htmltext = "30571-04.html";
						break;
					}
					case 2:
					{
						if (st.hasQuestItems(KASHA_CRYSTAL))
						{
							Q00261_CollectorsDream.giveNewbieReward(player);
							for (final int reward : REWARDS)
								st.rewardItems(reward, 1);
							st.exitQuest(true, true);
							htmltext = "30571-05.html";
						}
						break;
					}
				}
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00276_TotemOfTheHestui();
	}
}

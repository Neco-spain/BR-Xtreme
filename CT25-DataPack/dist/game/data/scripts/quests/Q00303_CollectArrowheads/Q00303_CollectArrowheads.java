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
package quests.Q00303_CollectArrowheads;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Collect Arrowheads (303)
 * @author ivantotov
 */
public final class Q00303_CollectArrowheads extends Quest
{
	// NPC
	private static final int MINIA = 30029;
	// Item
	private static final int ORCISH_ARROWHEAD = 963;
	// Misc
	private static final int MIN_LEVEL = 10;
	private static final int REQUIRED_ITEM_COUNT = 10;
	// Monster
	private static final int TUNATH_ORC_MARKSMAN = 20361;

	private Q00303_CollectArrowheads(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(MINIA);
		addTalkId(MINIA);
		addKillId(TUNATH_ORC_MARKSMAN);
		registerQuestItems(ORCISH_ARROWHEAD);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equals("30029-04.htm"))
		{
			st.startQuest();
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember != null)
		{
			final QuestState st = partyMember.getQuestState(getName());
			if (st.giveItemRandomly(npc, ORCISH_ARROWHEAD, 1, REQUIRED_ITEM_COUNT, 0.4, true))
				st.setCond(2);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				htmltext = player.getLevel() >= MIN_LEVEL ? "30029-03.htm" : "30029-02.htm";
				break;
			}
			case State.STARTED:
			{
				switch (st.getCond())
				{
					case 1:
					{
						if (st.getQuestItemsCount(ORCISH_ARROWHEAD) < REQUIRED_ITEM_COUNT)
							htmltext = "30029-05.html";
						break;
					}
					case 2:
					{
						if (st.getQuestItemsCount(ORCISH_ARROWHEAD) >= REQUIRED_ITEM_COUNT)
						{
							st.giveAdena(1000, true);
							st.addExpAndSp(2000, 0);
							st.exitQuest(true, true);
							htmltext = "30029-06.html";
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
		new Q00303_CollectArrowheads(303, Q00303_CollectArrowheads.class.getSimpleName(), "Collect Arrowheads");
	}
}
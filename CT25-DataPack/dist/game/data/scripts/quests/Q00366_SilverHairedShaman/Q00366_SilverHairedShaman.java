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
package quests.Q00366_SilverHairedShaman;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

/**
 * Silver Haired Shaman (366)
 * @author Adry_85, jurchiks
 */
public final class Q00366_SilverHairedShaman extends Quest
{
	// NPC
	private static final int DIETER = 30111;
	// Item
	private static final int SAIRONS_SILVER_HAIR = 5874;
	// Misc
	private static final int MIN_LEVEL = 48;
	// Mobs
	private static final Map<Integer, Integer> MOBS = new HashMap<>();
	static
	{
		MOBS.put(20986, 80); // saitnn
		MOBS.put(20987, 73); // saitnn_doll
		MOBS.put(20988, 80); // saitnn_puppet
	}

	private Q00366_SilverHairedShaman()
	{
		super(366, Q00366_SilverHairedShaman.class.getSimpleName(), "Silver Haired Shaman");
		addStartNpc(DIETER);
		addTalkId(DIETER);
		for (final int id : MOBS.keySet())
			super.addKillId(id);
		registerQuestItems(SAIRONS_SILVER_HAIR);
	}

	@Override
	public boolean checkPartyMember(final L2PcInstance member, final L2Npc npc)
	{
		final QuestState qs = member.getQuestState(getName());
		return qs != null && qs.isStarted();
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = getQuestState(player, false);
		if (st == null)
			return null;

		String htmltext = null;
		switch (event)
		{
			case "30111-02.htm":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "30111-05.html":
			{
				st.exitQuest(true, true);
				htmltext = event;
				break;
			}
			case "30111-06.html":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		if (getRandom(100) < MOBS.get(npc.getId()))
		{
			final L2PcInstance luckyPlayer = getRandomPartyMember(player, npc);
			if (luckyPlayer != null)
				giveItemRandomly(luckyPlayer, npc, SAIRONS_SILVER_HAIR, 1, 0, 1.0, true);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		if (st.isCreated())
			htmltext = player.getLevel() >= MIN_LEVEL ? "30111-01.htm" : "30111-03.html";
		else if (st.isStarted())
			if (hasQuestItems(player, SAIRONS_SILVER_HAIR))
			{
				final long itemCount = getQuestItemsCount(player, SAIRONS_SILVER_HAIR);
				giveAdena(player, itemCount * 500 + 29000, true);
				takeItems(player, SAIRONS_SILVER_HAIR, -1);
				htmltext = "30111-04.html";
			}
			else
				htmltext = "30111-07.html";
		return htmltext;
	}

	public static void main(final String args[])
	{
		new Q00366_SilverHairedShaman();
	}
}

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
package quests.Q00691_MatrasSuspiciousRequest;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Matras' Suspicious Request (691)
 * @author GKR
 */
public final class Q00691_MatrasSuspiciousRequest extends Quest
{
	// NPC
	private static final int MATRAS = 32245;
	// Items
	private static final int RED_GEM = 10372;
	private static final int DYNASTY_SOUL_II = 10413;
	// Reward
	private static final Map<Integer, Integer> REWARD_CHANCES = new HashMap<>();
	static
	{
		REWARD_CHANCES.put(22363, 890);
		REWARD_CHANCES.put(22364, 261);
		REWARD_CHANCES.put(22365, 560);
		REWARD_CHANCES.put(22366, 560);
		REWARD_CHANCES.put(22367, 190);
		REWARD_CHANCES.put(22368, 129);
		REWARD_CHANCES.put(22369, 210);
		REWARD_CHANCES.put(22370, 787);
		REWARD_CHANCES.put(22371, 257);
		REWARD_CHANCES.put(22372, 656);
	}
	// Misc
	private static final int MIN_LEVEL = 76;

	private Q00691_MatrasSuspiciousRequest(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(MATRAS);
		addTalkId(MATRAS);
		for (final int id : REWARD_CHANCES.keySet())
			super.addKillId(id);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = null;
		switch (event)
		{
			case "32245-02.htm":
			case "32245-11.html":
				htmltext = event;
				break;
			case "32245-04.htm":
				st.startQuest();
				htmltext = event;
				break;
			case "take_reward":
				if (st.isStarted())
				{
					final int gemsCount = st.getInt("submitted_gems");
					if (gemsCount >= 744)
					{
						st.set("submitted_gems", Integer.toString(gemsCount - 744));
						st.giveItems(DYNASTY_SOUL_II, 1);
						htmltext = "32245-09.html";
					}
					else
						htmltext = getHtm(player.getHtmlPrefix(), "32245-10.html").replace("%itemcount%", st.get("submitted_gems"));
				}
				break;
			case "32245-08.html":
				if (st.isStarted())
				{
					final int submittedCount = st.getInt("submitted_gems");
					final int broughtCount = (int) st.getQuestItemsCount(RED_GEM);
					final int finalCount = submittedCount + broughtCount;
					st.takeItems(RED_GEM, broughtCount);
					st.set("submitted_gems", Integer.toString(finalCount));
					htmltext = getHtm(player.getHtmlPrefix(), "32245-08.html").replace("%itemcount%", Integer.toString(finalCount));
				}
				break;
			case "32245-12.html":
				if (st.isStarted())
				{
					st.giveAdena(st.getInt("submitted_gems") * 10000, true);
					st.exitQuest(true, true);
					htmltext = event;
				}
				break;
		}
		return htmltext;
	}

	@Override
	public final String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance pl = getRandomPartyMember(player, 1);
		if (pl == null)
			return super.onKill(npc, player, isPet);

		final QuestState st = pl.getQuestState(getName());
		int chance = (int) (Config.RATE_QUEST_DROP * REWARD_CHANCES.get(npc.getId()));
		final int numItems = Math.max(chance / 1000, 1);
		chance = chance % 1000;
		if (getRandom(1000) <= chance)
		{
			st.giveItems(RED_GEM, numItems);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public final String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
				htmltext = player.getLevel() >= MIN_LEVEL ? "32245-01.htm" : "32245-03.html";
				break;
			case State.STARTED:
				if (st.hasQuestItems(RED_GEM))
					htmltext = "32245-05.html";
				else if (st.getInt("submitted_gems") > 0)
					htmltext = getHtm(player.getHtmlPrefix(), "32245-07.html").replace("%itemcount%", st.get("submitted_gems"));
				else
					htmltext = "32245-06.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00691_MatrasSuspiciousRequest(691, Q00691_MatrasSuspiciousRequest.class.getSimpleName(), "Matras' Suspicious Request");
	}
}
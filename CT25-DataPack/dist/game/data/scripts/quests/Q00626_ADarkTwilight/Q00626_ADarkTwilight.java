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
package quests.Q00626_ADarkTwilight;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * A Dark Twilight (626)<br>
 * Original Jython script by disKret.
 * @author Citizen
 */
public class Q00626_ADarkTwilight extends Quest
{
	// NPCs
	private static final int HIERARCH = 31517;
	// Items
	private static final int BLOOD_OF_SAINT = 7169;
	// Monsters
	private static final Map<Integer, Integer> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(21520, 641); // Eye of Splendor
		MONSTERS.put(21523, 648); // Flash of Splendor
		MONSTERS.put(21524, 692); // Blade of Splendor
		MONSTERS.put(21525, 710); // Blade of Splendor
		MONSTERS.put(21526, 772); // Wisdom of Splendor
		MONSTERS.put(21529, 639); // Soul of Splendor
		MONSTERS.put(21530, 683); // Victory of Splendor
		MONSTERS.put(21531, 767); // Punishment of Splendor
		MONSTERS.put(21532, 795); // Shout of Splendor
		MONSTERS.put(21535, 802); // Signet of Splendor
		MONSTERS.put(21536, 774); // Crown of Splendor
		MONSTERS.put(21539, 848); // Wailing of Splendor
		MONSTERS.put(21540, 880); // Wailing of Splendor
		MONSTERS.put(21658, 790); // Punishment of Splendor
	}
	// Misc
	private static final int MIN_LEVEL_REQUIRED = 60;
	private static final int ITEMS_COUNT_REQUIRED = 300;
	// Rewards
	private static final int ADENA_COUNT = 100000;
	private static final int XP_COUNT = 162773;
	private static final int SP_COUNT = 12500;

	private Q00626_ADarkTwilight(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(HIERARCH);
		addTalkId(HIERARCH);
		for (final int id : MONSTERS.keySet())
			super.addKillId(id);
		registerQuestItems(BLOOD_OF_SAINT);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;
		String htmltext = event;
		switch (event)
		{
			case "31517-05.html":
				break;
			case "31517-02.htm":
				st.startQuest();
				break;
			case "Exp":
				if (st.getQuestItemsCount(BLOOD_OF_SAINT) < ITEMS_COUNT_REQUIRED)
					return "31517-06.html";
				st.addExpAndSp(XP_COUNT, SP_COUNT);
				st.exitQuest(true, true);
				htmltext = "31517-07.html";
				break;
			case "Adena":
				if (st.getQuestItemsCount(BLOOD_OF_SAINT) < ITEMS_COUNT_REQUIRED)
					return "31517-06.html";
				st.giveAdena(ADENA_COUNT, true);
				st.exitQuest(true, true);
				htmltext = "31517-07.html";
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(killer, 1);
		if (partyMember != null)
		{
			final QuestState st = partyMember.getQuestState(getName());
			final float chance = MONSTERS.get(npc.getId()) * Config.RATE_QUEST_DROP;
			if (getRandom(1000) < chance)
			{
				st.giveItems(BLOOD_OF_SAINT, 1);
				if (st.getQuestItemsCount(BLOOD_OF_SAINT) < ITEMS_COUNT_REQUIRED)
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				else
					st.setCond(2, true);
			}
		}
		return super.onKill(npc, killer, isPet);
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
				htmltext = player.getLevel() >= MIN_LEVEL_REQUIRED ? "31517-01.htm" : "31517-00.htm";
				break;
			case State.STARTED:
				switch (st.getCond())
				{
					case 1:
						htmltext = "31517-03.html";
						break;
					case 2:
						htmltext = "31517-04.html";
						break;
				}
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00626_ADarkTwilight(626, Q00626_ADarkTwilight.class.getSimpleName(), "A Dark Twilight");
	}
}
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
package quests.Q00688_DefeatTheElrokianRaiders;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Defeat the Elrokian Raiders! (688)
 * @author Adry_85
 */
public class Q00688_DefeatTheElrokianRaiders extends Quest
{
	// NPCs
	private static final int ELROKI = 22214;
	private static final int DINN = 32105;
	// Item
	private static final int DINOSAUR_FANG_NECKLACE = 8785;
	// Misc
	private static final int MIN_LEVEL = 75;
	private static final int DROP_RATE = 448;

	public Q00688_DefeatTheElrokianRaiders(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DINN);
		addTalkId(DINN);
		addKillId(ELROKI);
		registerQuestItems(DINOSAUR_FANG_NECKLACE);
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
			case "32105-02.htm":
			case "32105-10.html":
			{
				htmltext = event;
				break;
			}
			case "32105-03.html":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "32105-06.html":
			{
				if (st.hasQuestItems(DINOSAUR_FANG_NECKLACE))
				{
					st.giveAdena(3000 * st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE), true);
					st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
					htmltext = event;
				}
				break;
			}
			case "donation":
			{
				if (st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE) < 100)
					htmltext = "32105-07.html";
				else
				{
					if (getRandom(1000) < 500)
					{
						st.giveAdena(450000, true);
						htmltext = "32105-08.html";
					}
					else
					{
						st.giveAdena(150000, true);
						htmltext = "32105-09.html";
					}
					st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
				}
				break;
			}
			case "32105-11.html":
			{
				if (st.hasQuestItems(DINOSAUR_FANG_NECKLACE))
					st.giveAdena(3000 * st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE), true);
				st.exitQuest(true, true);
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return super.onKill(npc, player, isPet);

		final QuestState st = partyMember.getQuestState(getName());

		final float chance = DROP_RATE * Config.RATE_QUEST_DROP;
		if (getRandom(1000) < chance)
		{
			st.rewardItems(DINOSAUR_FANG_NECKLACE, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
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
				htmltext = player.getLevel() >= MIN_LEVEL ? "32105-01.htm" : "32105-04.html";
				break;
			}
			case State.STARTED:
			{
				htmltext = st.hasQuestItems(DINOSAUR_FANG_NECKLACE) ? "32105-05.html" : "32105-12.html";
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String args[])
	{
		new Q00688_DefeatTheElrokianRaiders(688, Q00688_DefeatTheElrokianRaiders.class.getSimpleName(), "Defeat the Elrokian Raiders!");
	}
}

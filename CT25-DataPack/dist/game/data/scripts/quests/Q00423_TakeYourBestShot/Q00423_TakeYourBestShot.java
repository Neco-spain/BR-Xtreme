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
package quests.Q00423_TakeYourBestShot;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00249_PoisonedPlainsOfTheLizardmen.Q00249_PoisonedPlainsOfTheLizardmen;

/**
 * Take Your Best Shot (423)
 * @author Gnacik
 * @version 2010-06-26 Based on official server Franz
 */
public class Q00423_TakeYourBestShot extends Quest
{
	// NPCs
	private static final int BATRACOS = 32740;
	private static final int JOHNNY = 32744;

	// Monster
	private static final int TANTA_GUARD = 18862;

	// Item
	private static final int SEER_UGOROS_PASS = 15496;

	// Misc
	private static final int MIN_LEVEL = 82;

	public Q00423_TakeYourBestShot(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(JOHNNY, BATRACOS);
		addTalkId(JOHNNY, BATRACOS);
		addFirstTalkId(BATRACOS);
		addKillId(TANTA_GUARD);
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
			case "32740.html":
			case "32740-01.html":
			case "32744-02.html":
			case "32744-03.htm":
				break;
			case "32744-04.htm":
				st.startQuest();
				break;
			case "32744-quit.html":
				st.exitQuest(true);
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		if (npc.isInsideRadius(96782, 85918, 0, 100, false, true))
			return "32740-ugoros.html";
		return "32740.html";
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && st.isCond(1))
			st.setCond(2, true);
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		switch (npc.getId())
		{
			case JOHNNY:
				switch (st.getState())
				{
					case State.CREATED:
						final QuestState _prev = player.getQuestState(Q00249_PoisonedPlainsOfTheLizardmen.class.getSimpleName());
						if (_prev != null && _prev.isCompleted() && player.getLevel() >= MIN_LEVEL)
							htmltext = st.hasQuestItems(SEER_UGOROS_PASS) ? "32744-07.htm" : "32744-01.htm";
						else
							htmltext = "32744-00.htm";
						break;
					case State.STARTED:
						if (st.isCond(1))
							htmltext = "32744-05.html";
						else if (st.isCond(2))
							htmltext = "32744-06.html";
						break;
				}
				break;
			case BATRACOS:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = st.hasQuestItems(SEER_UGOROS_PASS) ? "32740-05.html" : "32740-00.html";
						break;
					case State.STARTED:
						if (st.isCond(1))
							htmltext = "32740-02.html";
						else if (st.isCond(2))
						{
							st.giveItems(SEER_UGOROS_PASS, 1);
							st.exitQuest(true, true);
							htmltext = "32740-04.html";
						}
						break;
				}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00423_TakeYourBestShot(423, Q00423_TakeYourBestShot.class.getSimpleName(), "Take Your Best Shot!");
	}
}
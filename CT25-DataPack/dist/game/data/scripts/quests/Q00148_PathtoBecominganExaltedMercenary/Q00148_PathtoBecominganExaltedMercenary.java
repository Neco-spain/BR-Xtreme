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
package quests.Q00148_PathtoBecominganExaltedMercenary;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00147_PathtoBecominganEliteMercenary.Q00147_PathtoBecominganEliteMercenary;

/**
 * Path to Becoming an Exalted Mercenary (148)
 * @author Gnacik
 * @version 2010-09-30 Based on official server Franz
 */
public class Q00148_PathtoBecominganExaltedMercenary extends Quest
{
	// NPCs
	private static final int[] MERC =
	{
		36481,
		36482,
		36483,
		36484,
		36485,
		36486,
		36487,
		36488,
		36489
	};
	// Items
	private static final int ELITE_CERTIFICATE = 13767;
	private static final int TOP_ELITE_CERTIFICATE = 13768;

	public Q00148_PathtoBecominganExaltedMercenary(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(MERC);
		addTalkId(MERC);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		if (event.equalsIgnoreCase("exalted-00b.htm"))
			st.giveItems(ELITE_CERTIFICATE, 1);
		else if (event.equalsIgnoreCase("exalted-03.htm"))
			st.startQuest();
		return htmltext;
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
				final QuestState _prev = player.getQuestState(Q00147_PathtoBecominganEliteMercenary.class.getSimpleName());
				if (player.getClan() != null && player.getClan().getHasCastle() > 0)
					htmltext = "castle.htm";
				else if (st.hasQuestItems(ELITE_CERTIFICATE))
					htmltext = "exalted-01.htm";
				else if (_prev != null && _prev.isCompleted())
					htmltext = "exalted-00a.htm";
				else
					htmltext = "exalted-00.htm";
				break;
			case State.STARTED:
				if (st.getCond() < 4)
					htmltext = "exalted-04.htm";
				else if (st.isCond(4))
				{
					st.takeItems(ELITE_CERTIFICATE, -1);
					st.giveItems(TOP_ELITE_CERTIFICATE, 1);
					st.exitQuest(false);
					htmltext = "exalted-05.htm";
				}
				break;
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg(player);
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00148_PathtoBecominganExaltedMercenary(148, Q00148_PathtoBecominganExaltedMercenary.class.getSimpleName(), "Path to Becoming an Exalted Mercenary");
	}
}

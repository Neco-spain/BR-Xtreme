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
package quests.Q00651_RunawayYouth;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Runaway Youth (651)
 * @author malyelfik
 */
public class Q00651_RunawayYouth extends Quest
{
	// NPCs
	private static final int BATIDAE = 31989;
	private static final int IVAN = 32014;
	// Item
	private static final int SOE = 736;
	// Misc
	private static final int MIN_LEVEL = 26;

	public Q00651_RunawayYouth(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(IVAN);
		addTalkId(BATIDAE, IVAN);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = null;
		if (event.equals("32014-03.html"))
			htmltext = event;
		else if (event.equals("32014-04.htm"))
		{
			if (!st.hasQuestItems(SOE))
				return "32014-05.htm";
			st.startQuest();
			st.takeItems(SOE, 1);
			npc.deleteMe();
			htmltext = event;
		}
		return htmltext;
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return htmltext;

		switch (npc.getId())
		{
			case IVAN:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() >= MIN_LEVEL ? "32014-01.htm" : "32014-01a.htm";
						break;
					case State.STARTED:
						htmltext = "32014-02.html";
						break;
				}
				break;
			case BATIDAE:
				if (st.isStarted())
				{
					st.giveAdena(2883, true);
					st.exitQuest(true, true);
					htmltext = "31989-01.html";
				}
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00651_RunawayYouth(651, Q00651_RunawayYouth.class.getSimpleName(), "Runaway Youth");
	}
}
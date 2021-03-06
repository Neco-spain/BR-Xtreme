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
package quests.Q00009_IntoTheCityOfHumans;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Into the City of Humans (9)
 * @author malyelfik
 */
public class Q00009_IntoTheCityOfHumans extends Quest
{
	// NPCs
	private static final int PETUKAI = 30583;
	private static final int TANAPI = 30571;
	private static final int TAMIL = 30576;
	// Items
	private static final int SCROLL_OF_ESCAPE_GIRAN = 7559;
	private static final int MARK_OF_TRAVELER = 7570;
	// Misc
	private static final int MIN_LEVEL = 3;

	private Q00009_IntoTheCityOfHumans(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(PETUKAI);
		addTalkId(PETUKAI, TANAPI, TAMIL);
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
			case "30583-04.htm":
				st.startQuest();
				break;
			case "30576-02.html":
				st.giveItems(MARK_OF_TRAVELER, 1);
				st.giveItems(SCROLL_OF_ESCAPE_GIRAN, 1);
				st.exitQuest(false, true);
				break;
			case "30571-02.html":
				st.setCond(2, true);
				break;
			default:
				htmltext = null;
				break;
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
			case PETUKAI:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() >= MIN_LEVEL ? player.getRace() == Race.Orc ? "30583-01.htm" : "30583-02.html" : "30583-03.html";
						break;
					case State.STARTED:
						if (st.isCond(1))
							htmltext = "30583-05.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case TANAPI:
				if (st.isStarted())
					htmltext = st.isCond(1) ? "30571-01.html" : "30571-03.html";
				break;
			case TAMIL:
				if (st.isStarted() && st.isCond(2))
					htmltext = "30576-01.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00009_IntoTheCityOfHumans(9, Q00009_IntoTheCityOfHumans.class.getSimpleName(), "Into the City of Humans");
	}
}
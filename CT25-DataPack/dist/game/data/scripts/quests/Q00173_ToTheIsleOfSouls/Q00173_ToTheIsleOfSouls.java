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
package quests.Q00173_ToTheIsleOfSouls;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00172_NewHorizons.Q00172_NewHorizons;

/**
 * To the Isle of Souls (173)
 * @author malyelfik
 */
public class Q00173_ToTheIsleOfSouls extends Quest
{
	// NPCs
	private static final int GALLADUCCI = 30097;
	private static final int GENTLER = 30094;

	// Items
	private static final int GALLADUCCIS_ORDER = 7563;
	private static final int MAGIC_SWORD_HILT = 7568;
	private static final int MARK_OF_TRAVELER = 7570;
	private static final int SCROLL_OF_ESCAPE_KAMAEL_VILLAGE = 9716;

	public Q00173_ToTheIsleOfSouls(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(GALLADUCCI);
		addTalkId(GALLADUCCI, GENTLER);

		registerQuestItems(GALLADUCCIS_ORDER, MAGIC_SWORD_HILT);
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
			case "30097-03.htm":
				st.startQuest();
				st.giveItems(GALLADUCCIS_ORDER, 1);
				break;
			case "30097-06.html":
				st.giveItems(SCROLL_OF_ESCAPE_KAMAEL_VILLAGE, 1);
				st.takeItems(MARK_OF_TRAVELER, 1);
				st.exitQuest(false, true);
				break;
			case "30094-02.html":
				st.setCond(2, true);
				st.takeItems(GALLADUCCIS_ORDER, -1);
				st.giveItems(MAGIC_SWORD_HILT, 1);
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
			case GALLADUCCI:
				switch (st.getState())
				{
					case State.CREATED:
						final QuestState qs = player.getQuestState(Q00172_NewHorizons.class.getSimpleName());
						htmltext = qs != null && qs.isCompleted() && player.getRace() == Race.Kamael && st.hasQuestItems(MARK_OF_TRAVELER) ? "30097-01.htm" : "30097-02.htm";
						break;
					case State.STARTED:
						htmltext = st.isCond(1) ? "30097-04.html" : "30097-05.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case GENTLER:
				if (st.isStarted())
					htmltext = st.isCond(1) ? "30094-01.html" : "30094-03.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00173_ToTheIsleOfSouls(173, Q00173_ToTheIsleOfSouls.class.getSimpleName(), "To the Isle of Souls");
	}
}
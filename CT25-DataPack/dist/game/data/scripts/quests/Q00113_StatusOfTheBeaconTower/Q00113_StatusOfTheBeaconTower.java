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
package quests.Q00113_StatusOfTheBeaconTower;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Status of the Beacon Tower (113)<br>
 * Original Jython script by Kerberos.
 * @author malyelfik
 */
public class Q00113_StatusOfTheBeaconTower extends Quest
{
	// NPCs
	private static final int MOIRA = 31979;
	private static final int TORRANT = 32016;
	// Items
	private static final int FLAME_BOX = 14860;
	private static final int FIRE_BOX = 8086;

	public Q00113_StatusOfTheBeaconTower(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(MOIRA);
		addTalkId(MOIRA, TORRANT);
		registerQuestItems(FIRE_BOX, FLAME_BOX);
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
			case "31979-02.htm":
				st.startQuest();
				st.giveItems(FLAME_BOX, 1);
				break;
			case "32016-02.html":
				if (st.hasQuestItems(FIRE_BOX))
				{
					st.giveAdena(21578, true);
					st.addExpAndSp(76665, 5333);
				}
				else
				{
					st.giveAdena(154800, true);
					st.addExpAndSp(619300, 44200);
				}
				st.exitQuest(false, true);
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
			case MOIRA:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = player.getLevel() >= 80 ? "31979-01.htm" : "31979-00.htm";
						break;
					case State.STARTED:
						htmltext = "31979-03.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg(player);
						break;
				}
				break;
			case TORRANT:
				if (st.isStarted())
					htmltext = "32016-01.html";
				break;
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00113_StatusOfTheBeaconTower(113, Q00113_StatusOfTheBeaconTower.class.getSimpleName(), "Status of the Beacon Tower");
	}
}
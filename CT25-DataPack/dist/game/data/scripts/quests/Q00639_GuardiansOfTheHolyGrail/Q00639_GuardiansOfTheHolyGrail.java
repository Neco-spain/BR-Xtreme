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
package quests.Q00639_GuardiansOfTheHolyGrail;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;

/**
 * Guardians of the Holy Grail (639)<br>
 * NOTE: This quest is no longer available since Freya(CT2.5)
 * @author corbin12
 */
public final class Q00639_GuardiansOfTheHolyGrail extends Quest
{
	// NPC
	private static final int DOMINIC = 31350;

	private Q00639_GuardiansOfTheHolyGrail(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(DOMINIC);
		addTalkId(DOMINIC);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null)
			st.exitQuest(true);
		return "31350-01.html";
	}

	public static void main(final String[] args)
	{
		new Q00639_GuardiansOfTheHolyGrail(639, Q00639_GuardiansOfTheHolyGrail.class.getSimpleName(), "Guardians of the Holy Grail");
	}
}
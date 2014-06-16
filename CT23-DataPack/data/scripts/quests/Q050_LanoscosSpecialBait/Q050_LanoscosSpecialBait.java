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
package quests.Q050_LanoscosSpecialBait;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Lanosco's Special Bait (50)<br>
 * Original Jython script by Kilkenny.
 * @author nonom
 */
public class Q050_LanoscosSpecialBait extends Quest
{
	// NPCs
	private static final int LANOSCO = 31570;
	private static final int SINGING_WIND = 21026;
	// Items
	private static final int ESSENCE_OF_WIND = 7621;
	private static final int WIND_FISHING_LURE = 7610;
	
	public Q050_LanoscosSpecialBait()
	{
		super(50, Q050_LanoscosSpecialBait.class.getSimpleName(), "Lanosco's Special Bait");
		addStartNpc(LANOSCO);
		addTalkId(LANOSCO);
		addKillId(SINGING_WIND);
		registerQuestItems(ESSENCE_OF_WIND);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg();
		}
		
		String htmltext = event;
		
		switch (event)
		{
			case "31570-03.htm":
				st.startQuest();
				break;
			case "31570-07.htm":
				if ((st.isCond(2)) && (st.getQuestItemsCount(ESSENCE_OF_WIND) >= 100))
				{
					htmltext = "31570-06.htm";
					st.giveItems(WIND_FISHING_LURE, 4);
					st.exitQuest(false);
				}
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
		{
			return null;
		}
		
		final QuestState st = partyMember.getQuestState(getName());
		
		if (st.getQuestItemsCount(ESSENCE_OF_WIND) < 100)
		{
			float chance = 33 * Config.RATE_QUEST_DROP;
			if (getRandom(100) < chance)
			{
				st.rewardItems(ESSENCE_OF_WIND, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		
		if (st.getQuestItemsCount(ESSENCE_OF_WIND) >= 100)
		{
			st.setCond(2, true);
		}
		
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (st.getState())
		{
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
			case State.CREATED:
				htmltext = (player.getLevel() >= 27) ? "31570-01.htm" : "31570-02.htm";
				break;
			case State.STARTED:
				htmltext = (st.isCond(1)) ? "31570-05.htm" : "31570-04.htm";
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q050_LanoscosSpecialBait();
	}
}

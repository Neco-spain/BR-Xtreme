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
package quests.Q052_WilliesSpecialBait;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Willie's Special Bait (52)<br>
 * Original Jython script by Kilkenny.
 * @author nonom
 */
public class Q052_WilliesSpecialBait extends Quest
{
	// NPCs
	private static final int WILLIE = 31574;
	// Mobs
	private static final int TARLK_BASILISK = 20573;
	// Items
	private static final int TARLK_EYE = 7623;
	private static final int EARTH_FISHING_LURE = 7612;
	
	public Q052_WilliesSpecialBait()
	{
		super(52, Q052_WilliesSpecialBait.class.getSimpleName(), "Willie's Special Bait");
		addStartNpc(WILLIE);
		addTalkId(WILLIE);
		addKillId(TARLK_BASILISK);
		registerQuestItems(TARLK_EYE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg();
		}
		
		String htmltext = event;
		switch (event)
		{
			case "31574-03.htm":
				st.startQuest();
				break;
			case "31574-07.htm":
				if (st.isCond(2) && (st.getQuestItemsCount(TARLK_EYE) >= 100))
				{
					htmltext = "31574-06.htm";
					st.giveItems(EARTH_FISHING_LURE, 4);
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
		if (st.getQuestItemsCount(TARLK_EYE) < 100)
		{
			float chance = 33 * Config.RATE_QUEST_DROP;
			if (getRandom(100) < chance)
			{
				st.rewardItems(TARLK_EYE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		
		if (st.getQuestItemsCount(TARLK_EYE) >= 100)
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
				htmltext = (player.getLevel() >= 48) ? "31574-01.htm" : "31574-02.htm";
				break;
			case State.STARTED:
				htmltext = (st.isCond(1)) ? "31574-05.htm" : "31574-04.htm";
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q052_WilliesSpecialBait();
	}
}

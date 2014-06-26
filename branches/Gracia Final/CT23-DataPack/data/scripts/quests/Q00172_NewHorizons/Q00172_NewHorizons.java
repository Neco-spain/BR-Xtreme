/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00172_NewHorizons;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * New Horizons (172)
 * @author BossForever
 */
public class Q00172_NewHorizons extends Quest
{
	// NPCs
	private static final int ZENYA = 32140;
	private static final int RAGARA = 32163;
	
	// Items
	private static final int SCROLL_OF_ESCAPE_GIRAN = 7559;
	private static final int MARK_OF_TRAVELER = 7570;
	
	public Q00172_NewHorizons()
	{
		super(172, Q00172_NewHorizons.class.getSimpleName(), "New Horizons");
		addStartNpc(ZENYA);
		addTalkId(ZENYA, RAGARA);
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
			case "32140-03.htm":
				st.startQuest();
				break;
				
			case "32163-02.htm":
				st.unset("cond");
				st.giveItems(SCROLL_OF_ESCAPE_GIRAN,1);
				st.giveItems(MARK_OF_TRAVELER,1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(true);
				break;
		}
		return htmltext;
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
			case State.CREATED:
				if (player.getLevel() >= 3 && player.getRace() == Race.Kamael)
					htmltext = "32140-01.htm";
				else
					htmltext = "32140-02.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
					case ZENYA:
						if (cond == 1)
							htmltext = "32140-04.htm";
						break;
					
					case RAGARA:
						if (cond == 1)
							htmltext = "32163-01.htm";
						
						break;
				}
				break;
			
			case State.COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00172_NewHorizons();
	}
}
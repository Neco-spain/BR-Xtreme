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
package quests.Q00047_IntoTheDarkForest;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.base.Race;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Into The Dark Forest (47)
 * Original Jython script by CubicVirtuoso.
 * @author BossForever 
 */
public class Q00047_IntoTheDarkForest extends Quest
{
	// NPCs
    private static final int GALLADUCCI = 30097;
    private static final int GENTLER = 30094;
    private static final int SANDRA = 30090;
    private static final int DUSTIN = 30116;
	
	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_1 = 7563;
	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_2 = 7564;
	private static final int GALLADUCCIS_ORDER_DOCUMENT_ID_3 = 7565;
	private static final int MAGIC_SWORD_HILT_ID = 7568;
	private static final int GEMSTONE_POWDER_ID = 7567;
	private static final int PURIFIED_MAGIC_NECKLACE_ID = 7566;
	private static final int MARK_OF_TRAVELER_ID = 7570;
	private static final int SCROLL_OF_ESCAPE_SPECIAL = 7556;
	
	public Q00047_IntoTheDarkForest()
	{
		super(47, Q00047_IntoTheDarkForest.class.getSimpleName(), "Into The Dark Forest");
		
		registerQuestItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1, GALLADUCCIS_ORDER_DOCUMENT_ID_2, GALLADUCCIS_ORDER_DOCUMENT_ID_3,MAGIC_SWORD_HILT_ID, GEMSTONE_POWDER_ID, PURIFIED_MAGIC_NECKLACE_ID);	
        addStartNpc(GALLADUCCI);
        addTalkId(GALLADUCCI, SANDRA, DUSTIN, GENTLER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = event;
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return htmltext;
		}
		
		switch (event)
		{
			case "30097-03.htm":
				st.startQuest();
				st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1,1);
				break;
				
			case "30094-02.htm":
	            st.set("cond","2");
	            st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_1,1);
	            st.giveItems(MAGIC_SWORD_HILT_ID,1);
	            break;
	            
			case "30097-06.htm":
	        	st.set("cond","3");
	            st.takeItems(MAGIC_SWORD_HILT_ID,1);
	            st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_2,1);
	            break;
	            
			case "30090-02.htm":
	            st.set("cond","4");
	            st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_2,1);
	            st.giveItems(GEMSTONE_POWDER_ID,1);
	            break;
	            
			case "30097-09.htm":
	            st.set("cond","5");
	            st.takeItems(GEMSTONE_POWDER_ID,1);
	            st.giveItems(GALLADUCCIS_ORDER_DOCUMENT_ID_3,1);
	            break;
	            
			case "30116-02.htm":
	            st.set("cond","6");
	            st.takeItems(GALLADUCCIS_ORDER_DOCUMENT_ID_3,1);
	            st.giveItems(PURIFIED_MAGIC_NECKLACE_ID,1);
	            break;
	            
			case "30097-12.htm":
	        	st.giveItems(SCROLL_OF_ESCAPE_SPECIAL,1);
	            st.takeItems(PURIFIED_MAGIC_NECKLACE_ID,1);
	            st.takeItems(MARK_OF_TRAVELER_ID,-1);
	            htmltext = "30097-12.htm";
	            st.unset("cond");
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
				st.isCond(0);
	            if (player.getRace()== Race.DarkElf && player.getLevel() >= 3 && st.getQuestItemsCount(MARK_OF_TRAVELER_ID) > 0)
	                htmltext = "30097-02.htm";
	            else
	                htmltext = "30097-01.htm";
				break;
			
			case State.STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId())
				{
	                   case GALLADUCCI:
	                        if (st.isCond(1))
	                            htmltext = "30097-04.htm";
	                        else if (st.isCond(2))
	                            htmltext = "30097-05.htm";
	                        else if (st.isCond(3))
	                            htmltext = "30097-07.htm";
	                        else if (st.isCond(4))
	                            htmltext = "30097-08.htm";
	                        else if (st.isCond(5))
	                            htmltext = "30097-10.htm";
	                        else if (st.isCond(6))
	                            htmltext = "30097-11.htm";
	                        break;

	                    case GENTLER:
	                        if (st.isCond(1))
	                            htmltext = "30094-01.htm";
	                        else if (cond > 1)
	                            htmltext = "30094-03.htm";
	                        break;

	                    case SANDRA:
	                        if (st.isCond(3))
	                            htmltext = "30090-01.htm";
	                        else if (cond > 3)
	                            htmltext = "30090-03.htm";
	                        break;

	                    case DUSTIN:
	                        if (st.isCond(5))
	                            htmltext = "30116-01.htm";
	                        else if (st.isCond(6))
	                            htmltext = "30116-03.htm";
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
		new Q00047_IntoTheDarkForest();
	}
}
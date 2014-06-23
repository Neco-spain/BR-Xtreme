/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00121_PavelTheGiants;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Pavel The Giants (121)
 * @author Ethernaly Pyton
 * @author BossForever Java
 */
public class Q00121_PavelTheGiants extends Quest
{
	private static final String qn = "Q00121_PavelTheGiants";
	
	// NPCs
	private static final int NEWYEAR = 31961;
	private static final int YUMI = 32041;

	public Q00121_PavelTheGiants()
	{
		super(121, qn, "Pavel The Giants!");
		
		addStartNpc(NEWYEAR);
		addTalkId(NEWYEAR, YUMI);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
		{
			return getNoQuestMsg();
		}
		String htmltext = event;
		
		switch (event)
		{
			case "32041-2.htm":
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(76960,5793);
				st.unset("cond");
				st.exitQuest(true);
				break;
				
			case "31961-1.htm":
				st.startQuest();
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			return htmltext;
		
		switch (st.getState())
		{
			case State.CREATED:
				switch (npc.getNpcId())
				{
					case NEWYEAR:
						htmltext = (player.getLevel() <46) ? "31961-1a.htm" : "31961-0.htm";
				}
			break;
			
			case State.STARTED:
				switch (npc.getNpcId())
				{
					case YUMI:
				         if (st.isCond(1))
				             htmltext = "32041-1.htm";
				        else
				          htmltext = "31961-2.htm";
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
		new Q00121_PavelTheGiants();
	}
}
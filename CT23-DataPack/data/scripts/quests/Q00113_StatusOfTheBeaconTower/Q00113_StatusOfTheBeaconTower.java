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

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Status of the Beacon Tower (113)<br>
 * Original Jython script by Kerberos.
 * @author malyelfik
 * @update BossForever to Gracia Final
 */
public class Q00113_StatusOfTheBeaconTower extends Quest
{
	// NPCs
	private static final int MOIRA = 31979;
	private static final int TORRANT = 32016;
	// Items
	private static final int FIRE_BOX = 8086;
	
	public Q00113_StatusOfTheBeaconTower()
	{
		super(113,Q00113_StatusOfTheBeaconTower.class.getSimpleName(), "Status of the Beacon Tower");
		addStartNpc(MOIRA);
		addTalkId(MOIRA, TORRANT);
		registerQuestItems(FIRE_BOX);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return null;
		}
		
		String htmltext = event;
		switch (event)
		{				
			case "31979-02.htm":
			        st.startQuest();
			        st.giveItems(FIRE_BOX,1);
			        
			case "32016-02.html":
			        st.giveItems(57,21578);
			        st.addExpAndSp(76665,5333);
			        st.takeItems(FIRE_BOX,1);
			        st.exitQuest(false,true);
			default:
				htmltext = null;
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
		
		switch (npc.getNpcId())
		{
			case MOIRA:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = (player.getLevel() >= 40) ? "31979-01.htm" : "31979-00.htm";
						break;
					case State.STARTED:
						htmltext = "31979-03.html";
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg();
						break;
				}
				break;
			case TORRANT:
				if (st.isStarted())
				{
					if (st.getQuestItemsCount(FIRE_BOX) == 1)
				        htmltext = "32016-01.htm";
				}
			    
				break;
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Q00113_StatusOfTheBeaconTower();
	}
}
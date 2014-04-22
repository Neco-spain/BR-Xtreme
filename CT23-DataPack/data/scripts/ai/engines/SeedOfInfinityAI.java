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
package ai.engines;

import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct23.xtreme.gameserver.instancemanager.InstanceManager;
import ct23.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct23.xtreme.gameserver.model.Location;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.util.Rnd;

/**
 *
 * @author Browser
 */
public class SeedOfInfinityAI extends Quest
{
	final private static String qn = "SeedOfInfinityAI";
	
	private static final int MOUTHOFEKIMUS = 32537;
	private static final int GATEKEEPROFABYSS = 32539;
	
	//Quests
	private static final String qn694 = "Q00694_BreakThroughTheHallOfSuffering";
	private static final String qn695 = "Q00695_DefendtheHallofSuffering";
	private static final String path = "/data/scripts/quests/";
	private static final String pathDef = "/data/html/default/";
		
	private static final String pathTp = "/data/html/teleporter/";
	private static final String qnHOF = "HallOfSuffering";
	private static final String qnDHOF = "DefenceHallOfSuffering";
	private static final String qnHOE = "HallOfErosion";
	private static final String qnDHOE = "DefenceHallOfErosion";
	private static final String qnHOI = "HallOfInfinity";
	private static final String qnHOEk = "HallOfEkimus";
	private static final String qnDHOEk = "DefenceHallOfEkimus";
	
	private static final Location[] _locs = {new Location(-187567, 205570, -9538)/*HOS*/, new Location(-179659, 211061, -12784)/*HOE*/, new Location(-179284,205990,-15520)/*HOI*/};
	
	@Override
	public String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn694);
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		String fileName = pathDef + "32530.htm";
		if (world != null && world.templateId == 115 && st != null && st.getInt("cond") == 1)
		{
			if (world == null || world.tag == null || (Integer)world.tag == -1)
			{
				_log.warning("Seed of Infinity - Hall of Suffering: " + player.getName() + "(" + player.getObjectId() + ") is try to cheat!");
				fileName = path + "Q"+qn694 + "/" +"32530-11.htm";
			}
			else if (player.getParty() != null && player.getParty().getPartyLeaderOID() == player.getObjectId())
				fileName = path + "Q"+qn694 + "/" + calcRewardItemHtm((Integer)world.tag);
			else
				fileName = path + "Q"+qn694 + "/" + "32530-10.htm";
		}
		
		st = player.getQuestState(qn695);
		if (world != null && world.templateId == 116 && st != null && st.getInt("cond") == 1)
		{
			if (world == null || world.tag == null || (Integer)world.tag == -1)
			{
				_log.warning("Seed of Infinity - Defence Hall of Suffering: " + player.getName() + "(" + player.getObjectId() + ") is try to cheat!");
				fileName = path + "Q"+qn695 + "/" +"32530-11.htm";
			}
			else if (player.getParty() != null && player.getParty().getPartyLeaderOID() == player.getObjectId())
				fileName = path + "Q"+qn695 + "/" + calcRewardItemHtm((Integer)world.tag);
			else
				fileName = path + "Q"+qn695 + "/" + "32530-10.htm";
		}
		npc.showChatWindow(player, fileName);
		return null;
	}
	
	private static String calcRewardItemHtm(int rewardId)
	{
		if (rewardId == 13777)
			return "32530-00.htm";
		else if (rewardId == 13778)
			return "32530-01.htm";
		else if (rewardId == 13779)
			return "32530-02.htm";
		else if (rewardId == 13780)
			return "32530-03.htm";
		else if (rewardId ==  13781)
			return "32530-04.htm";
		else if (rewardId == 13782)
			return "32530-05.htm";
		else if (rewardId == 13783)
			return "32530-06.htm";
		else if (rewardId == 13784)
			return "32530-07.htm";
		else if (rewardId == 13785)
			return "32530-08.htm";
		else if (rewardId == 13786)
			return "32530-09.htm";
		else
			return "32530-11.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		
		if (npc.getNpcId() == MOUTHOFEKIMUS)
		{
			if (event.equalsIgnoreCase(qnHOF) && player.getLevel() >= 75 && player.getLevel() <= 82)
			{
				if (GraciaSeedsManager.getInstance().getSoIState() < 3)
					player.processQuestEvent(qnHOF,"Enter");
				else if (GraciaSeedsManager.getInstance().getSoIState() > 3)
					player.processQuestEvent(qnDHOF,"Enter");
				return "";
			}
			else if (event.equalsIgnoreCase(qnHOE) && player.getLevel() >= 75)
			{
				if (GraciaSeedsManager.getInstance().getSoIState() == 1)
					player.processQuestEvent(qnHOE,"Enter");
				else if (GraciaSeedsManager.getInstance().getSoIState() == 4)
					player.processQuestEvent(qnDHOE,"Enter");
				return "";
			}
		}
		else if (npc.getNpcId() == GATEKEEPROFABYSS)
		{
			if (player.getLevel() >= 75)
			{
				if (event.equalsIgnoreCase(qnHOI))
				{
					if (contains(new int[]{2,5}, GraciaSeedsManager.getInstance().getSoIState()))
						event = pathTp + "32539-2.htm";
					else if (contains(new int[]{3,4}, GraciaSeedsManager.getInstance().getSoIState()))
						event = pathTp + "32539-1.htm";
					else
						event = pathTp + "32539-3.htm";
				}
				else if (event.equalsIgnoreCase(qnHOI+"2"))
				{
					if (GraciaSeedsManager.getInstance().getSoIState() == 3)
						player.teleToLocation(_locs[Rnd.get(0, 2)], true);//HOS,HOE,HOI
					else if (GraciaSeedsManager.getInstance().getSoIState() == 4)
						player.teleToLocation(_locs[2], true);//HOI
					
					return null;
				}
				else if (event.equalsIgnoreCase("Ekimus"))
				{
					if (GraciaSeedsManager.getInstance().getSoIState() == 2)
						player.processQuestEvent(qnHOEk,"Enter");
					else if (GraciaSeedsManager.getInstance().getSoIState() == 5)
						player.processQuestEvent(qnDHOEk,"Enter");
					
					return null;
				}
			}
			else
				event = pathTp + "32539-3.htm";
			
			npc.showChatWindow(player, event);
			return null;
		}
		return event;
	}
	
	public SeedOfInfinityAI(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(MOUTHOFEKIMUS);
		addTalkId(MOUTHOFEKIMUS);
		addStartNpc(GATEKEEPROFABYSS);
		addTalkId(GATEKEEPROFABYSS);
	}
	
	public static void main(String[] args)
	{
		new SeedOfInfinityAI(-1,qn,"instances");
	}
	
}

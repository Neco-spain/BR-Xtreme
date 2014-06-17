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
package ai.individual.npc.Dorian;

import quests.Q024_InhabitantsOfTheForestOfTheDead.Q024_InhabitantsOfTheForestOfTheDead;
import ai.engines.L2AttackableAIScript;
import ct23.xtreme.gameserver.datatables.SpawnTable;
import ct23.xtreme.gameserver.model.L2Spawn;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.network.serverpackets.NpcSay;

/**
 * Dorian (Raid Fighter) - Quest AI
 * @author malyelfik
 */
public final class Dorian extends L2AttackableAIScript
{
	// NPC
	private static final int DORIAN = 25332;
	
	// Items
	private static final int SILVER_CROSS = 7153;
	private static final int BROKEN_SILVER_CROSS = 7154;
	
	private Dorian()
	{
		super(-1, Dorian.class.getSimpleName(), "ai/individual/npc");
		addSpawnId(DORIAN);
		
		for (L2Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if ((spawn != null) && (spawn.getNpcid() == DORIAN))
			{
				startQuestTimer("checkArea", 3000, spawn.getLastSpawn(), null, true);
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("checkArea"))
		{
			if (npc.isDecayed())
			{
				cancelQuestTimers("checkArea");
			}
			else
			{
				for (L2PcInstance pl : npc.getKnownList().getKnownPlayersInRadius(300))
				{
					final QuestState qs = pl.getQuestState(Q024_InhabitantsOfTheForestOfTheDead.class.getSimpleName());
					if ((qs != null) && qs.isCond(3))
					{
						qs.takeItems(SILVER_CROSS, -1);
						qs.giveItems(BROKEN_SILVER_CROSS, 1);
						qs.setCond(4, true);
						AutoChat(npc,"That Sing!");
					}
				}
			}
		}
		return null;
	}
	public void AutoChat(L2Npc npc, String text)
	{
		L2PcInstance[] chars = ((L2Character)npc).getKnownList().getKnownPlayers().values().toArray(new L2PcInstance[0]);
		if (chars.length != 0)
		{
			for (int iCharPos = 0; iCharPos < chars.length; iCharPos++)
			{
				L2PcInstance pc = chars[iCharPos];
				pc.sendPacket(new NpcSay(npc.getObjectId(), 0, npc.getNpcId(), text));
			}
		}
	}	
	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("checkArea", 3000, npc, null, true);
		return null;
	}
	
	public static void main(String[] args)
	{
		new Dorian();
	}
}
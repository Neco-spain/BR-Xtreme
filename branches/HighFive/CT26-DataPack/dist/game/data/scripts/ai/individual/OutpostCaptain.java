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
package ai.individual;

import ai.npc.AbstractNpcAI;

import ct26.xtreme.gameserver.datatables.DoorTable;
import ct26.xtreme.gameserver.instancemanager.HellboundManager;
import ct26.xtreme.gameserver.model.actor.L2Npc;
import ct26.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;

/**
 * Outpost Captain's AI.
 * @author DS
 */
public final class OutpostCaptain extends AbstractNpcAI
{
	private static final int CAPTAIN = 18466;
	
	private static final int[] DEFENDERS =
	{
		22357,
		22358
	};
	
	private static final int DOORKEEPER = 32351;
	
	private OutpostCaptain()
	{
		super(OutpostCaptain.class.getSimpleName(), "ai/individual");
		addKillId(CAPTAIN);
		addSpawnId(CAPTAIN, DOORKEEPER);
		addSpawnId(DEFENDERS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("level_up"))
		{
			npc.deleteMe();
			HellboundManager.getInstance().setLevel(9);
		}
		return null;
	}
	
	@Override
	public final String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (HellboundManager.getInstance().getLevel() == 8)
		{
			addSpawn(DOORKEEPER, npc.getSpawn().getLocation(), false, 0, false);
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public final String onSpawn(L2Npc npc)
	{
		npc.setIsNoRndWalk(true);
		
		if (npc.getId() == CAPTAIN)
		{
			L2DoorInstance door = DoorTable.getInstance().getDoor(20250001);
			if (door != null)
			{
				door.closeMe();
			}
		}
		else if (npc.getId() == DOORKEEPER)
		{
			startQuestTimer("level_up", 3000, npc, null);
		}
		
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new OutpostCaptain();
	}
}
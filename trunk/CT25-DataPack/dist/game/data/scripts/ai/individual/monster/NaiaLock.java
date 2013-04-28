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

package ai.individual.monster;

import ai.group_template.L2AttackableAIScript;
import ai.individual.raidboss.SinWardens;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;

public class NaiaLock extends L2AttackableAIScript
{
	private static final int LOCK = 18491;
	
	public NaiaLock (int id, String name, String descr)
	{
		super(id,name,descr);
		addKillId(LOCK);
	}

	@Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		((L2MonsterInstance)npc).getMinionList().onMasterDie(true);		
		return super.onKill(npc, killer, isPet); 
	}

	public static void main(String[] args)
	{
		new SinWardens(-1, NaiaLock.class.getSimpleName(), "ai/individual/monster");
	}
}
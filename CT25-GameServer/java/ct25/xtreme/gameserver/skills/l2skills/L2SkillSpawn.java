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
package ct25.xtreme.gameserver.skills.l2skills;

import java.util.logging.Level;

import ct25.xtreme.gameserver.datatables.NpcTable;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.L2Spawn;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.templates.StatsSet;
import ct25.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct25.xtreme.util.Rnd;

public class L2SkillSpawn extends L2Skill
{
	private final int _npcId;
	private final int _despawnDelay;
	private final boolean _summonSpawn;
	private final boolean _randomOffset;
	
	public L2SkillSpawn(StatsSet set)
	{
		super(set);
		_npcId = set.getInt("npcId", 0);
		_despawnDelay = set.getInt("despawnDelay", 0);
		_summonSpawn = set.getBoolean("isSummonSpawn", false);
		_randomOffset = set.getBoolean("randomOffset", true);
	}
	
	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
			return;
		
		if (_npcId == 0)
		{
			_log.warning("NPC ID not defined for skill ID:"+this.getId());
			return;
		}
		
		final L2NpcTemplate template = NpcTable.getInstance().getTemplate(_npcId);
		if (template == null)
		{
			_log.warning("Spawn of the nonexisting NPC ID:"+_npcId+", skill ID:"+this.getId());
			return;
		}
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template);
			spawn.setInstanceId(caster.getInstanceId());
			spawn.setHeading(-1);
			
			if (_randomOffset)
			{
				spawn.setLocx(caster.getX() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20)));
				spawn.setLocy(caster.getY() + (Rnd.nextBoolean() ? Rnd.get(20, 50) : Rnd.get(-50, -20)));
			}
			else
			{
				spawn.setLocx(caster.getX());
				spawn.setLocy(caster.getY());
			}
			spawn.setLocz(caster.getZ() + 20);
			
			spawn.stopRespawn();
			L2Npc npc = spawn.spawnOne(_summonSpawn);
			if (_despawnDelay > 0)
				npc.scheduleDespawn(_despawnDelay);
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Exception while spawning NPC ID: " + _npcId + ", skill ID: " + this.getId() + ", exception: " + e.getMessage(), e);
		}
	}
}
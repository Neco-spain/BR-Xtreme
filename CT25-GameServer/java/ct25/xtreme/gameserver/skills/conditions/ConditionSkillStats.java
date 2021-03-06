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
package ct25.xtreme.gameserver.skills.conditions;

import ct25.xtreme.gameserver.skills.Env;
import ct25.xtreme.gameserver.skills.Stats;

/**
 * The Class ConditionSkillStats.
 *
 * @author mkizub
 */
public class ConditionSkillStats extends Condition
{
	
	private final Stats _stat;
	
	/**
	 * Instantiates a new condition skill stats.
	 *
	 * @param stat the stat
	 */
	public ConditionSkillStats(Stats stat)
	{
		super();
		_stat = stat;
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.skills.conditions.Condition#testImpl(ct25.xtreme.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (env.skill == null)
			return false;
		return env.skill.getStat() == _stat;
	}
}

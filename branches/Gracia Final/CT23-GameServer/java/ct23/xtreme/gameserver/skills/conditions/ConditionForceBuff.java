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
package ct23.xtreme.gameserver.skills.conditions;

import ct23.xtreme.gameserver.model.L2Effect;
import ct23.xtreme.gameserver.skills.Env;
import ct23.xtreme.gameserver.skills.effects.EffectFusion;

/**
 * @author kombat, Forsaiken
 */

public final class ConditionForceBuff extends Condition
{
	private static final short BATTLE_FORCE = 5104;
	private static final short SPELL_FORCE = 5105;
	
	private final byte[] _forces;
	
	public ConditionForceBuff(byte[] forces)
	{
		_forces = forces;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.skills.conditions.Condition#testImpl(ct23.xtreme.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		if (_forces[0] > 0)
		{
			L2Effect force = env.player.getFirstEffect(BATTLE_FORCE);
			if (force == null || ((EffectFusion) force)._effect < _forces[0])
				return false;
		}
		
		if (_forces[1] > 0)
		{
			L2Effect force = env.player.getFirstEffect(SPELL_FORCE);
			if (force == null || ((EffectFusion) force)._effect < _forces[1])
				return false;
		}
		
		return true;
	}
}
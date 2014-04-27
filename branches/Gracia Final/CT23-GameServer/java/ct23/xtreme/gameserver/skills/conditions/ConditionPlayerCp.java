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

import ct23.xtreme.gameserver.skills.Env;

/**
 * The Class ConditionPlayerCp.
 */
public class ConditionPlayerCp extends Condition
{

	private final int _cp;

	/**
	 * Instantiates a new condition player cp.
	 *
	 * @param cp the cp
	 */
	public ConditionPlayerCp(int cp)
	{
		_cp = cp;
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.skills.conditions.Condition#testImpl(ct23.xtreme.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env)
	{
		return env.player.getCurrentCp()*100/env.player.getMaxCp() >= _cp;
	}
}
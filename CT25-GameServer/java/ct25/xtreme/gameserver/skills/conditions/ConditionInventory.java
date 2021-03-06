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

/**
 * The Class ConditionInventory.
 *
 * @author mkizub
 */
public abstract class ConditionInventory extends Condition implements ConditionListener
{
	
	protected final int _slot;
	
	/**
	 * Instantiates a new condition inventory.
	 *
	 * @param slot the slot
	 */
	public ConditionInventory(int slot)
	{
		_slot = slot;
	}
	
	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see ct25.xtreme.gameserver.skills.conditions.Condition#testImpl(ct25.xtreme.gameserver.skills.Env)
	 */
	@Override
	public abstract boolean testImpl(Env env);
}

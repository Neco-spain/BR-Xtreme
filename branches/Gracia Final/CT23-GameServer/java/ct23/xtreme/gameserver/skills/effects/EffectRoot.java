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
package ct23.xtreme.gameserver.skills.effects;

import ct23.xtreme.gameserver.model.L2Effect;
import ct23.xtreme.gameserver.skills.Env;
import ct23.xtreme.gameserver.templates.effects.EffectTemplate;
import ct23.xtreme.gameserver.templates.skills.L2EffectType;

/**
 * @author mkizub
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class EffectRoot extends L2Effect
{
	
	public EffectRoot(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.ROOT;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		getEffected().startRooted();
		return true;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffected().stopRooting(this);
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
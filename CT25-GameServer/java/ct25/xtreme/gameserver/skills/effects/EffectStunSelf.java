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
package ct25.xtreme.gameserver.skills.effects;

import ct25.xtreme.gameserver.model.CharEffectList;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.skills.Env;
import ct25.xtreme.gameserver.templates.effects.EffectTemplate;
import ct25.xtreme.gameserver.templates.skills.L2EffectType;

public class EffectStunSelf extends L2Effect
{
	public EffectStunSelf(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#getEffectType()
	 */
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.STUN_SELF;
	}
	
	@Override
	public boolean isSeflEffectType()
	{
		return true;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		getEffector().startStunning();
		return true;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		getEffector().stopStunning(false);
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
	
	/* (non-Javadoc)
	 * @see ct25.xtreme.gameserver.model.L2Effect#getEffectFlags()
	 */
	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_STUNNED;
	}
}

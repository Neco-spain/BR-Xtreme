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

import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.skills.Env;
import ct25.xtreme.gameserver.templates.effects.EffectTemplate;
import ct25.xtreme.gameserver.templates.skills.L2EffectType;

public class EffectCombatPointHeal extends L2Effect
{
	public EffectCombatPointHeal(Env env, EffectTemplate template)
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
		return L2EffectType.COMBAT_POINT_HEAL;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		return false;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();
		if (target.isInvul())
			return false;
		
		double cp = calc();
		
		if ((target.getCurrentCp() + cp) > target.getMaxCp())
			cp = target.getMaxCp() - target.getCurrentCp();
		target.setCurrentCp(cp + target.getCurrentCp());
		
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED);
		sm.addNumber((int) cp);
		target.sendPacket(sm);
		return false;
	}
}

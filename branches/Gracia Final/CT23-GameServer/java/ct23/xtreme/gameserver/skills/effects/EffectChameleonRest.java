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

import ct23.xtreme.gameserver.ai.CtrlIntention;
import ct23.xtreme.gameserver.model.L2Effect;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.skills.Env;
import ct23.xtreme.gameserver.templates.effects.EffectTemplate;
import ct23.xtreme.gameserver.templates.skills.L2EffectType;
import ct23.xtreme.gameserver.templates.skills.L2SkillType;

public class EffectChameleonRest extends L2Effect
{
	public EffectChameleonRest(Env env, EffectTemplate template)
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
		return L2EffectType.RELAXING;
	}
	
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#onStart()
	 */
	@Override
	public boolean onStart()
	{
		
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			setChameleon(true);
			((L2PcInstance) effected).setSilentMoving(true);
			((L2PcInstance) effected).sitDown();
		}
		else
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		return super.onStart();
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		setChameleon(false);
		
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
			((L2PcInstance) effected).setSilentMoving(false);
		
		super.onExit();
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Effect#onActionTime()
	 */
	@Override
	public boolean onActionTime()
	{
		L2Character effected = getEffected();
		boolean retval = true;
		
		if (effected.isDead())
			retval = false;
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != L2SkillType.CONT)
			return false;
		
		if (effected instanceof L2PcInstance)
		{
			if (!((L2PcInstance) effected).isSitting())
				retval = false;
		}
		
		double manaDam = calc();
		
		if (manaDam > effected.getCurrentMp())
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			effected.sendPacket(sm);
			return false;
		}
		
		if (!retval)
			setChameleon(retval);
		else
			effected.reduceCurrentMp(manaDam);
		
		return retval;
	}
	
	/**
	 * 
	 * @param val
	 */
	private void setChameleon(boolean val)
	{
		L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
			((L2PcInstance) effected).setRelax(val);
	}
}
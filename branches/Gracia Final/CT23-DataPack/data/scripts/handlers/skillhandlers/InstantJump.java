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
package handlers.skillhandlers;


import ct23.xtreme.gameserver.ai.CtrlIntention;
import ct23.xtreme.gameserver.handler.ISkillHandler;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.FlyToLocation;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.network.serverpackets.ValidateLocation;
import ct23.xtreme.gameserver.network.serverpackets.FlyToLocation.FlyType;
import ct23.xtreme.gameserver.skills.Env;
import ct23.xtreme.gameserver.skills.Formulas;
import ct23.xtreme.gameserver.templates.skills.L2SkillType;
import ct23.xtreme.gameserver.util.Util;

/**
 *
 * @author  Didldak
 * Some parts taken from EffectWarp, which cannot be used for this case.
 */
public class InstantJump implements ISkillHandler
{
	
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.INSTANT_JUMP
	};
	
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		
		L2Character target = (L2Character)targets[0];
		int x=0,y=0,z=0;
		
		int px = target.getX();
		int py = target.getY();
		double ph = Util.convertHeadingToDegree(target.getHeading());
		
		ph+=180;
		
		if(ph>360)
			ph-=360;
		
		ph = (Math.PI * ph) / 180;
		
		x = (int) (px + (25 * Math.cos(ph)));
		y = (int) (py + (25 * Math.sin(ph)));
		z = target.getZ();
		

		
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		activeChar.broadcastPacket(new FlyToLocation(activeChar, x, y, z, FlyType.DUMMY));
		activeChar.abortAttack();
		activeChar.abortCast();
		
		activeChar.setXYZ(x, y, z);
		activeChar.broadcastPacket(new ValidateLocation(activeChar));
		
		if (skill.hasEffects())
		{
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
			{
				activeChar.stopSkillEffects(skill.getId());
				skill.getEffects(target, activeChar);
				//SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				//sm.addSkillName(skill);
				//activeChar.sendPacket(sm);
			}
			else
			{
				// activate attacked effects, if any
				target.stopSkillEffects(skill.getId());
				
				byte shld = Formulas.calcShldUse(activeChar, target, skill);
				if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, false, false, false))
				{
					skill.getEffects(activeChar, target, new Env(shld, false, false, false));
				
					//SystemMessage sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					//sm.addSkillName(skill);
					//target.sendPacket(sm);
				}
				else
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2);
					sm.addCharName(target);
					sm.addSkillName(skill);
					activeChar.sendPacket(sm);
				}
			}
		}
		
		
	}
	
	
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

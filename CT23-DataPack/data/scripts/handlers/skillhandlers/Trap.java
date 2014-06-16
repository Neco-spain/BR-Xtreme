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

import ct23.xtreme.gameserver.handler.ISkillHandler;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Trap;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.templates.skills.L2SkillType;

public class Trap implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.DETECT_TRAP,
		L2SkillType.REMOVE_TRAP
	};
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.ISkillHandler#useSkill(ct23.xtreme.gameserver.model.actor.L2Character, ct23.xtreme.gameserver.model.L2Skill, ct23.xtreme.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (activeChar == null || skill == null)
			return;
		
		switch (skill.getSkillType())
		{
			case DETECT_TRAP:
			{
				for (L2Character target: activeChar.getKnownList().getKnownCharactersInRadius(skill.getSkillRadius()))
				{
					if (!(target instanceof L2Trap))
						continue;
					
					if (target.isAlikeDead())
						continue;

					final L2Trap trap = (L2Trap)target;

					if (trap.getLevel() <= skill.getPower())
						trap.setDetected(activeChar);
				}
				break;
			}
			case REMOVE_TRAP:
			{
				for (L2Character target: (L2Character[]) targets)
				{
					if (!(target instanceof L2Trap))
						continue;
					
					if (target.isAlikeDead())
						continue;

					final L2Trap trap = (L2Trap)target;

					if (trap.canSee(activeChar))
					{
						if (activeChar instanceof L2PcInstance)
							((L2PcInstance) activeChar).sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
						continue;
					}

					if (trap.getLevel() > skill.getPower())
						continue;
					
					trap.unSummon();
					if (activeChar instanceof L2PcInstance)
						((L2PcInstance) activeChar).sendPacket(new SystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED));
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
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

import ct23.xtreme.gameserver.ai.CtrlEvent;
import ct23.xtreme.gameserver.ai.CtrlIntention;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.handler.ISkillHandler;
import ct23.xtreme.gameserver.instancemanager.DuelManager;
import ct23.xtreme.gameserver.model.L2Effect;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Attackable;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.L2Summon;
import ct23.xtreme.gameserver.model.actor.instance.L2ClanHallManagerInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.skills.Env;
import ct23.xtreme.gameserver.skills.Formulas;
import ct23.xtreme.gameserver.templates.skills.L2SkillType;

/**
 * This class ...
 *
 * @version $Revision: 1.1.2.2.2.9 $ $Date: 2005/04/03 15:55:04 $
 */

public class Continuous implements ISkillHandler
{
	//private static Logger _log = Logger.getLogger(Continuous.class.getName());
	
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.BUFF,
		L2SkillType.DEBUFF,
		L2SkillType.DOT,
		L2SkillType.MDOT,
		L2SkillType.POISON,
		L2SkillType.BLEED,
		L2SkillType.HOT,
		L2SkillType.CPHOT,
		L2SkillType.MPHOT,
		L2SkillType.FEAR,
		L2SkillType.CONT,
		L2SkillType.WEAKNESS,
		L2SkillType.REFLECT,
		L2SkillType.UNDEAD_DEFENSE,
		L2SkillType.AGGDEBUFF,
		L2SkillType.FUSION
	};

	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.ISkillHandler#useSkill(ct23.xtreme.gameserver.model.actor.L2Character, ct23.xtreme.gameserver.model.L2Skill, ct23.xtreme.gameserver.model.L2Object[])
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		boolean acted = true;
		
		L2PcInstance player = null;
		if (activeChar instanceof L2PcInstance)
			player = (L2PcInstance) activeChar;
		
		if (skill.getEffectId() != 0)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());
			
			if (sk != null)
				skill = sk;
		}
		
		for (L2Character target: (L2Character[]) targets)
		{
			boolean ss = false;
			boolean sps = false;
			boolean bss = false;
			byte shld = 0;
			
			if (Formulas.calcSkillReflect(target, skill) == Formulas.SKILL_REFLECT_SUCCEED)
				target = activeChar;
			
			// Player holding a cursed weapon can't be buffed and can't buff
			if (skill.getSkillType() == L2SkillType.BUFF && !(activeChar instanceof L2ClanHallManagerInstance))
			{
				if (target != activeChar)
				{
					if (target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
						continue;
					else if (player != null && player.isCursedWeaponEquipped())
						continue;
				}
				// TODO: boolean isn't good idea, could cause bugs
				else if (skill.getId() == 2168 && activeChar instanceof L2PcInstance)
					((L2PcInstance) activeChar).setCharmOfLuck(true);
			}
			
			switch (skill.getSkillType())
			{
				case HOT:
				case CPHOT:
				case MPHOT:
					if (activeChar.isInvul())
						continue;
					break;
			}
			
			if (skill.isOffensive() || skill.isDebuff())
			{
				L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
				if (weaponInst != null)
				{
					if (skill.isMagic())
					{
						if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
						{
							bss = true;
							if (skill.getId() != 1020) // vitalize
								weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						}
						else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
						{
							sps = true;
							if (skill.getId() != 1020) // vitalize
								weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
						}
					}
					else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
					{
						ss = true;
						if (skill.getId() != 1020) // vitalize
							weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
					}
				}
				else if (activeChar instanceof L2Summon)
				{
					L2Summon activeSummon = (L2Summon) activeChar;
					if (skill.isMagic())
					{
						if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
						{
							bss = true;
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
						}
						else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
						{
							sps = true;
							activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
						}
					}
					else if (activeSummon.getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT)
					{
						ss = true;
						activeSummon.setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
					}
				}
				else if (activeChar instanceof L2Npc)
				{
          ss = ((L2Npc) activeChar)._soulshotcharged;
          ((L2Npc) activeChar)._soulshotcharged = false;
          bss = ((L2Npc) activeChar)._spiritshotcharged;
          ((L2Npc) activeChar)._spiritshotcharged = false;
				}
				
				shld = Formulas.calcShldUse(activeChar, target, skill);
				acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss);
			}
			
			if (acted)
			{
				if (skill.isToggle())
				{
					L2Effect[] effects = target.getAllEffects();
					if (effects != null)
					{
						for (L2Effect e : effects)
						{
							if (e != null)
							{
								if (e.getSkill().getId() == skill.getId())
								{
									e.exit();
									return;
								}
							}
						}
					}
				}
				
				// if this is a debuff let the duel manager know about it
				// so the debuff can be removed after the duel
				// (player & target must be in the same duel)
				if (target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && (skill.getSkillType() == L2SkillType.DEBUFF || skill.getSkillType() == L2SkillType.BUFF) && player != null
						&& player.getDuelId() == ((L2PcInstance) target).getDuelId())
				{
					DuelManager dm = DuelManager.getInstance();
					for (L2Effect buff : skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss)))
						if (buff != null)
							dm.onBuff(((L2PcInstance) target), buff);
				}
				else
					skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
				
				if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
				{
					if (target instanceof L2Attackable)
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					else if (target instanceof L2Playable)
					{
						if (target.getTarget() == activeChar)
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						else
							target.setTarget(activeChar);
					}
				}
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
			}
			
			// Possibility of a lethal strike
			Formulas.calcLethalHit(activeChar, target, skill);
		}
		// self Effect :]
		L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if (effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}
		skill.getEffectsSelf(activeChar);
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

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

import ct25.xtreme.gameserver.handler.ISkillHandler;
import ct25.xtreme.gameserver.model.L2ItemInstance;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Summon;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.skills.Env;
import ct25.xtreme.gameserver.skills.Formulas;
import ct25.xtreme.gameserver.templates.skills.L2SkillType;

/*
 * Just a quick draft to support Wrath skill. Missing angle based calculation etc.
 */

public class CpDamPercent implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS =
	{
		L2SkillType.CPDAMPERCENT
	};

	/**
	 * @see ct25.xtreme.gameserver.handler.ISkillHandler#useSkill(ct25.xtreme.gameserver.model.actor.L2Character, ct25.xtreme.gameserver.model.L2Skill, ct25.xtreme.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(final L2Character activeChar, final L2Skill skill, final L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
			return;

		boolean ss = false;
		boolean sps = false;
		boolean bss = false;

		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if (weaponInst != null)
		{
			if (skill.isMagic())
			{
				if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
					bss = true;
				else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
					sps = true;
			}
			else if (weaponInst.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
				ss = true;
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;

			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
			else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
				activeSummon.setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if (activeChar instanceof L2Npc)
		{
			ss = ((L2Npc) activeChar)._soulshotcharged;
			((L2Npc) activeChar)._soulshotcharged = false;
			bss = ((L2Npc) activeChar)._spiritshotcharged;
			((L2Npc) activeChar)._spiritshotcharged = false;
		}

		for (final L2Character target : (L2Character[]) targets)
		{
			if (activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
				target.stopFakeDeath(true);
			else if (target.isDead() || target.isInvul())
				continue;

			final byte shld = Formulas.calcShldUse(activeChar, target, skill);

			final int damage = (int) (target.getCurrentCp() * (skill.getPower() / 100));

			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
			{
				target.breakAttack();
				target.breakCast();
			}
			skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));
			activeChar.sendDamageMessage(target, damage, false, false, false);
			target.setCurrentCp(target.getCurrentCp() - damage);
		}
	}

	/**
	 * @see ct25.xtreme.gameserver.handler.ISkillHandler#getSkillIds()
	 */
	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}

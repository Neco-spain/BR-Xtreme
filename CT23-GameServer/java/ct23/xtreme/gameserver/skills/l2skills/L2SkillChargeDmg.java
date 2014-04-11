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
package ct23.xtreme.gameserver.skills.l2skills;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.model.L2Effect;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.skills.BaseStats;
import ct23.xtreme.gameserver.skills.Formulas;
import ct23.xtreme.gameserver.templates.StatsSet;
import ct23.xtreme.gameserver.templates.item.L2WeaponType;

public class L2SkillChargeDmg extends L2Skill
{
	private static final Logger _logDamage = Logger.getLogger("damage");

	public L2SkillChargeDmg(StatsSet set)
	{
		super(set);
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}

		double modifier = 0;
		if (caster instanceof L2PcInstance)
		{
			// thanks Diego Vargas of L2Guru: 70*((0.8+0.201*No.Charges) * (PATK+POWER)) / PDEF
			modifier = 0.8+0.201*(getNumCharges()+((L2PcInstance)caster).getCharges());
		}
		L2ItemInstance weapon = caster.getActiveWeaponInstance();
		boolean soul = (weapon != null
				&& weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT
				&& weapon.getItemType() != L2WeaponType.DAGGER );

		for (L2Character target: (L2Character[]) targets)
		{
			if (target.isAlikeDead())
				continue;

			//	Calculate skill evasion
			boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, this);
			if(skillIsEvaded)
			{
				if (caster instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.C1_DODGES_ATTACK);
					sm.addString(target.getName());
					((L2PcInstance) caster).sendPacket(sm);
				}
				if (target instanceof L2PcInstance)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.AVOIDED_C1_ATTACK2);
					sm.addString(caster.getName());
					((L2PcInstance) target).sendPacket(sm);
				}

				//no futher calculations needed. 
				continue;
			}

			// TODO: should we use dual or not?
			// because if so, damage are lowered but we don't do anything special with dual then
			// like in doAttackHitByDual which in fact does the calcPhysDam call twice
			//boolean dual  = caster.isUsingDualWeapon();
			byte shld = Formulas.calcShldUse(caster, target, this);
			boolean crit = false;
			if (this.getBaseCritRate() > 0)
				crit = Formulas.calcCrit(this.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(caster), target);
			// damage calculation, crit is static 2x
			double damage = Formulas.calcPhysDam(caster, target, this, shld, false, false, soul);
			if (crit)
				damage *= 2;

			if (damage > 0)
			{
				double finalDamage = damage*modifier;

				if (Config.LOG_GAME_DAMAGE
						&& caster instanceof L2Playable
						&& damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
            		LogRecord record = new LogRecord(Level.INFO, "");
            		record.setParameters(new Object[]{caster, " did damage ", (int)damage, this, " to ", target});
            		record.setLoggerName("pdam");
            		_logDamage.log(record);
				}

				target.reduceCurrentHp(finalDamage, caster, this);

				// vengeance reflected damage
				if ((Formulas.calcSkillReflect(target, this) & Formulas.SKILL_REFLECT_VENGEANCE) != 0)
					caster.reduceCurrentHp(damage, target, this);

				caster.sendDamageMessage(target, (int)finalDamage, false, crit, false);

			}
			else
			{
				caster.sendDamageMessage(target, 0, false, false, true);
			}
		}
		if (soul && weapon!= null)
			weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
		// effect self :]
		L2Effect seffect = caster.getFirstEffect(getId());
		if (seffect != null && seffect.isSelfEffect())
		{
			//Replace old effect with new one.
			seffect.exit();
		}
		// cast self effect if any
		getEffectsSelf(caster);
	}
}

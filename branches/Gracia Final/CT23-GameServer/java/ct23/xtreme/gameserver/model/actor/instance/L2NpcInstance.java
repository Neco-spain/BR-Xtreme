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
package ct23.xtreme.gameserver.model.actor.instance;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.datatables.EnchantGroupsTable;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.datatables.SkillTreeTable;
import ct23.xtreme.gameserver.model.L2Effect;
import ct23.xtreme.gameserver.model.L2EnchantSkillLearn;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.L2SkillLearn;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.status.FolkStatus;
import ct23.xtreme.gameserver.model.base.ClassId;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.AcquireSkillList;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct23.xtreme.gameserver.network.serverpackets.ExEnchantSkillList;
import ct23.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.network.serverpackets.ExEnchantSkillList.EnchantSkillType;
import ct23.xtreme.gameserver.skills.effects.EffectBuff;
import ct23.xtreme.gameserver.skills.effects.EffectDebuff;
import ct23.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct23.xtreme.util.StringUtil;

public class L2NpcInstance extends L2Npc
{
	private final ClassId[] _classesToTeach;

	public L2NpcInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setInstanceType(InstanceType.L2NpcInstance);
		setIsInvul(false);
		_classesToTeach = template.getTeachInfo();
	}		
	@Override
	public FolkStatus getStatus()
	{
		return (FolkStatus)super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new FolkStatus(this));
	}

	@Override
	public void addEffect(L2Effect newEffect)
	{
		if (newEffect instanceof EffectDebuff || newEffect instanceof EffectBuff)
			super.addEffect(newEffect);
		else if (newEffect != null)
			newEffect.stopEffectTask();
	}

	public ClassId[] getClassesToTeach()
	{
		return _classesToTeach;
	}

	/**
	 * this displays SkillList to the player.
	 * @param player
	 */
	public static void showSkillList(L2PcInstance player, L2Npc npc, ClassId classId)
	{
		if (Config.DEBUG)
			_log.fine("SkillList activated on: "+npc.getObjectId());

		int npcId = npc.getTemplate().npcId;

		if (npcId == 32611)
		{
			L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSpecialSkills(player);
			AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Special);

			int counts = 0;

			for (L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());

				if (sk == null)
					continue;

				counts++;
				asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), 0, 1);
			}

			if (counts == 0) // No more skills to learn, come back when you level.
				player.sendPacket(new SystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
			else
				player.sendPacket(asl);

			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (!npc.getTemplate().canTeach(classId))
		{
			npc.showNoTeachHtml(player);
			return;
		}

		if (((L2NpcInstance)npc).getClassesToTeach() == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
			final String sb = StringUtil.concat(
					"<html><body>" +
					"I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:",
					String.valueOf(npcId),
					", Your classId:",
					String.valueOf(player.getClassId().getId()),
					"<br>" +
					"</body></html>"
			);
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}

		L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, classId);
		AcquireSkillList asl = new AcquireSkillList(AcquireSkillList.SkillType.Usual);
		int counts = 0;

		for (L2SkillLearn s: skills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
			if (sk == null)
				continue;

			int cost = SkillTreeTable.getInstance().getSkillCost(player, sk);
			counts++;

			asl.addSkill(s.getId(), s.getLevel(), s.getLevel(), cost, 0);
		}

		if (counts == 0)
		{
			int minlevel = SkillTreeTable.getInstance().getMinLevelForNewSkill(player, classId);
			if (minlevel > 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN);
				sm.addNumber(minlevel);
				player.sendPacket(sm);
			}
			else
				player.sendPacket(new SystemMessage(SystemMessageId.NO_MORE_SKILLS_TO_LEARN));
		}
		else
			player.sendPacket(asl);

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
     * this displays EnchantSkillList to the player.
     * @param player
     */
	public void showEnchantSkillList(L2PcInstance player, boolean isSafeEnchant)
	{
		if (Config.DEBUG)
			_log.fine("EnchantSkillList activated on: "+getObjectId());

		int npcId = getTemplate().npcId;

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final String sb = StringUtil.concat(
					"<html><body>" +
					"I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:",
					String.valueOf(npcId),
					", Your classId:",
					String.valueOf(player.getClassId().getId()),
					"<br>" +
					"</body></html>"
			);
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}

		if (!getTemplate().canTeach(player.getClassId()))
		{
			showNoTeachHtml(player);
			return;
		}

		if (player.getClassId().level() < 3)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml(
					"<html><body>Enchant A Skill:<br>" +
					"Only characters who have changed their occupation three times are allowed to enchant a skill." +
					"</body></html>");
			player.sendPacket(html);
			return;
		}

		int playerLevel = player.getLevel();
		if (playerLevel >= 76)
		{
			ExEnchantSkillList esl = new ExEnchantSkillList(isSafeEnchant ? EnchantSkillType.SAFE : EnchantSkillType.NORMAL);
			L2Skill[] charSkills = player.getAllSkills();
			int counts = 0;

			for  (L2Skill skill : charSkills)
			{
				L2EnchantSkillLearn enchantLearn = EnchantGroupsTable.getInstance().getSkillEnchantmentForSkill(skill);
				if (enchantLearn != null)
				{
					esl.addSkill(skill.getId(), skill.getLevel());
					counts++;
				}
			}

			if (counts == 0)
				player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));
			else
				player.sendPacket(esl);
		}
		else
			player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	/**
	 * Show the list of enchanted skills for changing enchantment route
	 * 
	 * @param player
	 * @param classId
	 */
	public void showEnchantChangeSkillList(L2PcInstance player)
	{
		if (Config.DEBUG)
			_log.fine("Enchanted Skill List activated on: "+getObjectId());

		int npcId = getTemplate().npcId;

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final String sb = StringUtil.concat(
					"<html><body>" +
					"I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:",
					String.valueOf(npcId),
					", Your classId:",
					String.valueOf(player.getClassId().getId()),
					"<br>" +
					"</body></html>"
			);
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}

		if (!getTemplate().canTeach(player.getClassId()))
		{
			showNoTeachHtml(player);
			return;
		}

		if (player.getClassId().level() < 3)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml(
					"<html><body>Enchant A Skill:<br>" +
					"Only characters who have changed their occupation three times are allowed to enchant a skill." +
					"</body></html>");
			player.sendPacket(html);
			return;
		}

		int playerLevel = player.getLevel();
		if (playerLevel >= 76)
		{
			ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.CHANGE_ROUTE);
			L2Skill[] charSkills = player.getAllSkills();
			for  (L2Skill skill : charSkills)
			{
				// is enchanted?
				if (skill.getLevel() > 100)
					esl.addSkill(skill.getId(), skill.getLevel());
			}

            player.sendPacket(esl);
		}
		else
			player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
    
	/**
	 * Show the list of enchanted skills for untraining
	 * 
	 * @param player
	 * @param classId
	 */
	public void showEnchantUntrainSkillList(L2PcInstance player, ClassId classId)
	{
		if (Config.DEBUG)
			_log.fine("Enchanted Skill List activated on: "+getObjectId());

		int npcId = getTemplate().npcId;

		if (_classesToTeach == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			final String sb = StringUtil.concat(
					"<html><body>" +
					"I cannot teach you. My class list is empty.<br> Ask admin to fix it. Need add my npcid and classes to skill_learn.sql.<br>NpcId:",
					String.valueOf(npcId),
					", Your classId:",
					String.valueOf(player.getClassId().getId()),
					"<br>" +
					"</body></html>"
			);
			html.setHtml(sb);
			player.sendPacket(html);
			return;
		}

		if (!getTemplate().canTeach(classId))
		{
			showNoTeachHtml(player);
			return;
		}

		if (player.getClassId().level() < 3)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setHtml(
					"<html><body>Enchant A Skill:<br>" +
					"Only characters who have changed their occupation three times are allowed to enchant a skill." +
					"</body></html>");
			player.sendPacket(html);
			return;
		}

		int playerLevel = player.getLevel();
		if (playerLevel >= 76)
		{
			ExEnchantSkillList esl = new ExEnchantSkillList(EnchantSkillType.UNTRAIN);
			L2Skill[] charSkills = player.getAllSkills();
			for  (L2Skill skill : charSkills)
			{
				// is enchanted?
				if (skill.getLevel() > 100)
					esl.addSkill(skill.getId(), skill.getLevel());
			}

			player.sendPacket(esl);
		}
		else
			player.sendPacket(new SystemMessage(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT));

		player.sendPacket(ActionFailed.STATIC_PACKET);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{       			
		if (command.startsWith("EnchantSkillList"))
			showEnchantSkillList(player, false);
		else if (command.startsWith("SafeEnchantSkillList"))
			showEnchantSkillList(player, true);
		else if (command.startsWith("ChangeEnchantSkillList"))
			showEnchantChangeSkillList(player);
		else if (command.startsWith("UntrainEnchantSkillList"))
			showEnchantUntrainSkillList(player, player.getClassId());
		else
		{
			// this class dont know any other commands, let forward
			// the command to the parent class

			super.onBypassFeedback(player, command);
		}
	}
}

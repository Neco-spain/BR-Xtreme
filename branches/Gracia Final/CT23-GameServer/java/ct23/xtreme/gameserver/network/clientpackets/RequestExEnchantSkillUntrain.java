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
package ct23.xtreme.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.datatables.SkillTreeTable;
import ct23.xtreme.gameserver.model.L2EnchantSkillLearn;
import ct23.xtreme.gameserver.model.L2EnchantSkillLearn.EnchantSkillDetail;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2ShortCut;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2NpcInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ExBrExtraUserInfo;
import ct23.xtreme.gameserver.network.serverpackets.ShortCutRegister;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.network.serverpackets.UserInfo;


/**
 * Format (ch) dd
 * c: (id) 0xD0
 * h: (subid) 0x33
 * d: skill id
 * d: skill lvl
 * @author -Wooden-
 *
 */
public final class RequestExEnchantSkillUntrain extends L2GameClientPacket
{
	private static final Logger _log = Logger.getLogger(RequestExEnchantSkillUntrain.class.getName());
	private static final Logger _logEnchant = Logger.getLogger("enchant");

	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#runImpl()
	 */
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
			return;
		
		L2Npc trainer = player.getLastFolkNPC();
		if (!(trainer instanceof L2NpcInstance))
			return;
		
		if (!trainer.canInteract(player) && !player.isGM())
			return;
		
		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
			return;
		
		if (player.getLevel() < 76)
			return;
		
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
			return;
		
		int reqItemId = SkillTreeTable.UNTRAIN_ENCHANT_BOOK;
		
		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
			return;
		
		int currentLevel = player.getSkillLevel(_skillId);
		if (currentLevel - 1 != _skillLvl && (currentLevel % 100 != 1 || _skillLvl != s.getBaseLevel()))
			return;
		
		EnchantSkillDetail esd = s.getEnchantSkillDetail(currentLevel);
		
		int requiredSp = esd.getSpCost();
		int requiredExp = esd.getExp();
		
		L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
		if (Config.ES_SP_BOOK_NEEDED)
		{
			if (spb == null) // Haven't spellbook
			{
				player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
				return;
			}
		}
		
		boolean check;
		check = player.getStat().addSp((int)(requiredSp * 0.8));
		if (Config.ES_SP_BOOK_NEEDED)
		{
			check &= player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
		}
		
		if (!check)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
			return;
		}
		
		if (_skillLvl % 100 == 0)
		{
			_skillLvl = s.getBaseLevel();
		}

		if (Config.LOG_SKILL_ENCHANTS)
		{
	        LogRecord record = new LogRecord(Level.INFO, "Untrain");
			record.setParameters(new Object[]{player, skill, spb});
			record.setLoggerName("skill");
			_logEnchant.log(record);
		}

		player.addSkill(skill, true);
		
		if (Config.DEBUG)
		{
			_log.fine("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requiredExp + " EXP.");
		}
		
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		
		if (_skillLvl > 100)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_DECREASED_BY_ONE);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		else
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.UNTRAIN_SUCCESSFUL_SKILL_S1_ENCHANT_LEVEL_RESETED);
			sm.addSkillName(_skillId);
			player.sendPacket(sm);
		}
		
		((L2NpcInstance)trainer).showEnchantUntrainSkillList(player, player.getClassId());
		this.updateSkillShortcuts(player);
	}
	
	private void updateSkillShortcuts(L2PcInstance player)
	{
		// update all the shortcuts to this skill
		L2ShortCut[] allShortCuts = player.getAllShortCuts();
		
		for (L2ShortCut sc : allShortCuts)
		{
			if (sc.getId() == _skillId && sc.getType() == L2ShortCut.TYPE_SKILL)
			{
				L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), player.getSkillLevel(_skillId), 1);
				player.sendPacket(new ShortCutRegister(newsc));
				player.registerShortCut(newsc);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:33 RequestExEnchantSkillUntrain";
	}
	
}
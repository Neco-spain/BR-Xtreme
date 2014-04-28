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
import ct23.xtreme.gameserver.model.base.Experience;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ExBrExtraUserInfo;
import ct23.xtreme.gameserver.network.serverpackets.ShortCutRegister;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.network.serverpackets.UserInfo;
import ct23.xtreme.util.Rnd;


/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x34 d: skill id d: skill lvl
 * 
 * @author -Wooden-
 * 
 */
public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
{
	protected static final Logger _log = Logger.getLogger(RequestExEnchantSkillRouteChange.class.getName());
	private static final Logger _logEnchant = Logger.getLogger("enchant");

	private int _skillId;
	private int _skillLvl;

	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}

	/*
	 * (non-Javadoc)
	 * 
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
		{
			return;
		}
		
		int reqItemId = SkillTreeTable.CHANGE_ENCHANT_BOOK;

		L2EnchantSkillLearn s = SkillTreeTable.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}

		int currentLevel = player.getSkillLevel(_skillId);
		// do u have this skill enchanted?
		if (currentLevel <= 100)
		{
			return;
		}

		int currentEnchantLevel = currentLevel % 100;
		// is the requested level valid?
		if (currentEnchantLevel != _skillLvl % 100)
		{
			return;
		}
		EnchantSkillDetail esd = s.getEnchantSkillDetail(_skillLvl);

		int requiredSp = esd.getSpCost();
        int requiredExp = esd.getExp();

        if (player.getSp() >= requiredSp)
        {
            long expAfter = player.getExp() - requiredExp;
            if (player.getExp() >= requiredExp && expAfter >= Experience.LEVEL[player.getLevel()])
            {
                // only first lvl requires book
                L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
                if (Config.ES_SP_BOOK_NEEDED) 
                {
                    if (spb == null)// Haven't spellbook
                    {
                        player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE));
                        return;
                    }
                }

                boolean check;
                check = player.getStat().removeExpAndSp(requiredExp, requiredSp);
                if (Config.ES_SP_BOOK_NEEDED) 
                {
                    check &= player.destroyItem("Consume", spb.getObjectId(), 1, trainer, true);
                }
                
                if (!check)
                {
                    player.sendPacket(new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL));
                    return;
                }
                
                int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
                _skillLvl -= levelPenalty;
                if (_skillLvl%100 == 0)
                {
                    _skillLvl = s.getBaseLevel();
                }

                skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);

                if (skill != null)
                {
                	if (Config.LOG_SKILL_ENCHANTS)
                	{
                		LogRecord record = new LogRecord(Level.INFO, "Route Change");
                		record.setParameters(new Object[]
                				{
                				player, skill, spb
                				});
                		record.setLoggerName("skill");
                		_logEnchant.log(record);
                	}
                	player.addSkill(skill, true);
                }

                if (Config.DEBUG)
                {
                	_log.fine("Learned skill ID: "+_skillId+" Level: "+_skillLvl+" for "+requiredSp+" SP, "+requiredExp+" EXP.");
                }

                player.sendPacket(new UserInfo(player));
                player.sendPacket(new ExBrExtraUserInfo(player));

                if (levelPenalty == 0)
                {
                	SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN);
                	sm.addSkillName(_skillId);
                	player.sendPacket(sm);
                }
                else
                {
                	SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2);
                	sm.addSkillName(_skillId);
                	sm.addNumber(levelPenalty);
                	player.sendPacket(sm);
                }
               
                ((L2NpcInstance)trainer).showEnchantChangeSkillList(player);

                this.updateSkillShortcuts(player);

            }
            else
            {
            	SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
            	player.sendPacket(sm);
            }
        }
        else
        {
            SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
            player.sendPacket(sm);
        }
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[C] D0:34 RequestExEnchantSkillRouteChange";
	}

}
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
package handlers.itemhandlers;

import ct23.xtreme.gameserver.datatables.MapRegionTable;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.handler.IItemHandler;
import ct23.xtreme.gameserver.instancemanager.CastleManorManager;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2Manor;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.instance.L2ChestInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;

/**
 * @author  l3x
 */
public class Seed implements IItemHandler
{	
	private int _seedId;
	private L2MonsterInstance _target;
	private L2PcInstance _activeChar;
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.handler.IItemHandler#useItem(ct23.xtreme.gameserver.model.actor.L2Playable, ct23.xtreme.gameserver.model.L2ItemInstance)
	 */
	public void useItem(L2Playable playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
			return;
		
		if (CastleManorManager.getInstance().isDisabled())
			return;
		
		_activeChar = (L2PcInstance) playable;
		L2Object target_ = _activeChar.getTarget();
		
		if (!(target_ instanceof L2Npc))
		{
			_activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		L2Character target = ((L2Character)target_);
		if (!(target instanceof L2MonsterInstance) || target instanceof L2ChestInstance || target.isRaid())
		{
			_activeChar.sendPacket(new SystemMessage(SystemMessageId.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING));
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		_target = (L2MonsterInstance) target;
		
		if (_target == null || _target.isDead())
		{
			_activeChar.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_TARGET));
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_target.isSeeded())
		{
			_activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		_seedId = item.getItemId();
		
		if (areaValid(MapRegionTable.getInstance().getAreaCastle(_activeChar)))
		{
			_target.setSeeded(_seedId, _activeChar);
			int skillId;
			int skillLvl;
			final String[] skills = item.getEtcItem().getSkills();
			if (skills != null)
			{
				String[] skill = skills[0].split("-");
				skillId = Integer.parseInt(skill[0]);
				skillLvl = Integer.parseInt(skill[1]);
				L2Skill itemskill = SkillTable.getInstance().getInfo(skillId, skillLvl); // Sowing skill
				_activeChar.useMagic(itemskill, false, false);
			}
			
		}
		else
		{
			_activeChar.sendPacket(new SystemMessage(SystemMessageId.THIS_SEED_MAY_NOT_BE_SOWN_HERE));
		}
	}
	
	/**
	 * 
	 * @param castleId
	 * @return
	 */
	private boolean areaValid(int castleId)
	{
		return (L2Manor.getInstance().getCastleIdForSeed(_seedId) == castleId);
	}
}
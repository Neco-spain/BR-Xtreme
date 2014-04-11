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

import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.L2Trap;
import ct23.xtreme.gameserver.model.olympiad.Olympiad;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.templates.chars.L2NpcTemplate;

public class L2TrapInstance extends L2Trap
{
	private L2PcInstance _owner;
	private int _level;
	private boolean _isInArena = false;
	
	/**
	 * @param objectId
	 * @param template
	 * @param owner
	 */
	public L2TrapInstance(int objectId, L2NpcTemplate template,
			L2PcInstance owner, int lifeTime, L2Skill skill)
	{
		super(objectId, template, lifeTime, skill);
		setInstanceType(InstanceType.L2TrapInstance);
		
		setInstanceId(owner.getInstanceId());
		
		_owner = owner;
		_level = owner.getLevel();
	}

	@Override
	public int getLevel()
	{
		return _level;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _owner;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_isInArena = isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE);
	}

	@Override
	public void deleteMe()
	{
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		super.deleteMe();
	}

	@Override
	public void unSummon()
	{
		if (_owner != null)
		{
			_owner.setTrap(null);
			_owner = null;
		}
		super.unSummon();
	}

	@Override
	public int getKarma()
    {
        return _owner != null ? _owner.getKarma() : 0;
    }

	@Override
	public byte getPvpFlag()
    {
        return _owner != null ? _owner.getPvpFlag() : 0;
    }

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if (miss || _owner == null)
			return;

		if (_owner.isInOlympiadMode() &&
				target instanceof L2PcInstance &&
				((L2PcInstance)target).isInOlympiadMode() &&
				((L2PcInstance)target).getOlympiadGameId() == _owner.getOlympiadGameId())
		{
			Olympiad.getInstance().notifyCompetitorDamage(getOwner(), damage, getOwner().getOlympiadGameId());
		}

		final SystemMessage sm;
		
		if (target.isInvul() && !(target instanceof L2NpcInstance))
			sm = new SystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
		else
		{
			sm = new SystemMessage(SystemMessageId.C1_GAVE_C2_DAMAGE_OF_S3);
			sm.addCharName(this);
			sm.addCharName(target);
			sm.addNumber(damage);
		}
		
		_owner.sendPacket(sm);
	}

	@Override
	public boolean canSee(L2Character cha)
	{
		if (_owner == null || cha == null)
			return false;

		if (cha == _owner)
			return true;

		if (_owner.isInParty()
				&& cha.isInParty()
				&& _owner.getParty().getPartyLeaderOID() == cha.getParty().getPartyLeaderOID())
			return true;

		return false;
	}

	@Override
	public void setDetected(L2Character detector)
	{
		if (_isInArena)
		{
			super.setDetected(detector);
			return;
		}
		if (_owner == null || (_owner.getPvpFlag() == 0 && _owner.getKarma() == 0))
			return;

		super.setDetected(detector);
	}

	@Override
	protected boolean checkTarget(L2Character target)
	{
		if (!L2Skill.checkForAreaOffensiveSkills(this, target, getSkill(), _isInArena))
			return false;

		if (_isInArena)
			return true;

		// trap not attack non-flagged players
		if (target instanceof L2Playable)
		{
			final L2PcInstance player = target.getActingPlayer();
			if (player == null || (player.getPvpFlag() == 0 && player.getKarma() == 0))
				return false;
		}

		return true;
	}
}

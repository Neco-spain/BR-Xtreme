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
package ct23.xtreme.gameserver.model.actor;

import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.serverpackets.AbstractNpcInfo;
import ct23.xtreme.gameserver.network.serverpackets.MyTargetSelected;
import ct23.xtreme.gameserver.taskmanager.DecayTaskManager;
import ct23.xtreme.gameserver.templates.chars.L2CharTemplate;
import ct23.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct23.xtreme.gameserver.templates.item.L2Weapon;

/**
 *
 * @author nBd
 */
public class L2Trap extends L2Character
{
	private L2PcInstance _owner;
	/**
	 * @param objectId
	 * @param template
	 */
	public L2Trap(int objectId, L2CharTemplate template, L2PcInstance owner)
	{
		super(objectId, template);
		setIsInvul(false);
		_owner = owner;
		setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#onSpawn()
	 */
	@Override
	public void onSpawn()
	{
		super.onSpawn();
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Object#onAction(ct23.xtreme.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public void onAction(L2PcInstance player)
	{
		player.setTarget(this);
		MyTargetSelected my = new MyTargetSelected(getObjectId(), player.getLevel() - getLevel());
		player.sendPacket(my);
	}
	
	/**
	 * 
	 *
	 */
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancelDecayTask(this);
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#onDecay()
	 */
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	/**
	 * 
	 * @return
	 */
	public final int getNpcId()
	{
		return getTemplate().npcId;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.L2Object#isAutoAttackable(ct23.xtreme.gameserver.model.actor.L2Character)
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	/**
	 * 
	 * @param owner
	 */
	public void deleteMe(L2PcInstance owner)
	{
		decayMe();
		getKnownList().removeAllKnownObjects();
		owner.setTrap(null);
	}
	
	/**
	 * 
	 * @param owner
	 */
	public synchronized void unSummon(L2PcInstance owner)
	{
		
		if (isVisible() && !isDead())
		{
			if (getWorldRegion() != null)
				getWorldRegion().removeFromZones(this);
			
			owner.setTrap(null);
			decayMe();
			getKnownList().removeAllKnownObjects();
		}
	}

	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#getActiveWeaponInstance()
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#getActiveWeaponItem()
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#getLevel()
	 */
	@Override
	public int getLevel()
	{
		return getTemplate().level;
	}
	
	/**
	 * 
	 * @return
	 */
	public final L2PcInstance getOwner()
	{
		return _owner;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#getTemplate()
	 */
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#getSecondaryWeaponInstance()
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}
	
	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#getSecondaryWeaponItem()
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	/**
	 * 
	 * @see ct23.xtreme.gameserver.model.actor.L2Character#updateAbnormalEffect()
	 */
	@Override
	public void updateAbnormalEffect()
	{
		
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isDetected()
	{
		return false;
	}
	
	/**
	 * 
	 *
	 */
	public void setDetected()
	{
		// Do nothing
	}
	
    @Override
    public void sendInfo(L2PcInstance activeChar)
    {
    	activeChar.sendPacket(new AbstractNpcInfo.TrapInfo(this, activeChar));
    }
}

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
package ct25.xtreme.gameserver.model.entity.event;

import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.restriction.AbstractRestriction;

/**
 * @author L0ngh0rn
 *
 */
public final class DMRestriction extends AbstractRestriction
{
	private static final class SingletonHolder
	{
		private static final DMRestriction INSTANCE = new DMRestriction();
	}
	
	public static DMRestriction getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	public DMRestriction()
	{
	}
	
	@Override
	public boolean fakePvPZone(L2PcInstance activeChar, L2PcInstance target)
	{		
		if ((DMEvent.isStarted()) && (DMEvent.isPlayerParticipant(activeChar) && DMEvent.isPlayerParticipant(target)))
			return true;
		
		return false;
	}
}

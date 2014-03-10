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
package ct23.xtreme.gameserver.model.actor.knownlist;

import ct23.xtreme.gameserver.model.L2Object;

public class NullKnownList extends ObjectKnownList
{

	/**
	 * @param activeObject
	 */
	public NullKnownList(L2Object activeObject)
	{
		super(activeObject);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.model.actor.knownlist.ObjectKnownList#addKnownObject(ct23.xtreme.gameserver.model.L2Object)
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		return false;
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.model.actor.knownlist.ObjectKnownList#getActiveObject()
	 */
	@Override
	public L2Object getActiveObject()
	{
		return super.getActiveObject();
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.model.actor.knownlist.ObjectKnownList#getDistanceToForgetObject(ct23.xtreme.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.model.actor.knownlist.ObjectKnownList#getDistanceToWatchObject(ct23.xtreme.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.model.actor.knownlist.ObjectKnownList#removeAllKnownObjects()
	 *
	 * no-op
	 */
	@Override
	public void removeAllKnownObjects()
	{ }

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.model.actor.knownlist.ObjectKnownList#removeKnownObject(ct23.xtreme.gameserver.model.L2Object)
	 */
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		return false;
	}
}
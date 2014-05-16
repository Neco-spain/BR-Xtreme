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
package ct23.xtreme.gameserver.instancemanager.tasks;

import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager;

/**
 * Task which updates Seed of Infinity state.
 * @author Browser
 */
public final class UpdateSoIStateTask implements Runnable
{
	private int _nextState;
	
	public UpdateSoIStateTask(int value)
	{
		_nextState = value;
	}
	
	@Override
	public void run()
	{
		final GraciaSeedsManager manager = GraciaSeedsManager.getInstance();
		manager.setSoIStage(_nextState, true);
		manager.updateSoIState();
		manager.handleSoIStages(false);
		if (_nextState == 1)
			manager.clearItems();
	}
}
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
package ct25.xtreme.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;
import java.util.logging.Logger;

import ct25.xtreme.L2DatabaseFactory;
import ct25.xtreme.gameserver.taskmanager.Task;
import ct25.xtreme.gameserver.taskmanager.TaskManager;
import ct25.xtreme.gameserver.taskmanager.TaskTypes;
import ct25.xtreme.gameserver.taskmanager.TaskManager.ExecutedTask;


/**
 * @author Layane
 * 
 */
public class TaskRecom extends Task
{
	private static final Logger _log = Logger.getLogger(TaskRecom.class.getName());
	private static final String NAME = "recommendations";
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.taskmanager.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.taskmanager.Task#onTimeElapsed(ct25.xtreme.gameserver.taskmanager.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		Connection con = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			
			PreparedStatement statement = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, time_left=?, rec_have=0 WHERE rec_have <=  20");
			statement.setInt(1, 0);	// Rec left = 0
			statement.setInt(2, 3600000); // Timer = 1 hour
			statement.execute();
			
			statement = con.prepareStatement("UPDATE character_reco_bonus SET rec_left=?, time_left=?, rec_have=GREATEST(rec_have-20,0) WHERE rec_have > 20");
			statement.setInt(1, 0);	// Rec left = 0
			statement.setInt(2, 3600000); // Timer = 1 hour
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not reset Recommendations System: " + e);
		}
		finally
		{
			L2DatabaseFactory.close(con);
		}
		_log.config("Recommendations System reseted");
	}
	
	/**
	 * 
	 * @see ct25.xtreme.gameserver.taskmanager.Task#initializate()
	 */
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
	
}

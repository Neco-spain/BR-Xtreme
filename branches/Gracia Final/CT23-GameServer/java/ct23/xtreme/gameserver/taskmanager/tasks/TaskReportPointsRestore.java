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
package ct23.xtreme.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ct23.xtreme.L2DatabaseFactory;
import ct23.xtreme.gameserver.taskmanager.Task;
import ct23.xtreme.gameserver.taskmanager.TaskManager;
import ct23.xtreme.gameserver.taskmanager.TaskManager.ExecutedTask;
import ct23.xtreme.gameserver.taskmanager.TaskTypes;

/**
 * @author BiggBoss
 */
public class TaskReportPointsRestore extends Task
{
    private static final String NAME = "report_points_restore";
    
    /* (non-Javadoc)
     * @see ct23.xtreme.gameserver.taskmanager.Task#getName()
     */
    @Override
    public String getName()
    {
        return NAME;
    }

    /* (non-Javadoc)
     * @see ct23.xtreme.gameserver.taskmanager.Task#onTimeElapsed(ct23.xtreme.gameserver.taskmanager.TaskManager.ExecutedTask)
     */
    @Override
    public void onTimeElapsed(ExecutedTask task)
    {
        Connection con = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            PreparedStatement update = con.prepareStatement("UPDATE accounts SET bot_report_points = 7");
            update.execute();
            update.close();
            System.out.println("Sucessfully restored Bot Report Points for all accounts!");
        }
        catch(SQLException sqle)
        {
            sqle.printStackTrace();
        }
        finally
        {
            try { con.close(); } catch(Exception e) { e.printStackTrace(); }
        }
    }
    
    /**
     * 
     * @see ct23.xtreme.gameserver.taskmanager.Task#initializate()
     */
    @Override
    public void initializate()
    {
        super.initializate();
        TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:00:00", "");
    }
}
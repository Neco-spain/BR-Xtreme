/** This program is free software: you can redistribute it and/or modify it under
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
package handlers.usercommandhandlers;

import java.text.SimpleDateFormat;
import java.util.Date;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.GameTimeController;
import ct25.xtreme.gameserver.handler.IUserCommandHandler;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;

public class Time implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		77
	};

	private static final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");

	/**
	 * @see ct25.xtreme.gameserver.handler.IUserCommandHandler#useUserCommand(int, ct25.xtreme.gameserver.model.actor.instance.L2PcInstance)
	 */
	@Override
	public boolean useUserCommand(final int id, final L2PcInstance activeChar)
	{
		if (COMMAND_IDS[0] != id)
			return false;

		final int t = GameTimeController.getInstance().getGameTime();
		final String h = "" + t / 60 % 24;
		String m;
		if (t % 60 < 10)
			m = "0" + t % 60;
		else
			m = "" + t % 60;

		SystemMessage sm;
		if (GameTimeController.getInstance().isNowNight())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT);
			sm.addString(h);
			sm.addString(m);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY);
			sm.addString(h);
			sm.addString(m);
		}
		activeChar.sendPacket(sm);
		if (Config.L2JMOD_DISPLAY_SERVER_TIME)
			activeChar.sendMessage("Server time is " + fmt.format(new Date(System.currentTimeMillis())));
		return true;
	}

	/**
	 * @see ct25.xtreme.gameserver.handler.IUserCommandHandler#getUserCommandList()
	 */
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}

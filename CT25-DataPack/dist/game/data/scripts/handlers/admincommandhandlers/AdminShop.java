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
package handlers.admincommandhandlers;

import java.util.logging.Logger;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.TradeController;
import ct25.xtreme.gameserver.handler.IAdminCommandHandler;
import ct25.xtreme.gameserver.model.L2TradeList;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.BuyList;
import ct25.xtreme.gameserver.network.serverpackets.ExBuySellListPacket;

/**
 * This class handles following admin commands: - gmshop = shows menu - buy id = shows shop with respective id
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShop implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminShop.class.getName());

	private static final String[] ADMIN_COMMANDS =
	{
		"admin_buy",
		"admin_gmshop"
	};

	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
			return false;

		if (command.startsWith("admin_buy"))
			try
			{
				handleBuyRequest(activeChar, command.substring(10));
			}
			catch (final IndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify buylist.");
			}
		else if (command.equals("admin_gmshop"))
			AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleBuyRequest(final L2PcInstance activeChar, final String command)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch (final Exception e)
		{
			_log.warning("admin buylist failed:" + command);
		}

		final L2TradeList list = TradeController.getInstance().getBuyList(val);

		if (list != null)
		{
			activeChar.sendPacket(new BuyList(list, activeChar.getAdena(), 0));
			activeChar.sendPacket(new ExBuySellListPacket(activeChar, list, 0, false));
			if (Config.DEBUG)
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM shop id " + val);
		}
		else
			_log.warning("no buylist with id:" + val);
		activeChar.sendPacket(ActionFailed.STATIC_PACKET);
	}
}

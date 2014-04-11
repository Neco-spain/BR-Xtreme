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
package ct23.xtreme.gameserver.network.clientpackets;

import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

/**
 * 
 * @author  Gnacik
 *
 */
public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket
{
	private static int _page;
	private static int _minlvl;
	private static int _maxlvl;
    private static int _mode; // 1 - waitlist 0 - room waitlist

    @Override
	protected void readImpl()
    {
    	_page = readD();
    	_minlvl = readD();
    	_maxlvl = readD();
    	_mode	= readD();
    }

    @Override
	protected void runImpl()
    {
		L2PcInstance _activeChar = getClient().getActiveChar();
		
		if (_activeChar == null)
			return;

		_activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(_activeChar,_page,_minlvl,_maxlvl, _mode));
    }

    @Override
    public String getType()
    {
        return "[C] D0:31 RequestListPartyMatchingWaitingRoom";
    }

}
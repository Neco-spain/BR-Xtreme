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
package ct23.xtreme.loginserver.clientpackets;

import ct23.xtreme.loginserver.serverpackets.LoginFail.LoginFailReason;
import ct23.xtreme.loginserver.serverpackets.ServerList;

/**
 * Format: ddc
 * d: fist part of session id
 * d: second part of session id
 * c: ?
 */
public class RequestServerList extends L2LoginClientPacket
{
	private int _skey1;
	private int _skey2;
	private int _data3;

	/**
	 * @return
	 */
	public int getSessionKey1()
	{
		return _skey1;
	}

	/**
	 * @return
	 */
	public int getSessionKey2()
	{
		return _skey2;
	}

	/**
	 * @return
	 */
	public int getData3()
	{
		return _data3;
	}

	@Override
	public boolean readImpl()
	{
		if (super._buf.remaining() >= 8)
		{
			_skey1  = readD(); // loginOk 1
			_skey2  = readD(); // loginOk 2
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * @see ct23.xtreme.mmocore.network.ReceivablePacket#run()
	 */
	@Override
	public void run()
	{
		if (getClient().getSessionKey().checkLoginPair(_skey1, _skey2))
		{
			getClient().sendPacket(new ServerList(getClient()));
		}
		else
		{
			getClient().close(LoginFailReason.REASON_ACCESS_FAILED);
		}
	}
}

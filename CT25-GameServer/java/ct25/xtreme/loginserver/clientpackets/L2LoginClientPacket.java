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
package ct25.xtreme.loginserver.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mmocore.network.ReceivablePacket;

import ct25.xtreme.loginserver.L2LoginClient;

/**
 *
 * @author  KenM
 */
public abstract class L2LoginClientPacket extends ReceivablePacket<L2LoginClient>
{
	private static Logger _log = Logger.getLogger(L2LoginClientPacket.class.getName());
	
	/**
	 * @see ct25.xtreme.mmocore.network.ReceivablePacket#read()
	 */
	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "ERROR READING: "+this.getClass().getSimpleName() + ": " + e.getMessage(), e);
			return false;
		}
	}
	
	protected abstract boolean readImpl();
}

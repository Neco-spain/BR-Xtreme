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

import java.util.logging.Logger;

/**
 * Format: chdd
 * d: Arena
 * d: Team
 * 
 * @author mrTJO
 */
public final class RequestExCubeGameChangeTeam extends L2GameClientPacket
{
	private static final String _C__D0_5A_REQUESTEXCUBEGAMECHANGETEAM = "[C] D0:5A RequestExCubeGameChangeTeam";
	private static Logger _log = Logger.getLogger(RequestExCubeGameChangeTeam.class.getName());
	
	int _arena;
	int _team;
	
	@Override
	protected void readImpl()
	{
		_arena = readD();
		_team = readD();
	}

	@Override
	public void runImpl()
	{	
		switch (_team)
		{
			case 0:
			case 1:
				// Change Player Team
				break;
			case -1:
				// Remove Player (me)
				break;
			default:
				_log.warning("Wrong Cube Game Team ID: "+_team);
				break;
		}
	}

	@Override
	public String getType()
	{
		return _C__D0_5A_REQUESTEXCUBEGAMECHANGETEAM;
	}
}

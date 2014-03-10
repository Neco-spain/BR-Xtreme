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

/**
 *
 * @author  KenM
 */
public class RequestKeyMapping extends L2GameClientPacket
{

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#getType()
     */
    @Override
    public String getType()
    {
        return "[C] D0:21 RequestKeyMapping";
    }

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#readImpl()
     */
    @Override
    protected void readImpl()
    {
        // trigger (no data)
    }

    /**
     * @see ct23.xtreme.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
     */
    @Override
    protected void runImpl()
    {
        // pending
    }
    
}
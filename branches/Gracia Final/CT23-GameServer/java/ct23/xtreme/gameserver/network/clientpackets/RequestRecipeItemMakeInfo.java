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
import ct23.xtreme.gameserver.network.serverpackets.RecipeItemMakeInfo;

/**
 */
public final class RequestRecipeItemMakeInfo extends L2GameClientPacket
{
    private static final String _C__AE_REQUESTRECIPEITEMMAKEINFO = "[C] AE RequestRecipeItemMakeInfo";
	//private static Logger _log = Logger.getLogger(RequestSellItem.class.getName());

	private int _id;
	private L2PcInstance _activeChar;

	@Override
	protected void readImpl()
	{
		_id = readD();
		_activeChar = getClient().getActiveChar();
	}

	@Override
	protected void runImpl()
	{
		RecipeItemMakeInfo response = new RecipeItemMakeInfo(_id, _activeChar);
		sendPacket(response);
	}

    /* (non-Javadoc)
     * @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#getType()
     */
    @Override
	public String getType()
    {
        return _C__AE_REQUESTRECIPEITEMMAKEINFO;
    }
}

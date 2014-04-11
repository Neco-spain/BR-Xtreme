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
package ct23.xtreme.gameserver.network.serverpackets;

/**
 * @author Migi
 *
 */
public class ExNoticePostArrived extends L2GameServerPacket
{
	private static final String _S__FE_A9_EXNOTICEPOSTARRIVED = "[S] A9 ExNoticePostArrived";
	private static final ExNoticePostArrived STATIC_PACKET_TRUE = new ExNoticePostArrived(true);
	private static final ExNoticePostArrived STATIC_PACKET_FALSE = new ExNoticePostArrived(false);
	
	public static final ExNoticePostArrived valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}
	
	boolean _showAnim;

	public ExNoticePostArrived(boolean showAnimation)
	{
		_showAnim = showAnimation;
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0xfe);
		writeH(0xa9);
		writeD(_showAnim ? 0x01 : 0x00);
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_A9_EXNOTICEPOSTARRIVED;
	}
}

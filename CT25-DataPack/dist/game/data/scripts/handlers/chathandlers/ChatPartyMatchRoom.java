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
package handlers.chathandlers;

import ct25.xtreme.gameserver.handler.IChatHandler;
import ct25.xtreme.gameserver.model.PartyMatchRoom;
import ct25.xtreme.gameserver.model.PartyMatchRoomList;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.network.serverpackets.CreatureSay;

/**
 * A chat handler
 * @author Gnacik
 */
public class ChatPartyMatchRoom implements IChatHandler
{
	private static final int[] COMMAND_IDS =
	{
		14
	};

	/**
	 * Handle chat type 'partymatchroom'
	 */
	@Override
	public void handleChat(final int type, final L2PcInstance activeChar, final String target, final String text)
	{
		if (activeChar.isInPartyMatchRoom())
		{
			final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(activeChar);
			if (_room != null)
			{
				final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
				for (final L2PcInstance _member : _room.getPartyMembers())
					_member.sendPacket(cs);
			}
		}
	}

	/**
	 * @return the chat types registered to this handler
	 */
	@Override
	public int[] getChatTypeList()
	{
		return COMMAND_IDS;
	}

	public static void main(final String[] args)
	{
		new ChatPartyMatchRoom();
	}
}
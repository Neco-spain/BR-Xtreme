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

import static ct23.xtreme.gameserver.model.actor.L2Character.ZONE_PEACE;
import ct23.xtreme.Config;
import ct23.xtreme.gameserver.instancemanager.MailManager;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.entity.Message;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ExChangePostState;
import ct23.xtreme.gameserver.network.serverpackets.ExShowReceivedPost;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.util.Util;

/**
 * @author Migi, DS
 */
public final class RequestReceivedPost extends L2GameClientPacket
{
	private static final String _C__D0_69_REQUESTRECEIVEDPOST = "[C] D0:69 RequestReceivedPost";

	private int _msgId;

	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}

	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null || !Config.ALLOW_MAIL)
			return;

		if (!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE));
			return;
		}

		final Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
			return;

		if (msg.getReceiverId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar,
					"Player "+activeChar.getName()+" tried to receive not own post!", Config.DEFAULT_PUNISH);
			return;
		}

		if (msg.isDeletedByReceiver())
			return;

		activeChar.sendPacket(new ExShowReceivedPost(msg));
		activeChar.sendPacket(new ExChangePostState(true, _msgId, Message.READED));
		msg.markAsRead();
	}

	@Override
	public String getType()
	{
		return _C__D0_69_REQUESTRECEIVEDPOST;
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}

/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
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
import ct23.xtreme.gameserver.network.serverpackets.ExShowSentPost;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.util.Util;

/**
 * @author Migi, DS
 */
public final class RequestSentPost extends L2GameClientPacket
{
	private static final String _C__D0_6E_REQUESTSENTPOST = "[C] D0:6E RequestSentPost";

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

		Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
			return;

		if (msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar,
					"Player "+activeChar.getName()+" tried to read not own post!", Config.DEFAULT_PUNISH);
			return;
		}

		if (msg.isDeletedBySender())
			return;

		activeChar.sendPacket(new ExShowSentPost(msg));
	}

	@Override
	public String getType()
	{
		return _C__D0_6E_REQUESTSENTPOST;
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
} 

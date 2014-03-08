/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ct26.xtreme.gameserver.network.clientpackets;

import ct26.xtreme.Config;
import ct26.xtreme.gameserver.instancemanager.MailManager;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.entity.Message;
import ct26.xtreme.gameserver.model.zone.ZoneId;
import ct26.xtreme.gameserver.network.SystemMessageId;
import ct26.xtreme.gameserver.network.serverpackets.ExReplySentPost;
import ct26.xtreme.gameserver.util.Util;

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
		if ((activeChar == null) || !Config.ALLOW_MAIL)
		{
			return;
		}
		
		Message msg = MailManager.getInstance().getMessage(_msgId);
		if (msg == null)
		{
			return;
		}
		
		if (!activeChar.isInsideZone(ZoneId.PEACE) && msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}
		
		if (msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to read not own post!", Config.DEFAULT_PUNISH);
			return;
		}
		
		if (msg.isDeletedBySender())
		{
			return;
		}
		
		activeChar.sendPacket(new ExReplySentPost(msg));
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

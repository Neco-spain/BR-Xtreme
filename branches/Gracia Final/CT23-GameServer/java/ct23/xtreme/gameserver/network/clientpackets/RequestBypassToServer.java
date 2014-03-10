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

import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.ai.CtrlIntention;
import ct23.xtreme.gameserver.communitybbs.CommunityBoard;
import ct23.xtreme.gameserver.datatables.AdminCommandAccessRights;
import ct23.xtreme.gameserver.handler.AdminCommandHandler;
import ct23.xtreme.gameserver.handler.IAdminCommandHandler;
import ct23.xtreme.gameserver.model.L2CharPosition;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2World;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2MerchantSummonInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.entity.L2Event;
import ct23.xtreme.gameserver.model.olympiad.Olympiad;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct23.xtreme.gameserver.network.serverpackets.ExHeroList;
import ct23.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct23.xtreme.gameserver.util.GMAudit;

/**
 * This class ...
 *
 * @version $Revision: 1.12.4.5 $ $Date: 2005/04/11 10:06:11 $
 */
public final class RequestBypassToServer extends L2GameClientPacket
{
	private static final String _C__21_REQUESTBYPASSTOSERVER = "[C] 21 RequestBypassToServer";
	private static Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());

	// S
	private String _command;

	/**
	 * @param decrypt
	 */
	@Override
	protected void readImpl()
	{
		_command = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		    return;
		
		if (!activeChar.getFloodProtectors().getServerBypass().tryPerformAction(_command))
			return;

		try
		{
			if (_command.startsWith("admin_")) //&& activeChar.getAccessLevel() >= Config.GM_ACCESSLEVEL)
			{
				String command = _command.split(" ")[0];

				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(command);
				
				if (ach == null)
				{
					if ( activeChar.isGM() )
						activeChar.sendMessage("The command " + command.substring(6) + " does not exist!");

					_log.warning("No handler registered for admin command '" + command + "'");
					return;
				}

				if (!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					activeChar.sendMessage("You don't have the access right to use this command!");
					_log.warning("Character " + activeChar.getName() + " tryed to use admin command " + command + ", but have no access to it!");
					return;
				}
				if (Config.GMAUDIT)
					GMAudit.auditGMAction(activeChar.getName()+" ["+activeChar.getObjectId()+"]", _command, (activeChar.getTarget() != null?activeChar.getTarget().getName():"no-target"));

				ach.useAdminCommand(_command, activeChar);
			}
			else if (_command.equals("come_here") && ( activeChar.isGM()))
			{
				comeHere(activeChar);
			}
			else if (_command.startsWith("player_help "))
			{
				playerHelp(activeChar, _command.substring(12));
			}
			else if (_command.startsWith("npc_"))
			{
				if(!activeChar.validateBypass(_command))
					return;

				int endOfId = _command.indexOf('_', 5);
				String id;
				if (endOfId > 0)
					id = _command.substring(4, endOfId);
				else
					id = _command.substring(4);
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if (_command.substring(endOfId+1).startsWith("event_participate"))
						L2Event.inscribePlayer(activeChar);
					else if (object instanceof L2Npc && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
						((L2Npc)object).onBypassFeedback(activeChar, _command.substring(endOfId+1));

					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe) {}
			}
			else if (_command.startsWith("summon_"))
			{
				if(!activeChar.validateBypass(_command))
					return;

				int endOfId = _command.indexOf('_', 8);
				String id;
				if (endOfId > 0)
					id = _command.substring(7, endOfId);
				else
					id = _command.substring(7);
				try
				{
					L2Object object = L2World.getInstance().findObject(Integer.parseInt(id));

					if (object instanceof L2MerchantSummonInstance && endOfId > 0 && activeChar.isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
						((L2MerchantSummonInstance)object).onBypassFeedback(activeChar, _command.substring(endOfId+1));

					activeChar.sendPacket(ActionFailed.STATIC_PACKET);
				}
				catch (NumberFormatException nfe) {}
			}
			//	Draw a Symbol
			else if (_command.equals("menu_select?ask=-16&reply=1"))
			{
                L2Object object = activeChar.getTarget();
                if (object instanceof L2Npc)
                    ((L2Npc) object).onBypassFeedback(activeChar, _command);
			}
			else if (_command.equals("menu_select?ask=-16&reply=2"))
			{
                L2Object object = activeChar.getTarget();
                if (object instanceof L2Npc)
                    ((L2Npc) object).onBypassFeedback(activeChar, _command);
			}
			// Navigate through Manor windows
            else if (_command.startsWith("manor_menu_select?"))
            {
            	L2Object object = activeChar.getTarget();
                if (object instanceof L2Npc)
                    ((L2Npc) object).onBypassFeedback(activeChar, _command);
            }
			else if (_command.startsWith("bbs_"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("_bbs"))
			{
				CommunityBoard.getInstance().handleCommands(getClient(), _command);
			}
			else if (_command.startsWith("Quest "))
			{
				if(!activeChar.validateBypass(_command))
					return;

				L2PcInstance player = getClient().getActiveChar();
				if (player == null) return;

				String p = _command.substring(6).trim();
				int idx = p.indexOf(' ');
				if (idx < 0)
					player.processQuestEvent(p, "");
				else
					player.processQuestEvent(p.substring(0, idx), p.substring(idx).trim());
			}
			else if (_command.startsWith("OlympiadArenaChange"))
			{
					Olympiad.bypassChangeArena(_command, activeChar);
			}
			else if (_command.startsWith("_herolist"))
			{
				activeChar.sendPacket(new ExHeroList());
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "Bad RequestBypassToServer: ", e);
		}
	}

	/**
	 * @param client
	 */
	private void comeHere(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if (obj == null) return;
		if (obj instanceof L2Npc)
		{
			L2Npc temp = (L2Npc) obj;
			temp.setTarget(activeChar);
			temp.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(activeChar.getX(),activeChar.getY(), activeChar.getZ(), 0 ));
		}
	}

	private void playerHelp(L2PcInstance activeChar, String path)
	{
        if (path.indexOf("..") != -1)
            return;
        
        StringTokenizer st = new StringTokenizer(path);
        String[] cmd = st.nextToken().split("#");
        
        if (cmd.length > 1)
        {
        	int itemId = 0;
        	itemId = Integer.parseInt(cmd[1]);
        	String filename = "data/html/help/"+cmd[0];
        	NpcHtmlMessage html = new NpcHtmlMessage(1,itemId);
        	html.setFile(filename);
        	html.disableValidation();
        	activeChar.sendPacket(html);
        }
        else
        {
        	String filename = "data/html/help/"+path;
        	NpcHtmlMessage html = new NpcHtmlMessage(1);
        	html.setFile(filename);
        	html.disableValidation();
        	activeChar.sendPacket(html);
        }
	}

	/* (non-Javadoc)
	 * @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__21_REQUESTBYPASSTOSERVER;
	}
}
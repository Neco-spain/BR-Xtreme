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
package handlers.voicedcommandhandlers;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.cache.HtmCache;
import ct25.xtreme.gameserver.handler.IVoicedCommandHandler;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.TvTEvent;
import ct25.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct25.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Tvt info.
 * @author denser
 */
public class TvTVoicedInfo implements IVoicedCommandHandler
{
	private static final String[] _voicedCommands =
	{
		"tvtinfo",
		"tvtjoin",
		"tvtleave"
	};

	/**
	 * Set this to false and recompile script if you dont want to use string cache. This will decrease performance but will be more consistent against possible html editions during runtime Recompiling the script will get the new html would be enough too [DrHouse]
	 */
	private static final boolean USE_STATIC_HTML = true;
	private static final String HTML = HtmCache.getInstance().getHtm(null, "data/html/mods/TvTEvent/Status.htm");

	@Override
	public boolean useVoicedCommand(final String command, final L2PcInstance activeChar, final String target)
	{
		if (command.equalsIgnoreCase("tvtinfo"))
		{
			if (TvTEvent.isStarting() || TvTEvent.isStarted())
			{
				final String htmContent = USE_STATIC_HTML && !HTML.isEmpty() ? HTML : HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "data/html/mods/TvTEvent/Status.htm");

				try
				{
					final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);

					npcHtmlMessage.setHtml(htmContent);
					// npcHtmlMessage.replace("%objectId%",
					// String.valueOf(getObjectId()));
					npcHtmlMessage.replace("%team1name%", Config.TVT_EVENT_TEAM_1_NAME);
					npcHtmlMessage.replace("%team1playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[0]));
					npcHtmlMessage.replace("%team1points%", String.valueOf(TvTEvent.getTeamsPoints()[0]));
					npcHtmlMessage.replace("%team2name%", Config.TVT_EVENT_TEAM_2_NAME);
					npcHtmlMessage.replace("%team2playercount%", String.valueOf(TvTEvent.getTeamsPlayerCounts()[1]));
					npcHtmlMessage.replace("%team2points%", String.valueOf(TvTEvent.getTeamsPoints()[1]));
					activeChar.sendPacket(npcHtmlMessage);
				}
				catch (final Exception e)
				{
					_log.warning("wrong TvT voiced: " + e);
				}

			}
			else
				activeChar.sendPacket(ActionFailed.STATIC_PACKET);
		}
		else if (command.equalsIgnoreCase("tvtjoin"))
			TvTEvent.onBypass("tvt_event_participation", activeChar);
		else if (command.equalsIgnoreCase("tvtleave"))
			TvTEvent.onBypass("tvt_event_remove_participation", activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _voicedCommands;
	}
}

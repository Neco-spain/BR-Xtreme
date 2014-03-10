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
package ct23.xtreme.gameserver.model.actor.instance;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.cache.HtmCache;
import ct23.xtreme.gameserver.datatables.CharTemplateTable;
import ct23.xtreme.gameserver.model.base.ClassId;
import ct23.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct23.xtreme.gameserver.network.serverpackets.TutorialCloseHtml;
import ct23.xtreme.gameserver.network.serverpackets.TutorialShowHtml;
import ct23.xtreme.gameserver.network.serverpackets.TutorialShowQuestionMark;
import ct23.xtreme.gameserver.templates.chars.L2NpcTemplate;
import ct23.xtreme.gameserver.util.StringUtil;

/**
 * This class ...
 *
 * @version $Revision: 1.4.2.1.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class L2ClassMasterInstance extends L2NpcInstance
{
	/**
	 * @param template
	 */
	public L2ClassMasterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public String getHtmlPath(int npcId, int val)
	{
		String pom = "";

		if (val == 0)
			pom = "" + npcId;
		else
			pom = npcId + "-" + val;

		return "data/html/classmaster/" + pom + ".htm";
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		if(command.startsWith("1stClass"))
		{
			showHtmlMenu(player, getObjectId(), 1);
		}
		else if(command.startsWith("2ndClass"))
		{
			showHtmlMenu(player, getObjectId(), 2);
		}
		else if(command.startsWith("3rdClass"))
		{
			showHtmlMenu(player, getObjectId(), 3);
		}
		else if(command.startsWith("change_class"))
		{
			int val = Integer.parseInt(command.substring(13));

			if (checkAndChangeClass(player, val))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
				html.setFile("data/html/classmaster/ok.htm");
				html.replace("%name%", CharTemplateTable.getInstance().getClassNameById(val));
				player.sendPacket(html);
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}

	public static final void onTutorialLink(L2PcInstance player, String request)
	{
		if (!Config.ALTERNATE_CLASS_MASTER
				|| request == null
				|| !request.startsWith("CO"))
			return;

		if (!player.getFloodProtectors().getServerBypass().tryPerformAction("changeclass"))
			return;

		try
		{
			int val = Integer.parseInt(request.substring(2));
			checkAndChangeClass(player, val);
		}
		catch (NumberFormatException e)
		{
		}
		player.sendPacket(new TutorialCloseHtml());
	}

	public static final void onTutorialQuestionMark(L2PcInstance player, int number)
	{
		if (!Config.ALTERNATE_CLASS_MASTER || number != 1001)
			return;

		showTutorialHtml(player);
	}

	public static final void showQuestionMark(L2PcInstance player)
	{
		if (!Config.ALTERNATE_CLASS_MASTER)
			return;

		final ClassId classId = player.getClassId();
		if (getMinLevel(classId.level()) > player.getLevel())
			return;

		player.sendPacket(new TutorialShowQuestionMark(1001));
	}

	private static final void showHtmlMenu(L2PcInstance player, int objectId, int level)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(objectId);

		if (!Config.ALLOW_CLASS_MASTERS)
		{
			html.setFile("data/html/classmaster/disabled.htm");
		}
		else
		{
			final ClassId currentClassId = player.getClassId();
			if (currentClassId.level() >= level)
			{
				html.setFile("data/html/classmaster/nomore.htm");
			}
			else
			{
				final int minLevel = getMinLevel(currentClassId.level());
				if (player.getLevel() >= minLevel || Config.ALLOW_ENTIRE_TREE)
				{
					final StringBuilder menu = new StringBuilder(100);
					for (ClassId cid : ClassId.values())
					{
						if (validateClassId(currentClassId, cid) && cid.level() == level)
						{
							StringUtil.append(menu,
									"<a action=\"bypass -h npc_%objectId%_change_class ",
									String.valueOf(cid.getId()),
									"\">",
									CharTemplateTable.getInstance().getClassNameById(cid.getId()),
									"</a><br>"
									);
						}
					}

					if (menu.length() > 0)
					{
						html.setFile("data/html/classmaster/template.htm");
						html.replace("%name%", CharTemplateTable.getInstance().getClassNameById(currentClassId.getId()));
						html.replace("%menu%", menu.toString());
					}
					else
					{
						html.setFile("data/html/classmaster/comebacklater.htm");
						html.replace("%level%", String.valueOf(getMinLevel(level - 1)));
					}
				}
				else	
				{
					if (minLevel < Integer.MAX_VALUE)
					{
						html.setFile("data/html/classmaster/comebacklater.htm");
						html.replace("%level%", String.valueOf(minLevel));
					}
					else
						html.setFile("data/html/classmaster/nomore.htm");
				}
			}
		}

		html.replace("%objectId%", String.valueOf(objectId));
		player.sendPacket(html);
	}

	private static final void showTutorialHtml(L2PcInstance player)
	{
		final ClassId currentClassId = player.getClassId();
		if (getMinLevel(currentClassId.level()) > player.getLevel()
				&& !Config.ALLOW_ENTIRE_TREE)
			return;

		String msg = HtmCache.getInstance().getHtm("data/html/classmaster/tutorialtemplate.htm");

		msg = msg.replaceAll("%name%", CharTemplateTable.getInstance().getClassNameById(currentClassId.getId()));

		final StringBuilder menu = new StringBuilder(100);
		for (ClassId cid : ClassId.values())
		{
			if (validateClassId(currentClassId, cid))
			{
				StringUtil.append(menu,
						"<a action=\"link CO",
						String.valueOf(cid.getId()),
						"\">",
						CharTemplateTable.getInstance().getClassNameById(cid.getId()),
						"</a><br>"
						);
			}
		}

		msg = msg.replaceAll("%menu%", menu.toString());
		player.sendPacket(new TutorialShowHtml(msg));
	}

	private static final boolean checkAndChangeClass(L2PcInstance player, int val)
	{
		final ClassId currentClassId = player.getClassId();
		if (getMinLevel(currentClassId.level()) > player.getLevel()
				&& !Config.ALLOW_ENTIRE_TREE)
			return false;

		if (!validateClassId(currentClassId, val))
			return false;

		player.setClassId(val);

		if (player.isSubClassActive())
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		else
			player.setBaseClass(player.getActiveClass());

		player.broadcastUserInfo();
		return true;
	}

	/**
	 * Returns minimum player level required for next class transfer
	 * @param level - current skillId level (0 - start, 1 - first, etc)
	 */
	private static final int getMinLevel(int level)
	{
		switch (level)
		{
			case 0:
				return 20;
			case 1:
				return 40;
			case 2:
				return 76;
			default:
				return Integer.MAX_VALUE;
		}
	}

	/**
	 * Returns true if class change is possible
	 * @param oldCID current player ClassId
	 * @param val new class index
	 * @return
	 */
	private static final boolean validateClassId(ClassId oldCID, int val)
	{
		try
		{
			return validateClassId(oldCID, ClassId.values()[val]);
		}
		catch (Exception e)
		{
			// possible ArrayOutOfBoundsException
		}
		return false;
	}

	/**
	 * Returns true if class change is possible
	 * @param oldCID current player ClassId
	 * @param newCID new ClassId
	 * @return true if class change is possible
	 */
	private static final boolean validateClassId(ClassId oldCID, ClassId newCID)
	{
		if (newCID == null || newCID.getRace() == null)
			return false;

		if (oldCID.equals(newCID.getParent()))
			return true;

		if (Config.ALLOW_ENTIRE_TREE
				&& newCID.childOf(oldCID))
			return true;

		return false;
	}
}
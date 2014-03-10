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

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import ct23.xtreme.Base64;
import ct23.xtreme.Config;
import ct23.xtreme.L2DatabaseFactory;
import ct23.xtreme.gameserver.Announcements;
import ct23.xtreme.gameserver.GmListTable;
import ct23.xtreme.gameserver.SevenSigns;
import ct23.xtreme.gameserver.TaskPriority;
import ct23.xtreme.gameserver.cache.HtmCache;
import ct23.xtreme.gameserver.communitybbs.Manager.RegionBBSManager;
import ct23.xtreme.gameserver.datatables.AdminCommandAccessRights;
import ct23.xtreme.gameserver.datatables.MapRegionTable;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.instancemanager.CastleManager;
import ct23.xtreme.gameserver.instancemanager.ClanHallManager;
import ct23.xtreme.gameserver.instancemanager.CoupleManager;
import ct23.xtreme.gameserver.instancemanager.CursedWeaponsManager;
import ct23.xtreme.gameserver.instancemanager.DimensionalRiftManager;
import ct23.xtreme.gameserver.instancemanager.FortManager;
import ct23.xtreme.gameserver.instancemanager.FortSiegeManager;
import ct23.xtreme.gameserver.instancemanager.InstanceManager;
import ct23.xtreme.gameserver.instancemanager.PetitionManager;
import ct23.xtreme.gameserver.instancemanager.QuestManager;
import ct23.xtreme.gameserver.instancemanager.SiegeManager;
import ct23.xtreme.gameserver.model.L2Clan;
import ct23.xtreme.gameserver.model.L2ItemInstance;
import ct23.xtreme.gameserver.model.L2World;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.instance.L2ClassMasterInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.entity.ClanHall;
import ct23.xtreme.gameserver.model.entity.Couple;
import ct23.xtreme.gameserver.model.entity.Fort;
import ct23.xtreme.gameserver.model.entity.FortSiege;
import ct23.xtreme.gameserver.model.entity.Hero;
import ct23.xtreme.gameserver.model.entity.L2Event;
import ct23.xtreme.gameserver.model.entity.Siege;
import ct23.xtreme.gameserver.model.entity.TvTEvent;
import ct23.xtreme.gameserver.model.olympiad.Olympiad;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.Die;
import ct23.xtreme.gameserver.network.serverpackets.EtcStatusUpdate;
import ct23.xtreme.gameserver.network.serverpackets.ExBasicActionList;
import ct23.xtreme.gameserver.network.serverpackets.ExBrExtraUserInfo;
import ct23.xtreme.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import ct23.xtreme.gameserver.network.serverpackets.ExStorageMaxCount;
import ct23.xtreme.gameserver.network.serverpackets.FriendList;
import ct23.xtreme.gameserver.network.serverpackets.HennaInfo;
import ct23.xtreme.gameserver.network.serverpackets.ItemList;
import ct23.xtreme.gameserver.network.serverpackets.NpcHtmlMessage;
import ct23.xtreme.gameserver.network.serverpackets.PledgeShowMemberListAll;
import ct23.xtreme.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import ct23.xtreme.gameserver.network.serverpackets.PledgeSkillList;
import ct23.xtreme.gameserver.network.serverpackets.PledgeStatusChanged;
import ct23.xtreme.gameserver.network.serverpackets.QuestList;
import ct23.xtreme.gameserver.network.serverpackets.ShortCutInit;
import ct23.xtreme.gameserver.network.serverpackets.SkillCoolTime;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.network.serverpackets.UserInfo;

/**
 * Enter World Packet Handler<p>
 * <p>
 * 0000: 03 <p>
 * packet format rev656 cbdddd
 * <p>
 */
public class EnterWorld extends L2GameClientPacket
{
	private static final String _C__03_ENTERWORLD = "[C] 03 EnterWorld";

	private static Logger _log = Logger.getLogger(EnterWorld.class.getName());

	public TaskPriority getPriority()
	{
		return TaskPriority.PR_URGENT;
	}

	@Override
	protected void readImpl()
	{
		// This is just a trigger packet. It has no content.
		/*
		readD();
		readD();
		readD();
		readD();
		readB();
		readD();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		readC();
		*/
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if (activeChar == null)
		{
			_log.warning("EnterWorld failed! activeChar returned 'null'.");
			getClient().closeNow();
			return;
		}

		// Restore to instanced area if enabled
		if (Config.RESTORE_PLAYER_INSTANCE)
			activeChar.setInstanceId(InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId()));
		else
		{
			int instanceId = InstanceManager.getInstance().getPlayerInstance(activeChar.getObjectId());
			if (instanceId > 0)
			InstanceManager.getInstance().getInstance(instanceId).removePlayer(activeChar.getObjectId());
		}

		if (L2World.getInstance().findObject(activeChar.getObjectId()) != null)
		{
			if (Config.DEBUG)
				_log.warning("User already exists in Object ID map! User "+activeChar.getName()+" is a character clone.");
		}

		// Apply special GM properties to the GM when entering
		if (activeChar.isGM())
		{
			if (Config.GM_STARTUP_INVULNERABLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invul", activeChar.getAccessLevel()))
				activeChar.setIsInvul(true);

			if (Config.GM_STARTUP_INVISIBLE && AdminCommandAccessRights.getInstance().hasAccess("admin_invisible", activeChar.getAccessLevel()))
				activeChar.getAppearance().setInvisible();
 
			if (Config.GM_STARTUP_SILENCE && AdminCommandAccessRights.getInstance().hasAccess("admin_silence", activeChar.getAccessLevel()))
				activeChar.setMessageRefusal(true);

			if (Config.GM_STARTUP_DIET_MODE && AdminCommandAccessRights.getInstance().hasAccess("admin_diet", activeChar.getAccessLevel()))
			{
				activeChar.setDietMode(true);
				activeChar.refreshOverloaded();
			}
 
			if (Config.GM_STARTUP_AUTO_LIST && AdminCommandAccessRights.getInstance().hasAccess("admin_gmliston", activeChar.getAccessLevel()))
				GmListTable.getInstance().addGm(activeChar, false);
			else
				GmListTable.getInstance().addGm(activeChar, true);
		}

		// Set dead status if applies
		if (activeChar.getCurrentHp() < 0.5)
			activeChar.setIsDead(true);
		
		// Set Hero status if it applies
		if (Hero.getInstance().getHeroes() != null && Hero.getInstance().getHeroes().containsKey(activeChar.getObjectId()))
			activeChar.setHero(true);

		setPledgeClass(activeChar);

		boolean showClanNotice = false;

		// Clan related checks are here
		if (activeChar.getClan() != null)
		{
			activeChar.sendPacket(new PledgeSkillList(activeChar.getClan()));

			notifyClanMembers(activeChar);

			notifySponsorOrApprentice(activeChar);

			ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(activeChar.getClan());

			if (clanHall != null)
			{
				if (!clanHall.getPaid())
					activeChar.sendPacket(new SystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW));
			}

			for (Siege siege : SiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;

				if (siege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte)1);

				else if (siege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte)2);
			}

			for (FortSiege siege : FortSiegeManager.getInstance().getSieges())
			{
				if (!siege.getIsInProgress())
					continue;

				if (siege.checkIsAttacker(activeChar.getClan()))
					activeChar.setSiegeState((byte)1);

				else if (siege.checkIsDefender(activeChar.getClan()))
					activeChar.setSiegeState((byte)2);
			}
			
			sendPacket(new PledgeShowMemberListAll(activeChar.getClan(), activeChar));
			sendPacket(new PledgeStatusChanged(activeChar.getClan()));
			
			// Residential skills support
			if (activeChar.getClan().getHasCastle() > 0)
				CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
			
			if (activeChar.getClan().getHasFort() > 0)
				FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);

			showClanNotice = activeChar.getClan().isNoticeEnabled();
		}

		// Updating Seal of Strife Buff/Debuff 
		if (SevenSigns.getInstance().isSealValidationPeriod() && SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) != SevenSigns.CABAL_NULL)
		{
			if (SevenSigns.getInstance().getPlayerCabal(activeChar) != SevenSigns.CABAL_NULL)
			{
				if (SevenSigns.getInstance().getPlayerCabal(activeChar) == SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE))
					activeChar.addSkill(SkillTable.getInstance().getInfo(5074,1));
				else
					activeChar.addSkill(SkillTable.getInstance().getInfo(5075,1));
			}
		}
		else
		{
			activeChar.removeSkill(SkillTable.getInstance().getInfo(5074,1));
			activeChar.removeSkill(SkillTable.getInstance().getInfo(5075,1));
		}

		if (Config.ENABLE_VITALITY && Config.RECOVER_VITALITY_ON_RECONNECT)
		{
			float points = Config.RATE_RECOVERY_ON_RECONNECT * (System.currentTimeMillis() - activeChar.getLastAccess()) / 60000;
			if (points > 0)
				activeChar.updateVitalityPoints(points, false, true);
		}

		activeChar.sendPacket(new UserInfo(activeChar));

		sendPacket(new ExBrExtraUserInfo(activeChar));

		// Send Macro List
		activeChar.getMacroses().sendUpdate();

		// Send Item List
		sendPacket(new ItemList(activeChar, false));

		// Send GG check
		activeChar.queryGameGuard();

		// Send Teleport Bookmark List
		sendPacket(new ExGetBookMarkInfoPacket(activeChar));
		
		// Send Shortcuts
		sendPacket(new ShortCutInit(activeChar));

		// Send Action list
		activeChar.sendPacket(ExBasicActionList.DEFAULT_ACTION_LIST);

		// Send Skill list
		activeChar.sendSkillList();

		// Send Dye Information
		activeChar.sendPacket(new HennaInfo(activeChar));

		Quest.playerEnter(activeChar);
		loadTutorial(activeChar);
		for (Quest quest : QuestManager.getInstance().getAllManagedScripts())
		{
			if (quest != null && quest.getOnEnterWorld())
				quest.notifyEnterWorld(activeChar);
		}
		activeChar.sendPacket(new QuestList());

		if (Config.PLAYER_SPAWN_PROTECTION > 0)
			activeChar.setProtection(true);

		activeChar.spawnMe(activeChar.getX(), activeChar.getY(), activeChar.getZ());

		if (L2Event.active && L2Event.connectionLossData.containsKey(activeChar.getName()) && L2Event.isOnEvent(activeChar))
			L2Event.restoreChar(activeChar);
		else if (L2Event.connectionLossData.containsKey(activeChar.getName()))
			L2Event.restoreAndTeleChar(activeChar);

		// Wedding Checks
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			engage(activeChar);
			notifyPartner(activeChar,activeChar.getPartnerId());
		}

		if (activeChar.isCursedWeaponEquipped()) 
		{
			CursedWeaponsManager.getInstance().getCursedWeapon(activeChar.getCursedWeaponEquippedId()).cursedOnLogin();
		}

		activeChar.updateEffectIcons();

		activeChar.sendPacket(new EtcStatusUpdate(activeChar));

		//Expand Skill
		activeChar.sendPacket(new ExStorageMaxCount(activeChar));

		sendPacket(new FriendList(activeChar));

		sendPacket(new SystemMessage(SystemMessageId.WELCOME_TO_LINEAGE));

		activeChar.sendMessage(getText("VGhpcyBzZXJ2ZXIgdXNlcyBMMkosIGEgcHJvamVjdCBmb3VuZGVkIGJ5IEwyQ2hlZg==\n"));
		activeChar.sendMessage(getText("YW5kIGRldmVsb3BlZCBieSB0aGUgTDJKIERldiBUZWFtIGF0IGwyanNlcnZlci5jb20=\n"));

		if (Config.DISPLAY_SERVER_VERSION)
		{
			if (Config.SERVER_VERSION != null)
				activeChar.sendMessage(getText("TDJKIFNlcnZlciBWZXJzaW9uOg==")+" "+Config.SERVER_VERSION);

			if (Config.DATAPACK_VERSION != null)
				activeChar.sendMessage(getText("TDJKIERhdGFwYWNrIFZlcnNpb246")+" "+Config.DATAPACK_VERSION);
		}
		activeChar.sendMessage(getText("Q29weXJpZ2h0IDIwMDQtMjAwOQ==\n"));

		SevenSigns.getInstance().sendCurrentPeriodMsg(activeChar);
		Announcements.getInstance().showAnnouncements(activeChar);

		if (showClanNotice)
		{
			NpcHtmlMessage notice = new NpcHtmlMessage(1);
			notice.setFile("data/html/clanNotice.htm");
			notice.replace("%clan_name%", activeChar.getClan().getName());
			notice.replace("%notice_text%", activeChar.getClan().getNotice().replaceAll("\r\n", "<br>"));
			sendPacket(notice);
		}
		else if (Config.SERVER_NEWS)
		{
			String serverNews = HtmCache.getInstance().getHtm("data/html/servnews.htm");
			if (serverNews != null)
				sendPacket(new NpcHtmlMessage(1, serverNews));
		}

		if (Config.PETITIONING_ALLOWED)
			PetitionManager.getInstance().checkPetitionMessages(activeChar);

		if (activeChar.isAlikeDead()) // dead or fake dead
		{
			// no broadcast needed since the player will already spawn dead to others
			sendPacket(new Die(activeChar));
		}

		notifyFriends(activeChar);

		activeChar.onPlayerEnter();

		sendPacket(new SkillCoolTime(activeChar));

		for (L2ItemInstance i : activeChar.getInventory().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}
		
		for (L2ItemInstance i : activeChar.getWarehouse().getItems())
		{
			if (i.isTimeLimitedItem())
			{
				i.scheduleLifeTimeTask();
			}
		}
		
		if (Olympiad.getInstance().playerInStadia(activeChar))
		{
			activeChar.doRevive();
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}

		if (DimensionalRiftManager.getInstance().checkIfInRiftZone(activeChar.getX(), activeChar.getY(), activeChar.getZ(), false))
			DimensionalRiftManager.getInstance().teleportToWaitingRoom(activeChar);

		if (activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED));

		// remove combat flag before teleporting
		if (activeChar.getInventory().getItemByItemId(9819) != null)
		{
			Fort fort = FortManager.getInstance().getFort(activeChar);

			if (fort != null)
				FortSiegeManager.getInstance().dropCombatFlag(activeChar);
			else
			{
				int slot = activeChar.getInventory().getSlotFromItem(activeChar.getInventory().getItemByItemId(9819));
				activeChar.getInventory().unEquipItemInBodySlotAndRecord(slot);
				activeChar.destroyItem("CombatFlag", activeChar.getInventory().getItemByItemId(9819), null, true);
			}
		}

		// Attacker or spectator logging in to a siege zone. Actually should be checked for inside castle only?
		if (!activeChar.isGM()
				// inside siege zone
				&& activeChar.isInsideZone(L2Character.ZONE_SIEGE)
				// but non-participant or attacker
				&& (!activeChar.isInSiege() || activeChar.getSiegeState() < 2))
			activeChar.teleToLocation(MapRegionTable.TeleportWhereType.Town);

		RegionBBSManager.getInstance().changeCommunityBoard();

		TvTEvent.onLogin(activeChar);

		L2ClassMasterInstance.showQuestionMark(activeChar);
	}

	/**
	* @param activeChar
	*/
	private void engage(L2PcInstance cha)
	{
		int _chaid = cha.getObjectId();

		for(Couple cl: CoupleManager.getInstance().getCouples())
		{
			if (cl.getPlayer1Id()==_chaid || cl.getPlayer2Id()==_chaid)
			{
				if (cl.getMaried())
					cha.setMarried(true);

				cha.setCoupleId(cl.getId());

				if (cl.getPlayer1Id()==_chaid)
					cha.setPartnerId(cl.getPlayer2Id());

				else
					cha.setPartnerId(cl.getPlayer1Id());
			}
		}
	}

	/**
	* @param activeChar partnerid
	*/
	private void notifyPartner(L2PcInstance cha,int partnerId)
	{
		if (cha.getPartnerId()!=0)
		{
			L2PcInstance partner;
			int objId = cha.getPartnerId();

			try
			{
				partner = (L2PcInstance)L2World.getInstance().findObject(cha.getPartnerId());

				if (partner != null)
					partner.sendMessage("Your Partner has logged in.");

				partner = null;
			}
			catch (ClassCastException cce)
			{
				_log.warning("Wedding Error: ID "+objId+" is now owned by a(n) "+L2World.getInstance().findObject(objId).getClass().getSimpleName());
			}
		}
	}

	/**
	* @param activeChar
	*/
	private void notifyFriends(L2PcInstance cha)
	{
		Connection con = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement;
			statement = con.prepareStatement("SELECT friend_name FROM character_friends WHERE charId=?");
			statement.setInt(1, cha.getObjectId());
			ResultSet rset = statement.executeQuery();

			L2PcInstance friend;
			String friendName;

			SystemMessage sm = new SystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN);
			sm.addString(cha.getName());

			while (rset.next())
			{
				friendName = rset.getString("friend_name");

				friend = L2World.getInstance().getPlayer(friendName);

				if (friend != null) //friend logged in.
				{
					friend.sendPacket(new FriendList(friend));
					friend.sendPacket(sm);
				}
			}
			sm = null;

			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error restoring friend data.", e);
		}
		finally
		{
			try {con.close();} catch (Exception e){}
		}
	}

	/**
	* @param activeChar
	*/
	private void notifyClanMembers(L2PcInstance activeChar)
	{
		L2Clan clan = activeChar.getClan();

		// This null check may not be needed anymore since notifyClanMembers is called from within a null check already. Please remove if we're certain it's ok to do so.
		if (clan != null)
		{
			clan.getClanMember(activeChar.getObjectId()).setPlayerInstance(activeChar);
			SystemMessage msg = new SystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN);
			msg.addString(activeChar.getName());
			clan.broadcastToOtherOnlineMembers(msg, activeChar);
			msg = null;
			clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(activeChar), activeChar);
		}
	}

	/**
	* @param activeChar
	*/
	private void notifySponsorOrApprentice(L2PcInstance activeChar)
	{
		if (activeChar.getSponsor() != 0)
		{
			L2PcInstance sponsor = (L2PcInstance)L2World.getInstance().findObject(activeChar.getSponsor());

			if (sponsor != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				sponsor.sendPacket(msg);
			}
		}
		else if (activeChar.getApprentice() != 0)
		{
			L2PcInstance apprentice = (L2PcInstance)L2World.getInstance().findObject(activeChar.getApprentice());

			if (apprentice != null)
			{
				SystemMessage msg = new SystemMessage(SystemMessageId.YOUR_SPONSOR_C1_HAS_LOGGED_IN);
				msg.addString(activeChar.getName());
				apprentice.sendPacket(msg);
			}
		}
	}

	/**
	* @param string
	* @return
	* @throws UnsupportedEncodingException
	*/
	private String getText(String string)
	{
		try
		{
			String result = new String(Base64.decode(string), "UTF-8");
			return result;
		}
		catch (UnsupportedEncodingException e)
		{
			return null;
		}
	}

	private void loadTutorial(L2PcInstance player)
	{
		QuestState qs = player.getQuestState("255_Tutorial");

		if (qs != null)
			qs.getQuest().notifyEvent("UC", null, player);
	}

	/* (non-Javadoc)
	* @see ct23.xtreme.gameserver.clientpackets.ClientBasePacket#getType()
	*/
	@Override
	public String getType()
	{
		return _C__03_ENTERWORLD;
	}

	private void setPledgeClass(L2PcInstance activeChar)
	{
		int pledgeClass = 0;

		// This null check may not be needed anymore since setPledgeClass is called from within a null check already. Please remove if we're certain it's ok to do so.
		if (activeChar.getClan() != null)
			pledgeClass = activeChar.getClan().getClanMember(activeChar.getObjectId()).calculatePledgeClass(activeChar);

		if (activeChar.isNoble() && pledgeClass < 5)
			pledgeClass = 5;

		if (activeChar.isHero() && pledgeClass < 8)
			pledgeClass = 8;

		activeChar.setPledgeClass(pledgeClass);
	}
}
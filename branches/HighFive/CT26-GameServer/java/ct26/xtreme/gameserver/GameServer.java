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
package ct26.xtreme.gameserver;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mmocore.network.SelectorConfig;
import org.mmocore.network.SelectorThread;

import ct26.xtreme.Config;
import ct26.xtreme.L2DatabaseFactory;
import ct26.xtreme.Server;
import ct26.xtreme.UPnPService;
import ct26.xtreme.gameserver.cache.HtmCache;
import ct26.xtreme.gameserver.datatables.AdminTable;
import ct26.xtreme.gameserver.datatables.ArmorSetsData;
import ct26.xtreme.gameserver.datatables.AugmentationData;
import ct26.xtreme.gameserver.datatables.BotReportTable;
import ct26.xtreme.gameserver.datatables.BuyListData;
import ct26.xtreme.gameserver.datatables.CategoryData;
import ct26.xtreme.gameserver.datatables.CharNameTable;
import ct26.xtreme.gameserver.datatables.CharSummonTable;
import ct26.xtreme.gameserver.datatables.CharTemplateTable;
import ct26.xtreme.gameserver.datatables.ClanTable;
import ct26.xtreme.gameserver.datatables.ClassListData;
import ct26.xtreme.gameserver.datatables.CrestTable;
import ct26.xtreme.gameserver.datatables.DoorTable;
import ct26.xtreme.gameserver.datatables.EnchantItemData;
import ct26.xtreme.gameserver.datatables.EnchantItemGroupsData;
import ct26.xtreme.gameserver.datatables.EnchantItemHPBonusData;
import ct26.xtreme.gameserver.datatables.EnchantItemOptionsData;
import ct26.xtreme.gameserver.datatables.EnchantSkillGroupsData;
import ct26.xtreme.gameserver.datatables.EventDroplist;
import ct26.xtreme.gameserver.datatables.ExperienceTable;
import ct26.xtreme.gameserver.datatables.FishData;
import ct26.xtreme.gameserver.datatables.FishingMonstersData;
import ct26.xtreme.gameserver.datatables.FishingRodsData;
import ct26.xtreme.gameserver.datatables.HennaData;
import ct26.xtreme.gameserver.datatables.HitConditionBonus;
import ct26.xtreme.gameserver.datatables.InitialEquipmentData;
import ct26.xtreme.gameserver.datatables.InitialShortcutData;
import ct26.xtreme.gameserver.datatables.ItemTable;
import ct26.xtreme.gameserver.datatables.KarmaData;
import ct26.xtreme.gameserver.datatables.ManorData;
import ct26.xtreme.gameserver.datatables.MerchantPriceConfigTable;
import ct26.xtreme.gameserver.datatables.MultisellData;
import ct26.xtreme.gameserver.datatables.NpcBufferTable;
import ct26.xtreme.gameserver.datatables.NpcData;
import ct26.xtreme.gameserver.datatables.OfflineTradersTable;
import ct26.xtreme.gameserver.datatables.OptionsData;
import ct26.xtreme.gameserver.datatables.PetDataTable;
import ct26.xtreme.gameserver.datatables.RecipeData;
import ct26.xtreme.gameserver.datatables.SecondaryAuthData;
import ct26.xtreme.gameserver.datatables.SiegeScheduleData;
import ct26.xtreme.gameserver.datatables.SkillData;
import ct26.xtreme.gameserver.datatables.SkillLearnData;
import ct26.xtreme.gameserver.datatables.SkillTreesData;
import ct26.xtreme.gameserver.datatables.SpawnTable;
import ct26.xtreme.gameserver.datatables.StaticObjects;
import ct26.xtreme.gameserver.datatables.SummonSkillsTable;
import ct26.xtreme.gameserver.datatables.TeleportLocationTable;
import ct26.xtreme.gameserver.datatables.TransformData;
import ct26.xtreme.gameserver.datatables.UIData;
import ct26.xtreme.gameserver.geoeditorcon.GeoEditorListener;
import ct26.xtreme.gameserver.handler.EffectHandler;
import ct26.xtreme.gameserver.idfactory.IdFactory;
import ct26.xtreme.gameserver.instancemanager.AirShipManager;
import ct26.xtreme.gameserver.instancemanager.AntiFeedManager;
import ct26.xtreme.gameserver.instancemanager.AuctionManager;
import ct26.xtreme.gameserver.instancemanager.BoatManager;
import ct26.xtreme.gameserver.instancemanager.CHSiegeManager;
import ct26.xtreme.gameserver.instancemanager.CastleManager;
import ct26.xtreme.gameserver.instancemanager.CastleManorManager;
import ct26.xtreme.gameserver.instancemanager.ClanHallManager;
import ct26.xtreme.gameserver.instancemanager.CoupleManager;
import ct26.xtreme.gameserver.instancemanager.CursedWeaponsManager;
import ct26.xtreme.gameserver.instancemanager.DayNightSpawnManager;
import ct26.xtreme.gameserver.instancemanager.DimensionalRiftManager;
import ct26.xtreme.gameserver.instancemanager.FortManager;
import ct26.xtreme.gameserver.instancemanager.FortSiegeManager;
import ct26.xtreme.gameserver.instancemanager.FourSepulchersManager;
import ct26.xtreme.gameserver.instancemanager.GlobalVariablesManager;
import ct26.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct26.xtreme.gameserver.instancemanager.GrandBossManager;
import ct26.xtreme.gameserver.instancemanager.HellboundManager;
import ct26.xtreme.gameserver.instancemanager.InstanceManager;
import ct26.xtreme.gameserver.instancemanager.ItemAuctionManager;
import ct26.xtreme.gameserver.instancemanager.ItemsOnGroundManager;
import ct26.xtreme.gameserver.instancemanager.MailManager;
import ct26.xtreme.gameserver.instancemanager.MapRegionManager;
import ct26.xtreme.gameserver.instancemanager.MercTicketManager;
import ct26.xtreme.gameserver.instancemanager.PetitionManager;
import ct26.xtreme.gameserver.instancemanager.PunishmentManager;
import ct26.xtreme.gameserver.instancemanager.QuestManager;
import ct26.xtreme.gameserver.instancemanager.RaidBossPointsManager;
import ct26.xtreme.gameserver.instancemanager.RaidBossSpawnManager;
import ct26.xtreme.gameserver.instancemanager.SiegeManager;
import ct26.xtreme.gameserver.instancemanager.TerritoryWarManager;
import ct26.xtreme.gameserver.instancemanager.WalkingManager;
import ct26.xtreme.gameserver.instancemanager.ZoneManager;
import ct26.xtreme.gameserver.model.AutoSpawnHandler;
import ct26.xtreme.gameserver.model.L2World;
import ct26.xtreme.gameserver.model.PartyMatchRoomList;
import ct26.xtreme.gameserver.model.PartyMatchWaitingList;
import ct26.xtreme.gameserver.model.entity.Hero;
import ct26.xtreme.gameserver.model.entity.TvTManager;
import ct26.xtreme.gameserver.model.olympiad.Olympiad;
import ct26.xtreme.gameserver.network.L2GameClient;
import ct26.xtreme.gameserver.network.L2GamePacketHandler;
import ct26.xtreme.gameserver.network.communityserver.CommunityServerThread;
import ct26.xtreme.gameserver.pathfinding.PathFinding;
import ct26.xtreme.gameserver.script.faenor.FaenorScriptEngine;
import ct26.xtreme.gameserver.scripting.L2ScriptEngineManager;
import ct26.xtreme.gameserver.taskmanager.AutoAnnounceTaskManager;
import ct26.xtreme.gameserver.taskmanager.KnownListUpdateTaskManager;
import ct26.xtreme.gameserver.taskmanager.TaskManager;
import ct26.xtreme.status.Status;
import ct26.xtreme.util.DeadLockDetector;
import ct26.xtreme.util.IPv4Filter;

public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private final LoginServerThread _loginThread;
	private static Status _statusServer;
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	
	public long getUsedMemoryMB()
	{
		return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576; // ;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
	
	public L2GamePacketHandler getL2GamePacketHandler()
	{
		return _gamePacketHandler;
	}
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		_log.finest(getClass().getSimpleName() + ": used mem:" + getUsedMemoryMB() + "MB");
		
		if (Config.SERVER_VERSION != null)
		{
			_log.info(getClass().getSimpleName() + ": HighFive Server Version:    " + Config.SERVER_VERSION);
		}
		if (Config.DATAPACK_VERSION != null)
		{
			_log.info(getClass().getSimpleName() + ": HighFive Datapack Version:  " + Config.DATAPACK_VERSION);
		}
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe(getClass().getSimpleName() + ": Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.init();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Data");
		CategoryData.getInstance();
		SecondaryAuthData.getInstance();
		
		printSection("Skills");
		EffectHandler.getInstance().executeScript();
		EnchantSkillGroupsData.getInstance();
		SkillTreesData.getInstance();
		SkillData.getInstance();
		SummonSkillsTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		EnchantItemGroupsData.getInstance();
		EnchantItemData.getInstance();
		EnchantItemOptionsData.getInstance();
		OptionsData.getInstance();
		EnchantItemHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		BuyListData.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetsData.getInstance();
		FishData.getInstance();
		FishingMonstersData.getInstance();
		FishingRodsData.getInstance();
		HennaData.getInstance();
		
		printSection("Characters");
		ClassListData.getInstance();
		InitialEquipmentData.getInstance();
		InitialShortcutData.getInstance();
		ExperienceTable.getInstance();
		KarmaData.getInstance();
		HitConditionBonus.getInstance();
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		AdminTable.getInstance();
		RaidBossPointsManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance().init();
		
		printSection("Clans");
		ClanTable.getInstance();
		CHSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
		{
			PathFinding.getInstance();
		}
		
		printSection("NPCs");
		SkillLearnData.getInstance();
		NpcData.getInstance();
		WalkingManager.getInstance();
		StaticObjects.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		HellboundManager.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeScheduleData.getInstance();
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		ManorData.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		printSection("Seven Signs");
		SevenSigns.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestTable.getInstance();
		TeleportLocationTable.getInstance();
		UIData.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		TransformData.getInstance();
		BotReportTable.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		GraciaSeedsManager.getInstance();
		
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		MerchantPriceConfigTable.getInstance().updateReferences();
		
		try
		{
			_log.info(getClass().getSimpleName() + ": Loading server scripts:");
			final File scripts = new File(Config.DATAPACK_ROOT, "data/scripts.cfg");
			if (!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
			{
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
			}
		}
		catch (IOException ioe)
		{
			_log.severe(getClass().getSimpleName() + ": Failed loading scripts.cfg, scripts are not going to be loaded!");
		}
		
		QuestManager.getInstance().report();
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}
		
		if ((Config.AUTODESTROY_ITEM_AFTER > 0) || (Config.HERB_AUTO_DESTROY_TIME > 0))
		{
			ItemsAutoDestroy.getInstance();
		}
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		if (Config.L2JMOD_ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
		}
		
		TaskManager.getInstance();
		
		AntiFeedManager.getInstance().registerEvent(AntiFeedManager.GAME_ID);
		
		if (Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
		{
			GeoEditorListener.getInstance();
		}
		
		PunishmentManager.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		TvTManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.getInstance().restoreOfflineTraders();
		}
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the allocation pool
		long freeMem = ((Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory()) + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info(getClass().getSimpleName() + ": Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		Toolkit.getDefaultToolkit().beep();
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;
		
		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, getClass().getSimpleName() + ": WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
			_selectorThread.start();
			_log.log(Level.INFO, getClass().getSimpleName() + ": is now listening on: " + Config.GAMESERVER_HOSTNAME + ":" + Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, getClass().getSimpleName() + ": FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		
		_log.log(Level.INFO, getClass().getSimpleName() + ": Maximum numbers of connected players: " + Config.MAXIMUM_ONLINE_USERS);
		_log.log(Level.INFO, getClass().getSimpleName() + ": Server loaded in " + ((System.currentTimeMillis() - serverLoadStart) / 1000) + " seconds.");
		
		printSection("UPnP");
		UPnPService.getInstance();
		
		AutoAnnounceTaskManager.getInstance();
	}
	
	public static void main(String[] args) throws Exception
	{
		Server.serverMode = Server.MODE_GAMESERVER;
		// Local Constants
		final String LOG_FOLDER = "log"; // Name of folder for log file
		final String LOG_NAME = "./log.cfg"; // Name of log file
		
		/*** Main ***/
		// Create log folder
		File logFolder = new File(Config.DATAPACK_ROOT, LOG_FOLDER);
		logFolder.mkdir();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File(LOG_NAME)))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		// Initialize config
		Config.load();
		printSection("Database");
		L2DatabaseFactory.getInstance();
		gameServer = new GameServer();
		
		if (Config.IS_TELNET_ENABLED)
		{
			_statusServer = new Status(Server.serverMode);
			_statusServer.start();
		}
		else
		{
			_log.info(GameServer.class.getSimpleName() + ": Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
		{
			s = "-" + s;
		}
		_log.info(s);
	}
}

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

package ct23.xtreme.gameserver;

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

import ct23.xtreme.Config;
import ct23.xtreme.L2DatabaseFactory;
import ct23.xtreme.Server;
import ct23.xtreme.gameserver.cache.CrestCache;
import ct23.xtreme.gameserver.cache.HtmCache;
import ct23.xtreme.gameserver.datatables.AccessLevels;
import ct23.xtreme.gameserver.datatables.AdminCommandAccessRights;
import ct23.xtreme.gameserver.datatables.ArmorSetsTable;
import ct23.xtreme.gameserver.datatables.AugmentationData;
import ct23.xtreme.gameserver.datatables.CharNameTable;
import ct23.xtreme.gameserver.datatables.CharTemplateTable;
import ct23.xtreme.gameserver.datatables.ClanTable;
import ct23.xtreme.gameserver.datatables.DoorTable;
import ct23.xtreme.gameserver.datatables.EnchantGroupsTable;
import ct23.xtreme.gameserver.datatables.EnchantHPBonusData;
import ct23.xtreme.gameserver.datatables.EventDroplist;
import ct23.xtreme.gameserver.datatables.ExtractableItemsData;
import ct23.xtreme.gameserver.datatables.ExtractableSkillsData;
import ct23.xtreme.gameserver.datatables.FishTable;
import ct23.xtreme.gameserver.datatables.GMSkillTable;
import ct23.xtreme.gameserver.datatables.HelperBuffTable;
import ct23.xtreme.gameserver.datatables.HennaTable;
import ct23.xtreme.gameserver.datatables.HennaTreeTable;
import ct23.xtreme.gameserver.datatables.HeroSkillTable;
import ct23.xtreme.gameserver.datatables.ItemTable;
import ct23.xtreme.gameserver.datatables.LevelUpData;
import ct23.xtreme.gameserver.datatables.MapRegionTable;
import ct23.xtreme.gameserver.datatables.MerchantPriceConfigTable;
import ct23.xtreme.gameserver.datatables.NobleSkillTable;
import ct23.xtreme.gameserver.datatables.NpcBufferTable;
import ct23.xtreme.gameserver.datatables.NpcTable;
import ct23.xtreme.gameserver.datatables.NpcWalkerRoutesTable;
import ct23.xtreme.gameserver.datatables.OfflineTradersTable;
import ct23.xtreme.gameserver.datatables.PetDataTable;
import ct23.xtreme.gameserver.datatables.PetSkillsTable;
import ct23.xtreme.gameserver.datatables.ResidentialSkillTable;
import ct23.xtreme.gameserver.datatables.SkillSpellbookTable;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.datatables.SkillTreeTable;
import ct23.xtreme.gameserver.datatables.SpawnTable;
import ct23.xtreme.gameserver.datatables.StaticObjects;
import ct23.xtreme.gameserver.datatables.SummonItemsData;
import ct23.xtreme.gameserver.datatables.TeleportLocationTable;
import ct23.xtreme.gameserver.datatables.UITable;
import ct23.xtreme.gameserver.geoeditorcon.GeoEditorListener;
import ct23.xtreme.gameserver.handler.AdminCommandHandler;
import ct23.xtreme.gameserver.handler.ChatHandler;
import ct23.xtreme.gameserver.handler.ItemHandler;
import ct23.xtreme.gameserver.handler.SkillHandler;
import ct23.xtreme.gameserver.handler.UserCommandHandler;
import ct23.xtreme.gameserver.handler.VoicedCommandHandler;
import ct23.xtreme.gameserver.idfactory.IdFactory;
import ct23.xtreme.gameserver.instancemanager.AirShipManager;
import ct23.xtreme.gameserver.instancemanager.AuctionManager;
import ct23.xtreme.gameserver.instancemanager.BoatManager;
import ct23.xtreme.gameserver.instancemanager.CastleManager;
import ct23.xtreme.gameserver.instancemanager.CastleManorManager;
import ct23.xtreme.gameserver.instancemanager.ClanHallManager;
import ct23.xtreme.gameserver.instancemanager.CoupleManager;
import ct23.xtreme.gameserver.instancemanager.CursedWeaponsManager;
import ct23.xtreme.gameserver.instancemanager.DayNightSpawnManager;
import ct23.xtreme.gameserver.instancemanager.DimensionalRiftManager;
import ct23.xtreme.gameserver.instancemanager.FortManager;
import ct23.xtreme.gameserver.instancemanager.FortSiegeManager;
import ct23.xtreme.gameserver.instancemanager.FourSepulchersManager;
import ct23.xtreme.gameserver.instancemanager.GlobalVariablesManager;
import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct23.xtreme.gameserver.instancemanager.GrandBossManager;
import ct23.xtreme.gameserver.instancemanager.HellboundManager;
import ct23.xtreme.gameserver.instancemanager.InstanceManager;
import ct23.xtreme.gameserver.instancemanager.ItemsOnGroundManager;
import ct23.xtreme.gameserver.instancemanager.MercTicketManager;
import ct23.xtreme.gameserver.instancemanager.PetitionManager;
import ct23.xtreme.gameserver.instancemanager.QuestManager;
import ct23.xtreme.gameserver.instancemanager.RaidBossPointsManager;
import ct23.xtreme.gameserver.instancemanager.RaidBossSpawnManager;
import ct23.xtreme.gameserver.instancemanager.SiegeManager;
import ct23.xtreme.gameserver.instancemanager.TerritoryWarManager;
import ct23.xtreme.gameserver.instancemanager.TransformationManager;
import ct23.xtreme.gameserver.instancemanager.ZoneManager;
import ct23.xtreme.gameserver.model.AutoChatHandler;
import ct23.xtreme.gameserver.model.AutoSpawnHandler;
import ct23.xtreme.gameserver.model.L2Manor;
import ct23.xtreme.gameserver.model.L2Multisell;
import ct23.xtreme.gameserver.model.L2World;
import ct23.xtreme.gameserver.model.PartyMatchRoomList;
import ct23.xtreme.gameserver.model.PartyMatchWaitingList;
import ct23.xtreme.gameserver.model.entity.Hero;
import ct23.xtreme.gameserver.model.entity.TvTManager;
import ct23.xtreme.gameserver.model.olympiad.Olympiad;
import ct23.xtreme.gameserver.network.L2GameClient;
import ct23.xtreme.gameserver.network.L2GamePacketHandler;
import ct23.xtreme.gameserver.network.communityserver.CommunityServerThread;
import ct23.xtreme.gameserver.pathfinding.PathFinding;
import ct23.xtreme.gameserver.script.faenor.FaenorScriptEngine;
import ct23.xtreme.gameserver.scripting.CompiledScriptCache;
import ct23.xtreme.gameserver.scripting.L2ScriptEngineManager;
import ct23.xtreme.gameserver.taskmanager.AutoAnnounceTaskManager;
import ct23.xtreme.gameserver.taskmanager.KnownListUpdateTaskManager;
import ct23.xtreme.gameserver.taskmanager.TaskManager;
import ct23.xtreme.gameserver.util.DynamicExtension;
import ct23.xtreme.status.Status;
import ct23.xtreme.util.DeadLockDetector;
import ct23.xtreme.util.IPv4Filter;

/**
 * This class ...
 * 
 * @version $Revision: 1.29.2.15.2.19 $ $Date: 2005/04/05 19:41:23 $
 */
public class GameServer
{
	private static final Logger _log = Logger.getLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	private final DeadLockDetector _deadDetectThread;
	private final IdFactory _idFactory;
	public static GameServer gameServer;
	private LoginServerThread _loginThread;
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
	
	public DeadLockDetector getDeadLockDetectorThread()
	{
		return _deadDetectThread;
	}
	
	public GameServer() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();
		
		gameServer = this;
		_log.finest("used mem:" + getUsedMemoryMB() + "MB");
		
		if (Config.SERVER_VERSION != null)
		{
			_log.info("L2J Server Version:    " + Config.SERVER_VERSION);
		}
		if (Config.DATAPACK_VERSION != null)
		{
			_log.info("L2J Datapack Version:  " + Config.DATAPACK_VERSION);
		}
		
		_idFactory = IdFactory.getInstance();
		
		if (!_idFactory.isInitialized())
		{
			_log.severe("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		
		ThreadPoolManager.getInstance();
		
		new File(Config.DATAPACK_ROOT, "data/crests").mkdirs();
		new File("log/game").mkdirs();
		
		// load script engines
		printSection("Engines");
		L2ScriptEngineManager.getInstance();
		
		printSection("World");
		// start game time control early
		GameTimeController.getInstance();
		InstanceManager.getInstance();
		L2World.getInstance();
		MapRegionTable.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		
		printSection("Skills");
		EnchantGroupsTable.getInstance();
		SkillTable.getInstance();
		SkillTreeTable.getInstance();
		PetSkillsTable.getInstance();
		NobleSkillTable.getInstance();
		GMSkillTable.getInstance();
		HeroSkillTable.getInstance();
		ResidentialSkillTable.getInstance();
		SkillSpellbookTable.getInstance();
		
		printSection("Items");
		ItemTable.getInstance();
		ExtractableItemsData.getInstance();
		ExtractableSkillsData.getInstance();
		SummonItemsData.getInstance();
		EnchantHPBonusData.getInstance();
		MerchantPriceConfigTable.getInstance().loadInstances();
		TradeController.getInstance();
		L2Multisell.getInstance();
		RecipeController.getInstance();
		ArmorSetsTable.getInstance();
		FishTable.getInstance();
		SkillSpellbookTable.getInstance();
		
		printSection("Characters");
		CharTemplateTable.getInstance();
		CharNameTable.getInstance();
		LevelUpData.getInstance();
		AccessLevels.getInstance();
		AdminCommandAccessRights.getInstance();
		GmListTable.getInstance();
		PetDataTable.getInstance().loadPetsData();
		
		printSection("Clans");
		ClanTable.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();
		
		printSection("Geodata");
		GeoData.getInstance();
		if (Config.GEODATA == 2)
			PathFinding.getInstance();
		
		printSection("NPCs");
		NpcTable.getInstance();
		NpcWalkerRoutesTable.getInstance();
		ZoneManager.getInstance();
		DoorTable.getInstance();
		StaticObjects.getInstance();
		CastleManager.getInstance().loadInstances();
		FortManager.getInstance().loadInstances();
		NpcBufferTable.getInstance();
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		RaidBossPointsManager.init();
		FourSepulchersManager.getInstance().init();
		DimensionalRiftManager.getInstance();
		EventDroplist.getInstance();
		
		printSection("Siege");
		SiegeManager.getInstance().getSieges();
		FortSiegeManager.getInstance();
		TerritoryWarManager.getInstance();
		CastleManorManager.getInstance();
		MercTicketManager.getInstance();
		L2Manor.getInstance();
		
		printSection("Olympiad");
		Olympiad.getInstance();
		Hero.getInstance();
		
		// Call to load caches
		printSection("Cache");
		HtmCache.getInstance();
		CrestCache.getInstance();
		TeleportLocationTable.getInstance();
		UITable.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		HennaTable.getInstance();
		HennaTreeTable.getInstance();
		HelperBuffTable.getInstance();
		AugmentationData.getInstance();
		CursedWeaponsManager.getInstance();
		
		printSection("Gracia Seeds");
		GraciaSeedsManager.getInstance();
		
		printSection("Scripts");
		QuestManager.getInstance();
		TransformationManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		
		try
		{
			_log.info("Loading Server Scripts");
			File scripts = new File(Config.DATAPACK_ROOT + "/data/scripts.cfg");
			if(!Config.ALT_DEV_NO_HANDLERS || !Config.ALT_DEV_NO_QUESTS)
				L2ScriptEngineManager.getInstance().executeScriptList(scripts);
		}
		catch (IOException ioe)
		{
			_log.severe("Failed loading scripts.cfg, no script going to be loaded");
		}
		try
		{
			CompiledScriptCache compiledScriptCache = L2ScriptEngineManager.getInstance().getCompiledScriptCache();
			if (compiledScriptCache == null)
			{
				_log.info("Compiled Scripts Cache is disabled.");
			}
			else
			{
				compiledScriptCache.purge();
				
				if (compiledScriptCache.isModified())
				{
					compiledScriptCache.save();
					_log.info("Compiled Scripts Cache was saved.");
				}
				else
				{
					_log.info("Compiled Scripts Cache is up-to-date.");
				}
			}
			
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "Failed to store Compiled Scripts Cache.", e);
		}
		QuestManager.getInstance().report();
		TransformationManager.getInstance().report();

		if (Config.SAVE_DROPPED_ITEM)
			ItemsOnGroundManager.getInstance();
		
		if (Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
			ItemsAutoDestroy.getInstance();
		
		MonsterRace.getInstance();
		
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		AutoSpawnHandler.getInstance();
		AutoChatHandler.getInstance();
		
		FaenorScriptEngine.getInstance();
		// Init of a cursed weapon manager
		
		_log.info("AutoChatHandler: Loaded " + AutoChatHandler.getInstance().size() + " handlers in total.");
		_log.info("AutoSpawnHandler: Loaded " + AutoSpawnHandler.getInstance().size() + " handlers in total.");
		
		AdminCommandHandler.getInstance();
		ChatHandler.getInstance();
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		UserCommandHandler.getInstance();
		VoicedCommandHandler.getInstance();
		
		if (Config.L2JMOD_ALLOW_WEDDING)
			CoupleManager.getInstance();
		
		TaskManager.getInstance();
		
		MerchantPriceConfigTable.getInstance().updateReferences();
		CastleManager.getInstance().activateInstances();
		FortManager.getInstance().activateInstances();
		HellboundManager.getInstance();

		//Universe.getInstance();
		
		if (Config.ACCEPT_GEOEDITOR_CONN)
			GeoEditorListener.getInstance();
		
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		
		// initialize the dynamic extension loader
		try
		{
			DynamicExtension.getInstance();
		}
		catch (Exception ex)
		{
			_log.log(Level.WARNING, "DynamicExtension could not be loaded and initialized", ex);
		}
		
		TvTManager.getInstance();
		KnownListUpdateTaskManager.getInstance();
		
		if ((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
			OfflineTradersTable.restoreOfflineTraders(); 
		
		if (Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
			_deadDetectThread = null;
		System.gc();
		// maxMemory is the upper limit the jvm can use, totalMemory the size of
		// the current allocation pool, freeMemory the unused memory in the
		// allocation pool
		long freeMem = (Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory()) / 1048576;
		long totalMem = Runtime.getRuntime().maxMemory() / 1048576;
		_log.info("GameServer Started, free memory " + freeMem + " Mb of " + totalMem + " Mb");
		
		_loginThread = LoginServerThread.getInstance();
		_loginThread.start();
		
		CommunityServerThread.initialize();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final L2GamePacketHandler gph = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<L2GameClient>(sc, gph, gph, gph, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (UnknownHostException e1)
			{
				_log.log(Level.SEVERE, "WARNING: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (IOException e)
		{
			_log.log(Level.SEVERE, "FATAL: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.info("Server Loaded in " + ((serverLoadEnd - serverLoadStart) / 1000) + " seconds");
		
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
		InputStream is = new FileInputStream(new File(LOG_NAME));
		LogManager.getLogManager().readConfiguration(is);
		is.close();
		
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
			_log.info("Telnet server is currently disabled.");
		}
	}
	
	public static void printSection(String s)
	{
		s = "=[ " + s + " ]";
		while (s.length() < 78)
			s = "-" + s;
		_log.info(s);
	}
}

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
package ai.engines;

import java.util.Map;

import quests.Q00692_HowtoOpposeEvil.Q00692_HowtoOpposeEvil;

import javolution.util.FastMap;
import ct23.xtreme.Config;
import ct23.xtreme.gameserver.ThreadPoolManager;
import ct23.xtreme.gameserver.datatables.DoorTable;
import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager.GraciaSeeds;
import ct23.xtreme.gameserver.instancemanager.ZoneManager;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.Location;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.interfaces.IL2Seed;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.zone.L2ZoneType;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct23.xtreme.gameserver.util.Util;

/**
 * Energy Seeds AI.
 *  reworked and update by @Browser
 *
 * original author: Gigiikun
 */
public class EnergySeeds extends L2AttackableAIScript implements IL2Seed
{
	// Common Variables
	private static final int HOWTOOPPOSEEVIL_CHANCE = 60;
	private static final int RATE = 1;
	private static final int RESPAWN = 480000;
	private static final int RANDOM_RESPAWN_OFFSET = 180000;
	private static Map<Integer, ESSpawn> _spawns = new FastMap<>();
	protected static Map<L2Npc, Integer> _spawnedNpcs = new FastMap<L2Npc, Integer>().shared();
	
	// Teleporter
	private static final int TEMPORARY_TELEPORTER = 32602;
	
	// Seeds
	private static final int[] SEED_IDS =
	{
		18678, 18679, 18680, 18681, 18682, 18683
	};
	
	// Seed of Destruction Doors
	private static int[] SEED_OF_DESTRUCTION_DOORS =
	{
		12240003, 12240004, 12240005, 12240006, 12240007, 12240008, 12240009,
		12240010, 12240011, 12240012, 12240013, 12240014, 12240015, 12240016,
		12240017, 12240018, 12240019, 12240020, 12240021, 12240022, 12240023,
		12240024, 12240025, 12240026, 12240027, 12240028, 12240029, 12240030,
		12240031
	};
	
	// Seed Of Infinity Door
	private static int[] SEED_OF_INFINITY_DOOR = { 14240102 };
	
	// Seed Exit Points
	private static final int[] SOD_EXIT_POINT = { -248717, 250260, 4337 };
	private static final int[] SOI_EXIT_POINT = { -183285, 205996, -12896 };
	
	// Seed Zones
	private static final int SOD_ZONE = 60009;
	private static final int SOI_ZONE = 60010;

	private EnergySeeds()
	{
		super(-1, EnergySeeds.class.getSimpleName(), "engines");
		registerMobs(SEED_IDS);
		addFirstTalkId(SEED_IDS);
		addFirstTalkId(TEMPORARY_TELEPORTER);
		addEnterZoneId(SOD_ZONE);
		addEnterZoneId(SOI_ZONE);
		addSpawnsToList();
		startAI();
	}
	
	protected boolean isSeedActive(GraciaSeeds seed)
	{
		switch(seed)
		{
			case INFINITY:
				return GraciaSeedsManager.getInstance().getSoIState() == 3;
			case DESTRUCTION:
				return GraciaSeedsManager.getInstance().getSoDState() == 2;
		}
		return true;
	}
	
	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (!Util.contains(targets, npc) || skill.getId() != 5780)
			return super.onSkillSee(npc, caster, skill, targets, isPet);
		
		npc.deleteMe();
		
		if (_spawnedNpcs.containsKey(npc) && _spawns.containsKey(_spawnedNpcs.get(npc)))
		{
			ESSpawn spawn = _spawns.get(_spawnedNpcs.get(npc));
			spawn.scheduleRespawn(RESPAWN + getRandom(RANDOM_RESPAWN_OFFSET));
			_spawnedNpcs.remove(npc);
			if (isSeedActive(spawn._seedId))
			{
				int itemId = 0;
				
				switch(npc.getNpcId())
				{
					case 18678: //Water
						itemId = 14016;
						break;
					case 18679: //Fire
						itemId = 14015;
						break;
					case 18680: //Wind
						itemId = 14017;
						break;
					case 18681: //Earth
						itemId = 14018;
						break;
					case 18682: //Divinity
						itemId = 14020;
						break;
					case 18683: //Darkness
						itemId = 14019;
						break;
					default:
						return super.onSkillSee(npc, caster, skill, targets, isPet);
				}
				if (getRandom(100) < 33)
				{
					caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
					caster.addItem("EnergySeed", itemId, getRandom(RATE + 1, 2 * RATE), null, true);
				}
				else
				{
					caster.sendPacket(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED);
					caster.addItem("EnergySeed", itemId, getRandom(1, RATE), null, true);
				}
				seedCollectEvent(caster, npc, spawn._seedId);
			}
		}
		
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("StartSoDAi"))
		{
			for (int doorId : SEED_OF_DESTRUCTION_DOORS)
			{
				L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.openMe();
				}
			}
			startAI(GraciaSeeds.DESTRUCTION);
		}
		else if (event.equalsIgnoreCase("StopSoDAi"))
		{
			for (int doorId : SEED_OF_DESTRUCTION_DOORS)
			{
				L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.closeMe();
				}
			}
			for (L2Character ch : ZoneManager.getInstance().getZoneById(SOD_ZONE).getCharactersInside().values())
			{
				if (ch != null)
				{
					ch.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
				}
			}
			stopAI(GraciaSeeds.DESTRUCTION);
		}
		else if (event.equalsIgnoreCase("StartSoIAi"))
		{
			for (int doorId : SEED_OF_INFINITY_DOOR)
			{
				L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.openMe();
				}
			}
			startAI(GraciaSeeds.INFINITY);
		}
		else if (event.equalsIgnoreCase("StopSoIAi"))
		{
			for (int doorId : SEED_OF_INFINITY_DOOR)
			{
				L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
				if (doorInstance != null)
				{
					doorInstance.closeMe();
				}
			}
			for (L2Character ch : ZoneManager.getInstance().getZoneById(SOI_ZONE).getCharactersInside().values())
			{
				if (ch != null)
				{
					ch.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
				}
			}
			stopAI(GraciaSeeds.INFINITY);
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == TEMPORARY_TELEPORTER)
		{
			player.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (_spawnedNpcs.containsKey(npc) && _spawns.containsKey(_spawnedNpcs.get(npc)))
		{
			_spawns.get(_spawnedNpcs.get(npc)).scheduleRespawn(RESPAWN + getRandom(RANDOM_RESPAWN_OFFSET));
			_spawnedNpcs.remove(npc);
		}
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.getInstanceId() != 0)
		{
			return super.onEnterZone(character, zone);
		}
		
		if (character instanceof L2PcInstance)
		{
			switch (zone.getId())
			{
				case SOD_ZONE:
					if (!isSeedActive(GraciaSeeds.DESTRUCTION) && !character.isGM())
					{
						character.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
					}
				case SOI_ZONE:
					if (!isSeedActive(GraciaSeeds.INFINITY) && !character.isGM())
					{
						character.teleToLocation(SOI_EXIT_POINT[0], SOI_EXIT_POINT[1], SOI_EXIT_POINT[2]);
					}
					break;
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	public void startAI()
	{
		// spawn all NPCs
		for (ESSpawn spawn : _spawns.values())
		{
			if (isSeedActive(spawn._seedId))
			{
				spawn.scheduleRespawn(0);
			}
		}
	}
	
	public void startAI(GraciaSeeds type)
	{
		// spawn all NPCs
		for (ESSpawn spawn : _spawns.values())
		{
			if (spawn._seedId == type)
			{
				spawn.scheduleRespawn(0);
			}
		}
	}
	
	public void stopAI(GraciaSeeds type)
	{
		for (L2Npc seed : _spawnedNpcs.keySet())
		{
			if (type == _spawns.get(_spawnedNpcs.get(seed))._seedId)
			{
				seed.deleteMe();
			}
		}
	}
	
	public void seedCollectEvent(L2PcInstance player, L2Npc seedEnergy, GraciaSeeds seedType)
	{
		if (player == null)
		{
			return;
		}
		QuestState st = player.getQuestState(Q00692_HowtoOpposeEvil.class.getSimpleName());
		switch (seedType)
		{
			case INFINITY:
				if ((st != null) && st.isCond(3))
				{
					handleQuestDrop(st, 13798);
				}
				break;
			case DESTRUCTION:
				if ((st != null) && st.isCond(3))
				{
					handleQuestDrop(st, 13867);
				}
				break;
		}
	}
	
	private void handleQuestDrop(QuestState st, int itemId)
	{
		double chance = HOWTOOPPOSEEVIL_CHANCE * Config.RATE_QUEST_DROP;
		int numItems = (int) (chance / 100);
		chance = chance % 100;
		if (getRandom(100) < chance)
		{
			numItems++;
		}
		if (numItems > 0)
		{
			st.giveItems(itemId, numItems);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}
	
	private void addSpawnsToList()
	{
		// Seed of Destruction
		// Temporary Teleporters
		//@formatter:off
		_spawns.put(1, new ESSpawn(1, GraciaSeeds.DESTRUCTION, new Location(-245790,220320,-12104), new int[]{TEMPORARY_TELEPORTER}));
		_spawns.put(2, new ESSpawn(2, GraciaSeeds.DESTRUCTION, new Location(-249770,207300,-11952), new int[]{TEMPORARY_TELEPORTER}));
		
		//Energy Seeds
		_spawns.put(3, new ESSpawn(3, GraciaSeeds.DESTRUCTION, new Location(-248360,219272,-12448), new int[]{18678,18679,18680}));
		_spawns.put(4, new ESSpawn(4, GraciaSeeds.DESTRUCTION, new Location(-249448,219256,-12448), new int[]{18678,18679,18680}));
		_spawns.put(5, new ESSpawn(5, GraciaSeeds.DESTRUCTION, new Location(-249432,220872,-12448), new int[]{18678,18679,18680}));
		_spawns.put(6, new ESSpawn(6, GraciaSeeds.DESTRUCTION, new Location(-248360,220888,-12448), new int[]{18678,18679,18680}));
		
		_spawns.put(7, new ESSpawn(7, GraciaSeeds.DESTRUCTION, new Location(-250088,219256,-12448), new int[]{18681,18682}));
		_spawns.put(8, new ESSpawn(8, GraciaSeeds.DESTRUCTION, new Location(-250600,219272,-12448), new int[]{18681,18682}));
		_spawns.put(9, new ESSpawn(9, GraciaSeeds.DESTRUCTION, new Location(-250584,220904,-12448), new int[]{18681,18682}));
		_spawns.put(10, new ESSpawn(10, GraciaSeeds.DESTRUCTION, new Location(-250072,220888,-12448), new int[]{18681,18682}));
		
		_spawns.put(11, new ESSpawn(11, GraciaSeeds.DESTRUCTION, new Location(-253096,217704,-12296), new int[]{18683,18678}));
		_spawns.put(12, new ESSpawn(12, GraciaSeeds.DESTRUCTION, new Location(-253112,217048,-12288), new int[]{18683,18678}));
		_spawns.put(13, new ESSpawn(13, GraciaSeeds.DESTRUCTION, new Location(-251448,217032,-12288), new int[]{18683,18678}));
		_spawns.put(14, new ESSpawn(14, GraciaSeeds.DESTRUCTION, new Location(-251416,217672,-12296), new int[]{18683,18678}));
		
		_spawns.put(15, new ESSpawn(15, GraciaSeeds.DESTRUCTION, new Location(-251416,217672,-12296), new int[]{18679,18680}));
		_spawns.put(16, new ESSpawn(16, GraciaSeeds.DESTRUCTION, new Location(-251416,217016,-12280), new int[]{18679,18680}));
		_spawns.put(17, new ESSpawn(17, GraciaSeeds.DESTRUCTION, new Location(-249752,217016,-12280), new int[]{18679,18680}));
		_spawns.put(18, new ESSpawn(18, GraciaSeeds.DESTRUCTION, new Location(-249736,217688,-12296), new int[]{18679,18680}));
		
		_spawns.put(19, new ESSpawn(19, GraciaSeeds.DESTRUCTION, new Location(-252472,215208,-12120), new int[]{18681,18682}));
		_spawns.put(20, new ESSpawn(20, GraciaSeeds.DESTRUCTION, new Location(-252552,216760,-12248), new int[]{18681,18682}));
		_spawns.put(21, new ESSpawn(21, GraciaSeeds.DESTRUCTION, new Location(-253160,216744,-12248), new int[]{18681,18682}));
		_spawns.put(22, new ESSpawn(22, GraciaSeeds.DESTRUCTION, new Location(-253128,215160,-12096), new int[]{18681,18682}));
		
		_spawns.put(23, new ESSpawn(23, GraciaSeeds.DESTRUCTION, new Location(-250392,215208,-12120), new int[]{18683,18678}));
		_spawns.put(24, new ESSpawn(24, GraciaSeeds.DESTRUCTION, new Location(-250264,216744,-12248), new int[]{18683,18678}));
		_spawns.put(25, new ESSpawn(25, GraciaSeeds.DESTRUCTION, new Location(-249720,216744,-12248), new int[]{18683,18678}));
		_spawns.put(26, new ESSpawn(26, GraciaSeeds.DESTRUCTION, new Location(-249752,215128,-12096), new int[]{18683,18678}));
		
		_spawns.put(27, new ESSpawn(27, GraciaSeeds.DESTRUCTION, new Location(-250280,216760,-12248), new int[]{18679,18680,18681}));
		_spawns.put(28, new ESSpawn(28, GraciaSeeds.DESTRUCTION, new Location(-250344,216152,-12248), new int[]{18679,18680,18681}));
		_spawns.put(29, new ESSpawn(29, GraciaSeeds.DESTRUCTION, new Location(-252504,216152,-12248), new int[]{18679,18680,18681}));
		_spawns.put(30, new ESSpawn(30, GraciaSeeds.DESTRUCTION, new Location(-252520,216792,-12248), new int[]{18679,18680,18681}));
		
		_spawns.put(31, new ESSpawn(31, GraciaSeeds.DESTRUCTION, new Location(-242520,217272,-12384), new int[]{18681,18682,18683}));
		_spawns.put(32, new ESSpawn(32, GraciaSeeds.DESTRUCTION, new Location(-241432,217288,-12384), new int[]{18681,18682,18683}));
		_spawns.put(33, new ESSpawn(33, GraciaSeeds.DESTRUCTION, new Location(-241432,218936,-12384), new int[]{18681,18682,18683}));
		_spawns.put(34, new ESSpawn(34, GraciaSeeds.DESTRUCTION, new Location(-242536,218936,-12384), new int[]{18681,18682,18683}));
		
		_spawns.put(35, new ESSpawn(35, GraciaSeeds.DESTRUCTION, new Location(-240808,217272,-12384), new int[]{18678,18679}));
		_spawns.put(36, new ESSpawn(36, GraciaSeeds.DESTRUCTION, new Location(-240280,217272,-12384), new int[]{18678,18679}));
		_spawns.put(37, new ESSpawn(37, GraciaSeeds.DESTRUCTION, new Location(-240280,218952,-12384), new int[]{18678,18679}));
		_spawns.put(38, new ESSpawn(38, GraciaSeeds.DESTRUCTION, new Location(-240792,218936,-12384), new int[]{18678,18679}));
		
		_spawns.put(39, new ESSpawn(39, GraciaSeeds.DESTRUCTION, new Location(-239576,217240,-12640), new int[]{18680,18681,18682}));
		_spawns.put(40, new ESSpawn(40, GraciaSeeds.DESTRUCTION, new Location(-239560,216168,-12640), new int[]{18680,18681,18682}));
		_spawns.put(41, new ESSpawn(41, GraciaSeeds.DESTRUCTION, new Location(-237896,216152,-12640), new int[]{18680,18681,18682}));
		_spawns.put(42, new ESSpawn(42, GraciaSeeds.DESTRUCTION, new Location(-237912,217256,-12640), new int[]{18680,18681,18682}));
		
		_spawns.put(43, new ESSpawn(43, GraciaSeeds.DESTRUCTION, new Location(-237896,215528,-12640), new int[]{18683,18678}));
		_spawns.put(44, new ESSpawn(44, GraciaSeeds.DESTRUCTION, new Location(-239560,215528,-12640), new int[]{18683,18678}));
		_spawns.put(45, new ESSpawn(45, GraciaSeeds.DESTRUCTION, new Location(-239560,214984,-12640), new int[]{18683,18678}));
		_spawns.put(46, new ESSpawn(46, GraciaSeeds.DESTRUCTION, new Location(-237896,215000,-12640), new int[]{18683,18678}));
		
		_spawns.put(47, new ESSpawn(47, GraciaSeeds.DESTRUCTION, new Location(-237896,213640,-12768), new int[]{18678,18679,18680}));
		_spawns.put(48, new ESSpawn(48, GraciaSeeds.DESTRUCTION, new Location(-239560,213640,-12768), new int[]{18678,18679,18680}));
		_spawns.put(49, new ESSpawn(49, GraciaSeeds.DESTRUCTION, new Location(-239544,212552,-12768), new int[]{18678,18679,18680}));
		_spawns.put(50, new ESSpawn(50, GraciaSeeds.DESTRUCTION, new Location(-237912,212552,-12768), new int[]{18678,18679,18680}));
		
		_spawns.put(51, new ESSpawn(51, GraciaSeeds.DESTRUCTION, new Location(-237912,211912,-12768), new int[]{18681,18682}));
		_spawns.put(52, new ESSpawn(52, GraciaSeeds.DESTRUCTION, new Location(-237912,211400,-12768), new int[]{18681,18682}));
		_spawns.put(53, new ESSpawn(53, GraciaSeeds.DESTRUCTION, new Location(-239560,211400,-12768), new int[]{18681,18682}));
		_spawns.put(54, new ESSpawn(54, GraciaSeeds.DESTRUCTION, new Location(-239560,211912,-12768), new int[]{18681,18682}));
		
		_spawns.put(55, new ESSpawn(55, GraciaSeeds.DESTRUCTION, new Location(-241960,214536,-12512), new int[]{18683,18678,18679}));
		_spawns.put(56, new ESSpawn(56, GraciaSeeds.DESTRUCTION, new Location(-241976,213448,-12512), new int[]{18683,18678,18679}));
		_spawns.put(57, new ESSpawn(57, GraciaSeeds.DESTRUCTION, new Location(-243624,213448,-12512), new int[]{18683,18678,18679}));
		_spawns.put(58, new ESSpawn(58, GraciaSeeds.DESTRUCTION, new Location(-243624,214520,-12512), new int[]{18683,18678,18679}));
		
		_spawns.put(59, new ESSpawn(59, GraciaSeeds.DESTRUCTION, new Location(-241976,212808,-12504), new int[]{18680,18681}));
		_spawns.put(60, new ESSpawn(60, GraciaSeeds.DESTRUCTION, new Location(-241960,212280,-12504), new int[]{18680,18681}));
		_spawns.put(61, new ESSpawn(61, GraciaSeeds.DESTRUCTION, new Location(-243624,212264,-12504), new int[]{18680,18681}));
		_spawns.put(62, new ESSpawn(62, GraciaSeeds.DESTRUCTION, new Location(-243624,212792,-12504), new int[]{18680,18681}));
		
		_spawns.put(63, new ESSpawn(63, GraciaSeeds.DESTRUCTION, new Location(-243640,210920,-12640), new int[]{18682,18683,18678}));
		_spawns.put(64, new ESSpawn(64, GraciaSeeds.DESTRUCTION, new Location(-243624,209832,-12640), new int[]{18682,18683,18678}));
		_spawns.put(65, new ESSpawn(65, GraciaSeeds.DESTRUCTION, new Location(-241976,209832,-12640), new int[]{18682,18683,18678}));
		_spawns.put(66, new ESSpawn(66, GraciaSeeds.DESTRUCTION, new Location(-241976,210920,-12640), new int[]{18682,18683,18678}));
		
		_spawns.put(67, new ESSpawn(67, GraciaSeeds.DESTRUCTION, new Location(-241976,209192,-12640), new int[]{18679,18680}));
		_spawns.put(68, new ESSpawn(68, GraciaSeeds.DESTRUCTION, new Location(-241976,208664,-12640), new int[]{18679,18680}));
		_spawns.put(69, new ESSpawn(69, GraciaSeeds.DESTRUCTION, new Location(-243624,208664,-12640), new int[]{18679,18680}));
		_spawns.put(70, new ESSpawn(70, GraciaSeeds.DESTRUCTION, new Location(-243624,209192,-12640), new int[]{18679,18680}));
		
		_spawns.put(71, new ESSpawn(71, GraciaSeeds.DESTRUCTION, new Location(-241256,208664,-12896), new int[]{18681,18682,18683}));
		_spawns.put(72, new ESSpawn(72, GraciaSeeds.DESTRUCTION, new Location(-240168,208648,-12896), new int[]{18681,18682,18683}));
		_spawns.put(73, new ESSpawn(73, GraciaSeeds.DESTRUCTION, new Location(-240168,207000,-12896), new int[]{18681,18682,18683}));
		_spawns.put(74, new ESSpawn(74, GraciaSeeds.DESTRUCTION, new Location(-241256,207000,-12896), new int[]{18681,18682,18683}));
		
		_spawns.put(75, new ESSpawn(75, GraciaSeeds.DESTRUCTION, new Location(-239528,208648,-12896), new int[]{18678,18679}));
		_spawns.put(76, new ESSpawn(76, GraciaSeeds.DESTRUCTION, new Location(-238984,208664,-12896), new int[]{18678,18679}));
		_spawns.put(77, new ESSpawn(77, GraciaSeeds.DESTRUCTION, new Location(-239000,207000,-12896), new int[]{18678,18679}));
		_spawns.put(78, new ESSpawn(78, GraciaSeeds.DESTRUCTION, new Location(-239512,207000,-12896), new int[]{18678,18679}));
		
		_spawns.put(79, new ESSpawn(79, GraciaSeeds.DESTRUCTION, new Location(-245064,213144,-12384), new int[]{18680,18681,18682}));
		_spawns.put(80, new ESSpawn(80, GraciaSeeds.DESTRUCTION, new Location(-245064,212072,-12384), new int[]{18680,18681,18682}));
		_spawns.put(81, new ESSpawn(81, GraciaSeeds.DESTRUCTION, new Location(-246696,212072,-12384), new int[]{18680,18681,18682}));
		_spawns.put(82, new ESSpawn(82, GraciaSeeds.DESTRUCTION, new Location(-246696,213160,-12384), new int[]{18680,18681,18682}));
		
		_spawns.put(83, new ESSpawn(83, GraciaSeeds.DESTRUCTION, new Location(-245064,211416,-12384), new int[]{18683,18678}));
		_spawns.put(84, new ESSpawn(84, GraciaSeeds.DESTRUCTION, new Location(-245048,210904,-12384), new int[]{18683,18678}));
		_spawns.put(85, new ESSpawn(85, GraciaSeeds.DESTRUCTION, new Location(-246712,210888,-12384), new int[]{18683,18678}));
		_spawns.put(86, new ESSpawn(86, GraciaSeeds.DESTRUCTION, new Location(-246712,211416,-12384), new int[]{18683,18678}));
		
		_spawns.put(87, new ESSpawn(87, GraciaSeeds.DESTRUCTION, new Location(-245048,209544,-12512), new int[]{18679,18680,18681}));
		_spawns.put(88, new ESSpawn(88, GraciaSeeds.DESTRUCTION, new Location(-245064,208456,-12512), new int[]{18679,18680,18681}));
		_spawns.put(89, new ESSpawn(89, GraciaSeeds.DESTRUCTION, new Location(-246696,208456,-12512), new int[]{18679,18680,18681}));
		_spawns.put(90, new ESSpawn(90, GraciaSeeds.DESTRUCTION, new Location(-246712,209544,-12512), new int[]{18679,18680,18681}));
		
		_spawns.put(91, new ESSpawn(91, GraciaSeeds.DESTRUCTION, new Location(-245048,207816,-12512), new int[]{18682,18683}));
		_spawns.put(92, new ESSpawn(92, GraciaSeeds.DESTRUCTION, new Location(-245048,207288,-12512), new int[]{18682,18683}));
		_spawns.put(93, new ESSpawn(93, GraciaSeeds.DESTRUCTION, new Location(-246696,207304,-12512), new int[]{18682,18683}));
		_spawns.put(94, new ESSpawn(94, GraciaSeeds.DESTRUCTION, new Location(-246712,207816,-12512), new int[]{18682,18683}));
		
		_spawns.put(95, new ESSpawn(95, GraciaSeeds.DESTRUCTION, new Location(-244328,207272,-12768), new int[]{18678,18679,18680}));
		_spawns.put(96, new ESSpawn(96, GraciaSeeds.DESTRUCTION, new Location(-243256,207256,-12768), new int[]{18678,18679,18680}));
		_spawns.put(97, new ESSpawn(97, GraciaSeeds.DESTRUCTION, new Location(-243256,205624,-12768), new int[]{18678,18679,18680}));
		_spawns.put(98, new ESSpawn(98, GraciaSeeds.DESTRUCTION, new Location(-244328,205608,-12768), new int[]{18678,18679,18680}));
		
		_spawns.put(99, new ESSpawn(99, GraciaSeeds.DESTRUCTION, new Location(-242616,207272,-12768), new int[]{18681,18682}));
		_spawns.put(100, new ESSpawn(100, GraciaSeeds.DESTRUCTION, new Location(-242104,207272,-12768), new int[]{18681,18682}));
		_spawns.put(101, new ESSpawn(101, GraciaSeeds.DESTRUCTION, new Location(-242088,205624,-12768), new int[]{18681,18682}));
		_spawns.put(102, new ESSpawn(102, GraciaSeeds.DESTRUCTION, new Location(-242600,205608,-12768), new int[]{18681,18682}));
		
		// Seed Of Infinity
		_spawns.put(302, new ESSpawn(302, GraciaSeeds.INFINITY, new Location(-187638,205392,-9529), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(303, new ESSpawn(303, GraciaSeeds.INFINITY, new Location(-188101,205699,-9537), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(304, new ESSpawn(304, GraciaSeeds.INFINITY, new Location(-187772,206175,-9526), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(305, new ESSpawn(305, GraciaSeeds.INFINITY, new Location(-187476,205709,-9559), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(306, new ESSpawn(306, GraciaSeeds.INFINITY, new Location(-186050,207995,-9531), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(307, new ESSpawn(307, GraciaSeeds.INFINITY, new Location(-186044,208588,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(308, new ESSpawn(308, GraciaSeeds.INFINITY, new Location(-186591,208589,-9532), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(309, new ESSpawn(309, GraciaSeeds.INFINITY, new Location(-186599,207999,-9536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(310, new ESSpawn(310, GraciaSeeds.INFINITY, new Location(-184759,210865,-9535), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(311, new ESSpawn(311, GraciaSeeds.INFINITY, new Location(-184211,210865,-9534), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(312, new ESSpawn(312, GraciaSeeds.INFINITY, new Location(-184756,211451,-9534), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(313, new ESSpawn(313, GraciaSeeds.INFINITY, new Location(-184220,211499,-9532), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(314, new ESSpawn(314, GraciaSeeds.INFINITY, new Location(-182534,213608,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(315, new ESSpawn(315, GraciaSeeds.INFINITY, new Location(-182518,214193,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(316, new ESSpawn(316, GraciaSeeds.INFINITY, new Location(-183063,214198,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(317, new ESSpawn(317, GraciaSeeds.INFINITY, new Location(-183070,213607,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(318, new ESSpawn(328, GraciaSeeds.INFINITY, new Location(-181200,216482,-9538), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(319, new ESSpawn(319, GraciaSeeds.INFINITY, new Location(-180655,216483,-9536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(320, new ESSpawn(320, GraciaSeeds.INFINITY, new Location(-180653,217068,-9532), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(321, new ESSpawn(321, GraciaSeeds.INFINITY, new Location(-181203,217070,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(322, new ESSpawn(322, GraciaSeeds.INFINITY, new Location(-177531,217535,-9533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(323, new ESSpawn(323, GraciaSeeds.INFINITY, new Location(-176860,217587,-9529), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(324, new ESSpawn(324, GraciaSeeds.INFINITY, new Location(-176926,218172,-9537), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(325, new ESSpawn(325, GraciaSeeds.INFINITY, new Location(-177509,218147,-9529), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(326, new ESSpawn(326, GraciaSeeds.INFINITY, new Location(-173094,218073,-9556), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(327, new ESSpawn(327, GraciaSeeds.INFINITY, new Location(-173833,218766,-9555), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(328, new ESSpawn(328, GraciaSeeds.INFINITY, new Location(-174296,217729,-9554), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(329, new ESSpawn(329, GraciaSeeds.INFINITY, new Location(-173316,217530,-9555), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(330, new ESSpawn(330, GraciaSeeds.INFINITY, new Location(-179381,211049,-12786), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(331, new ESSpawn(331, GraciaSeeds.INFINITY, new Location(-179680,211343,-12781), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(332, new ESSpawn(332, GraciaSeeds.INFINITY, new Location(-179935,211032,-12793), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(333, new ESSpawn(333, GraciaSeeds.INFINITY, new Location(-179663,210831,-12789), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(334, new ESSpawn(334, GraciaSeeds.INFINITY, new Location(-180336,211106,-12015), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(335, new ESSpawn(335, GraciaSeeds.INFINITY, new Location(-180340,212228,-12018), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(336, new ESSpawn(336, GraciaSeeds.INFINITY, new Location(-181535,212193,-12036), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(337, new ESSpawn(337, GraciaSeeds.INFINITY, new Location(-181507,211062,-12022), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(338, new ESSpawn(338, GraciaSeeds.INFINITY, new Location(-182732,210369,-11931), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(339, new ESSpawn(339, GraciaSeeds.INFINITY, new Location(-182597,210577,-11944), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(340, new ESSpawn(340, GraciaSeeds.INFINITY, new Location(-183806,209503,-11926), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(341, new ESSpawn(341, GraciaSeeds.INFINITY, new Location(-182987,209357,-11924), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(342, new ESSpawn(342, GraciaSeeds.INFINITY, new Location(-183026,208807,-11924), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(343, new ESSpawn(343, GraciaSeeds.INFINITY, new Location(-183814,208634,-11926), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(344, new ESSpawn(344, GraciaSeeds.INFINITY, new Location(-183358,207535,-11926), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(345, new ESSpawn(345, GraciaSeeds.INFINITY, new Location(-182666,207913,-11926), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(346, new ESSpawn(346, GraciaSeeds.INFINITY, new Location(-181471,207142,-12026), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(347, new ESSpawn(347, GraciaSeeds.INFINITY, new Location(-181565,206024,-12034), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(348, new ESSpawn(348, GraciaSeeds.INFINITY, new Location(-180190,206064,-12016), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(349, new ESSpawn(349, GraciaSeeds.INFINITY, new Location(-180409,207224,-12016), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(350, new ESSpawn(350, GraciaSeeds.INFINITY, new Location(-178914,207242,-12017), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(351, new ESSpawn(351, GraciaSeeds.INFINITY, new Location(-179088,206126,-12018), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(352, new ESSpawn(352, GraciaSeeds.INFINITY, new Location(-177889,205872,-12040), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(353, new ESSpawn(353, GraciaSeeds.INFINITY, new Location(-177954,207216,-12020), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(354, new ESSpawn(354, GraciaSeeds.INFINITY, new Location(-176648,207886,-11927), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(355, new ESSpawn(355, GraciaSeeds.INFINITY, new Location(-175711,207706,-11948), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(356, new ESSpawn(356, GraciaSeeds.INFINITY, new Location(-175508,208532,-11927), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(357, new ESSpawn(357, GraciaSeeds.INFINITY, new Location(-176270,208731,-11924), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(358, new ESSpawn(358, GraciaSeeds.INFINITY, new Location(-176335,209415,-11924), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(359, new ESSpawn(359, GraciaSeeds.INFINITY, new Location(-175503,209561,-11925), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(360, new ESSpawn(360, GraciaSeeds.INFINITY, new Location(-175739,210510,-11944), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(361, new ESSpawn(361, GraciaSeeds.INFINITY, new Location(-176550,210282,-11933), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(362, new ESSpawn(362, GraciaSeeds.INFINITY, new Location(-177945,211022,-12024), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(363, new ESSpawn(363, GraciaSeeds.INFINITY, new Location(-177736,212120,-12031), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(364, new ESSpawn(364, GraciaSeeds.INFINITY, new Location(-178846,212402,-12018), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(365, new ESSpawn(365, GraciaSeeds.INFINITY, new Location(-178867,211039,-12015), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(366, new ESSpawn(366, GraciaSeeds.INFINITY, new Location(-180053,206969,-15506), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(367, new ESSpawn(367, GraciaSeeds.INFINITY, new Location(-180294,206535,-15507), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(368, new ESSpawn(368, GraciaSeeds.INFINITY, new Location(-180112,205799,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(369, new ESSpawn(369, GraciaSeeds.INFINITY, new Location(-179111,205646,-15513), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(370, new ESSpawn(370, GraciaSeeds.INFINITY, new Location(-178725,206281,-15510), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(371, new ESSpawn(371, GraciaSeeds.INFINITY, new Location(-178964,206894,-15509), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(372, new ESSpawn(372, GraciaSeeds.INFINITY, new Location(-177503,207795,-15505), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(373, new ESSpawn(373, GraciaSeeds.INFINITY, new Location(-176984,207217,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(374, new ESSpawn(374, GraciaSeeds.INFINITY, new Location(-176416,207201,-15501), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(375, new ESSpawn(375, GraciaSeeds.INFINITY, new Location(-175937,207745,-15496), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(376, new ESSpawn(376, GraciaSeeds.INFINITY, new Location(-176109,208489,-15505), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(377, new ESSpawn(377, GraciaSeeds.INFINITY, new Location(-176851,208739,-15513), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(378, new ESSpawn(378, GraciaSeeds.INFINITY, new Location(-177001,210471,-15505), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(379, new ESSpawn(379, GraciaSeeds.INFINITY, new Location(-176442,210489,-15506), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(380, new ESSpawn(380, GraciaSeeds.INFINITY, new Location(-175932,211228,-15508), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(381, new ESSpawn(381, GraciaSeeds.INFINITY, new Location(-176404,211988,-15508), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(382, new ESSpawn(382, GraciaSeeds.INFINITY, new Location(-177045,212000,-15506), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(383, new ESSpawn(383, GraciaSeeds.INFINITY, new Location(-177512,211467,-15509), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(384, new ESSpawn(384, GraciaSeeds.INFINITY, new Location(-177372,217854,-9536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(385, new ESSpawn(385, GraciaSeeds.INFINITY, new Location(-177237,218140,-9536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(386, new ESSpawn(386, GraciaSeeds.INFINITY, new Location(-177021,217647,-9528), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(387, new ESSpawn(387, GraciaSeeds.INFINITY, new Location(-177372,217792,-9544), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(388, new ESSpawn(388, GraciaSeeds.INFINITY, new Location(-173727,218270,-9536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(389, new ESSpawn(389, GraciaSeeds.INFINITY, new Location(-173727,218049,-9536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(390, new ESSpawn(390, GraciaSeeds.INFINITY, new Location(-178948,212336,-15504), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(391, new ESSpawn(391, GraciaSeeds.INFINITY, new Location(-178783,213093,-15501), new int[]{18678, 18679, 18680, 18681, 18682, 18683})); 
		_spawns.put(392, new ESSpawn(392, GraciaSeeds.INFINITY, new Location(-179283,213641,-15497), new int[]{18678, 18679, 18680, 18681, 18682, 18683})); 
		_spawns.put(393, new ESSpawn(393, GraciaSeeds.INFINITY, new Location(-180081,213455,-15507), new int[]{18678, 18679, 18680, 18681, 18682, 18683})); 
		_spawns.put(394, new ESSpawn(394, GraciaSeeds.INFINITY, new Location(-180391,212881,-15514), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(395, new ESSpawn(395, GraciaSeeds.INFINITY, new Location(-180143,212307,-15509), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(396, new ESSpawn(396, GraciaSeeds.INFINITY, new Location(-181572,211397,-15503), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(397, new ESSpawn(397, GraciaSeeds.INFINITY, new Location(-182013,212002,-15504), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(398, new ESSpawn(398, GraciaSeeds.INFINITY, new Location(-182870,211897,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(399, new ESSpawn(399, GraciaSeeds.INFINITY, new Location(-183238,211211,-15511), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(400, new ESSpawn(400, GraciaSeeds.INFINITY, new Location(-182902,210620,-15506), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(401, new ESSpawn(401, GraciaSeeds.INFINITY, new Location(-182269,210427,-15509), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(402, new ESSpawn(402, GraciaSeeds.INFINITY, new Location(-182019,208741,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(403, new ESSpawn(403, GraciaSeeds.INFINITY, new Location(-182640,208738,-15503), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(404, new ESSpawn(404, GraciaSeeds.INFINITY, new Location(-183145,208180,-15499), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(405, new ESSpawn(405, GraciaSeeds.INFINITY, new Location(-183012,207450,-15495), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(406, new ESSpawn(406, GraciaSeeds.INFINITY, new Location(-182279,207179,-15504), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(407, new ESSpawn(407, GraciaSeeds.INFINITY, new Location(-181645,207654,-15508), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(408, new ESSpawn(408, GraciaSeeds.INFINITY, new Location(-179790,208689,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(409, new ESSpawn(409, GraciaSeeds.INFINITY, new Location(-179285,208714,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(410, new ESSpawn(410, GraciaSeeds.INFINITY, new Location(-178606,208915,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(411, new ESSpawn(411, GraciaSeeds.INFINITY, new Location(-178360,209580,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(412, new ESSpawn(412, GraciaSeeds.INFINITY, new Location(-178524,210196,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(413, new ESSpawn(413, GraciaSeeds.INFINITY, new Location(-178967,210642,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(414, new ESSpawn(414, GraciaSeeds.INFINITY, new Location(-179564,210792,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(415, new ESSpawn(415, GraciaSeeds.INFINITY, new Location(-180130,210598,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(416, new ESSpawn(416, GraciaSeeds.INFINITY, new Location(-180568,210139,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(417, new ESSpawn(417, GraciaSeeds.INFINITY, new Location(-180692,209540,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(418, new ESSpawn(418, GraciaSeeds.INFINITY, new Location(-180518,208941,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(419, new ESSpawn(419, GraciaSeeds.INFINITY, new Location(-179801,208651,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(420, new ESSpawn(420, GraciaSeeds.INFINITY, new Location(-179538,209983,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(421, new ESSpawn(421, GraciaSeeds.INFINITY, new Location(-179535,209146,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(422, new ESSpawn(422, GraciaSeeds.INFINITY, new Location(-179964,209581,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(423, new ESSpawn(423, GraciaSeeds.INFINITY, new Location(-179099,209573,-15502), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(424, new ESSpawn(424, GraciaSeeds.INFINITY, new Location(-175979,208710,-11921), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(425, new ESSpawn(425, GraciaSeeds.INFINITY, new Location(-181210,208628,-12462), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(426, new ESSpawn(426, GraciaSeeds.INFINITY, new Location(-175873,207582,-11932), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(427, new ESSpawn(427, GraciaSeeds.INFINITY, new Location(-176237,207577,-11925), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(428, new ESSpawn(428, GraciaSeeds.INFINITY, new Location(-176575,207896,-11931), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(429, new ESSpawn(429, GraciaSeeds.INFINITY, new Location(-176227,207875,-11931), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(430, new ESSpawn(430, GraciaSeeds.INFINITY, new Location(-176325,207877,-11931), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(431, new ESSpawn(431, GraciaSeeds.INFINITY, new Location(-180364,211944,-12019), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(432, new ESSpawn(432, GraciaSeeds.INFINITY, new Location(-181616,211413,-12015), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(433, new ESSpawn(433, GraciaSeeds.INFINITY, new Location(-181404,211042,-12023), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(434, new ESSpawn(434, GraciaSeeds.INFINITY, new Location(-181558,212227,-12035), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(435, new ESSpawn(435, GraciaSeeds.INFINITY, new Location(-180459,212322,-12018), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(436, new ESSpawn(436, GraciaSeeds.INFINITY, new Location(-180428,211180,-12014), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(437, new ESSpawn(437, GraciaSeeds.INFINITY, new Location(-180718,212162,-12028), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(438, new ESSpawn(438, GraciaSeeds.INFINITY, new Location(-183114,209397,-11923), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(439, new ESSpawn(439, GraciaSeeds.INFINITY, new Location(-182917,210495,-11925), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(440, new ESSpawn(440, GraciaSeeds.INFINITY, new Location(-183918,210225,-11934), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(441, new ESSpawn(441, GraciaSeeds.INFINITY, new Location(-183862,209909,-11932), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(442, new ESSpawn(442, GraciaSeeds.INFINITY, new Location(-183246,210631,-11923), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(443, new ESSpawn(443, GraciaSeeds.INFINITY, new Location(-180003,206703,-15520), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(444, new ESSpawn(444, GraciaSeeds.INFINITY, new Location(-180056,216162,-15511), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(445, new ESSpawn(445, GraciaSeeds.INFINITY, new Location(-179586,205657,-15499), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(446, new ESSpawn(446, GraciaSeeds.INFINITY, new Location(-179029,205991,-15518), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(447, new ESSpawn(447, GraciaSeeds.INFINITY, new Location(-179949,206751,-15521), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(448, new ESSpawn(448, GraciaSeeds.INFINITY, new Location(-179949,206505,-15522), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(449, new ESSpawn(449, GraciaSeeds.INFINITY, new Location(-180207,206362,-15512), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(450, new ESSpawn(450, GraciaSeeds.INFINITY, new Location(-180046,206109,-15511), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(451, new ESSpawn(451, GraciaSeeds.INFINITY, new Location(-179843,205686,-15511), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(452, new ESSpawn(452, GraciaSeeds.INFINITY, new Location(-179658,206002,-15518), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(453, new ESSpawn(453, GraciaSeeds.INFINITY, new Location(-179262,205896,-15536), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(454, new ESSpawn(454, GraciaSeeds.INFINITY, new Location(-178907,205950,-15509), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(455, new ESSpawn(455, GraciaSeeds.INFINITY, new Location(-179128,206423,-15528), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(456, new ESSpawn(456, GraciaSeeds.INFINITY, new Location(-179262,206890,-15524), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(457, new ESSpawn(457, GraciaSeeds.INFINITY, new Location(-177238,208020,-15521), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(458, new ESSpawn(458, GraciaSeeds.INFINITY, new Location(-177202,207533,-15516), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(459, new ESSpawn(459, GraciaSeeds.INFINITY, new Location(-176900,207515,-15511), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(460, new ESSpawn(460, GraciaSeeds.INFINITY, new Location(-176847,208022,-15523), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(461, new ESSpawn(461, GraciaSeeds.INFINITY, new Location(-176187,207648,-15499), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(462, new ESSpawn(462, GraciaSeeds.INFINITY, new Location(-176044,208039,-15524), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(463, new ESSpawn(463, GraciaSeeds.INFINITY, new Location(-176346,208233,-15533), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		_spawns.put(464, new ESSpawn(464, GraciaSeeds.INFINITY, new Location(-176440,208594,-15527), new int[]{18678, 18679, 18680, 18681, 18682, 18683}));
		
		// Raid Bosses
		_spawns.put(465, new ESSpawn(465, GraciaSeeds.INFINITY, new Location(-180906,206635,-12032), new int[]{25643, 25644, 25645, 25646, 25647, 25648}));
		_spawns.put(466, new ESSpawn(466, GraciaSeeds.INFINITY, new Location(-178472,211823,-12025), new int[]{25649, 25650, 25651, 25652, 25643, 25644}));
		_spawns.put(467, new ESSpawn(467, GraciaSeeds.INFINITY, new Location(-180926,211887,-12029), new int[]{25645, 25646, 25647, 25648, 25649, 25650}));
		_spawns.put(468, new ESSpawn(468, GraciaSeeds.INFINITY, new Location(-178492,206426,-12023), new int[]{25651, 25652, 25643, 25644, 25645, 25646}));
		_spawns.put(469, new ESSpawn(469, GraciaSeeds.INFINITY, new Location(-176426,211219,-15504), new int[]{25647, 25648, 25649, 25650, 25651, 25652}));
		_spawns.put(470, new ESSpawn(470, GraciaSeeds.INFINITY, new Location(-177040,207870,-15504), new int[]{25643, 25644, 25645, 25646, 25647, 25648}));
		_spawns.put(471, new ESSpawn(471, GraciaSeeds.INFINITY, new Location(-179762,206479,-15504), new int[]{25649, 25650, 25651, 25652, 25643, 25644}));
		_spawns.put(472, new ESSpawn(472, GraciaSeeds.INFINITY, new Location(-182388,207599,-15504), new int[]{25645, 25646, 25647, 25648, 25649, 25650}));
		_spawns.put(473, new ESSpawn(473, GraciaSeeds.INFINITY, new Location(-182733,211096,-15504), new int[]{25651, 25652, 25643, 25644, 25645, 25646}));
		
		// Life Stab Npc
		_spawns.put(474, new ESSpawn(474, GraciaSeeds.INFINITY, new Location(-186327,208286,-9536), new int[]{32542}));
		_spawns.put(475, new ESSpawn(475, GraciaSeeds.INFINITY, new Location(-179659,211061,-12784), new int[]{32542}));
		_spawns.put(476, new ESSpawn(476, GraciaSeeds.INFINITY, new Location(-179538,211313,-15488), new int[]{32547}));
		_spawns.put(477, new ESSpawn(477, GraciaSeeds.INFINITY, new Location(-179779,212540,-15520), new int[]{32542}));
		_spawns.put(478, new ESSpawn(478, GraciaSeeds.INFINITY, new Location(-177028,211135,-15520), new int[]{32542}));
		_spawns.put(479, new ESSpawn(479, GraciaSeeds.INFINITY, new Location(-182069,211140,-15520), new int[]{32542}));
		_spawns.put(480, new ESSpawn(480, GraciaSeeds.INFINITY, new Location(-182268,208218,-15520), new int[]{32542}));
		_spawns.put(481, new ESSpawn(481, GraciaSeeds.INFINITY, new Location(-179284,205990,-15520), new int[]{32542}));
		_spawns.put(482, new ESSpawn(482, GraciaSeeds.INFINITY, new Location(-179465,205648,-15498), new int[]{32542}));
		_spawns.put(483, new ESSpawn(483, GraciaSeeds.INFINITY, new Location(-183290,210004,-11939), new int[]{32542}));
		_spawns.put(484, new ESSpawn(484, GraciaSeeds.INFINITY, new Location(-184429,211155,-9536), new int[]{32542}));
		_spawns.put(485, new ESSpawn(485, GraciaSeeds.INFINITY, new Location(-183288,208205,-11939), new int[]{32542}));
		_spawns.put(486, new ESSpawn(486, GraciaSeeds.INFINITY, new Location(-176039,208203,-11948), new int[]{32542}));
		_spawns.put(487, new ESSpawn(487, GraciaSeeds.INFINITY, new Location(-176036,210002,-11948), new int[]{32542}));
		_spawns.put(488, new ESSpawn(488, GraciaSeeds.INFINITY, new Location(-180911,211652,-12028), new int[]{32542}));
		_spawns.put(489, new ESSpawn(489, GraciaSeeds.INFINITY, new Location(-180911,206551,-12028), new int[]{32542}));
		_spawns.put(490, new ESSpawn(490, GraciaSeeds.INFINITY, new Location(-182811,213871,-9504), new int[]{32542}));
		_spawns.put(491, new ESSpawn(491, GraciaSeeds.INFINITY, new Location(-178417,206558,-12032), new int[]{32542}));
		_spawns.put(492, new ESSpawn(492, GraciaSeeds.INFINITY, new Location(-178418,211653,-12029), new int[]{32542}));
		_spawns.put(493, new ESSpawn(493, GraciaSeeds.INFINITY, new Location(-187776,205696,-9536), new int[]{32542}));
		_spawns.put(494, new ESSpawn(494, GraciaSeeds.INFINITY, new Location(-180921,216789,-9536), new int[]{32542}));
		_spawns.put(495, new ESSpawn(495, GraciaSeeds.INFINITY, new Location(-173727,218169,-9536), new int[]{32542}));
		_spawns.put(496, new ESSpawn(496, GraciaSeeds.INFINITY, new Location(-177264,217760,-9536), new int[]{32542}));
		//@formatter:on
	}
	
	private class ESSpawn
	{
		protected final int _spawnId;
		protected final GraciaSeeds _seedId;
		protected final int[] _npcIds;
		protected final Location _loc;
		
		public ESSpawn(int spawnId, GraciaSeeds seedId, Location loc, int[] npcIds)
		{
			_spawnId = spawnId;
			_seedId = seedId;
			_loc = loc;
			_npcIds = npcIds;
		}
		
		public void scheduleRespawn(long waitTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					// if the AI is inactive, do not spawn the NPC
					if (isSeedActive(_seedId))
					{
						// get a random NPC that should spawn at this location
						Integer spawnId = _spawnId; // the map uses "Integer", not "int"
						_spawnedNpcs.put(addSpawn(_npcIds[getRandom(_npcIds.length)], _loc, false, 0), spawnId);
					}
				}
			}, waitTime);
		}
	}
	
	
	public static void main(String[] args)
	{
		new EnergySeeds();
	}
}
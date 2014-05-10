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

import javolution.util.FastMap;

import ct23.xtreme.Config;
import ct23.xtreme.gameserver.ThreadPoolManager;
import ct23.xtreme.gameserver.datatables.DoorTable;
import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager;
import ct23.xtreme.gameserver.instancemanager.GraciaSeedsManager.GraciaSeedTypes;
import ct23.xtreme.gameserver.instancemanager.ZoneManager;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2DoorInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.interfaces.IL2Seed;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.zone.L2ZoneType;
import ct23.xtreme.gameserver.network.SystemMessageId;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;
import ct23.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct23.xtreme.gameserver.util.Util;
import ct23.xtreme.util.Rnd;

/**
 *
 *  @author Browser
 *	Rework L2j Script BrXtreme Freya
 */
public class EnergySeeds extends L2AttackableAIScript implements IL2Seed
{
	private static final String qn = "EnergySeeds";
	private static final String HOWTOOPPOSEEVIL = "Q00692_HowtoOpposeEvil";
	private static final int HOWTOOPPOSEEVIL_CHANCE = 60;
	private static final int RATE = 1;
	private static final int RESPAWN = 480000;
	private static final int RANDOM_RESPAWN_OFFSET = 180000;
	private static Map<Integer, ESSpawn> _spawns = new FastMap<>();
	private static Map<L2Npc, Integer> _spawnedNpcs = new FastMap<L2Npc, Integer>().shared();
	
	private static final int TEMPORARY_TELEPORTER = 32602;
	private static final int SOI_TELEPORTER = 32540;
	private static final int[] SEEDIDS = { 18678, 18679, 18680, 18681, 18682, 18683 };
	
	private static int[] SEED_OF_DESTRUCTION_DOORS =
	{
		12240003,12240004,12240005,12240006,12240007,12240008,
		12240009,12240010,12240011,12240012,12240013,12240014,
		12240015,12240016,12240017,12240018,12240019,12240020,
		12240021,12240022,12240023,12240024,12240025,12240026,
		12240027,12240028,12240029,12240030,12240031
	};
	
	private static final int SOD_ZONE = 60009;
	private static final int[] SOD_EXIT_POINT = { -248717, 250260, 4337 };
	
	private class ESSpawn
	{
		private int _spawnId;
		private GraciaSeedTypes _seedId;
		private int[] _npcIds;
		private int[] _spawnCoords;
		private int _respawn = RESPAWN;
		private int _respawnRnd = RANDOM_RESPAWN_OFFSET;
		
		public ESSpawn(int spawnId, GraciaSeedTypes seedId, int[] spawnCoords, int[] npcIds)
		{
			_spawnId = spawnId;
			_seedId = seedId;
			_spawnCoords = spawnCoords;
			_npcIds = npcIds;
		}
		
		public ESSpawn(int spawnId, GraciaSeedTypes seedId, int[] spawnCoords, int[] npcIds, int respawn, int respawnRnd)
		{
			_spawnId = spawnId;
			_seedId = seedId;
			_spawnCoords = spawnCoords;
			_npcIds = npcIds;
			_respawn = respawn;
			_respawnRnd = respawnRnd;
		}
		
		public void scheduleRespawn(long waitTime)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				public void run()
				{
					// if the AI is inactive, do not spawn the NPC
					if (isSeedActive(_seedId))
					{
						//get a random NPC that should spawn at this location
						Integer spawnId = _spawnId; // the map uses "Integer", not "int"
						_spawnedNpcs.put(addSpawn(_npcIds[Rnd.get(_npcIds.length)], _spawnCoords[0], _spawnCoords[1], _spawnCoords[2], 0, false, 0), spawnId);
					}
				}
			}, waitTime);
		}
		
		public int getRespawn()
		{ return _respawn;}
		
		public int getRespawnRnd()
		{ return _respawnRnd;}
	}
	
	private EnergySeeds(int questId, String name, String descr)
	{
		super(questId, name, descr);
		
		GraciaSeedsManager.getInstance().registerSeed(this);
		
		registerMobs(SEEDIDS);
		for(int i : SEEDIDS)
			addFirstTalkId(i);
		addFirstTalkId(TEMPORARY_TELEPORTER);
		addEnterZoneId(SOD_ZONE);
		addStartNpc(SOI_TELEPORTER);
		addTalkId(SOI_TELEPORTER);
		addSpawnsToList();
		startAI();
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		if (contains(SEEDIDS, npc.getNpcId()))
			npc.setIsNoRndWalk(true);
		return super.onSpawn(npc);
	}
	
	private boolean isSeedActive(GraciaSeedTypes seed)
	{
		switch(seed)
		{
			case INFINITY_SUFFERING:
				return GraciaSeedsManager.getInstance().getSoIState() == 3;
			case INFINITY_EROSION:
				return (GraciaSeedsManager.getInstance().getSoIState() == 3 || GraciaSeedsManager.getInstance().getSoIState() == 5);
			case INFINITY_INFINITY:
				return (GraciaSeedsManager.getInstance().getSoIState() == 3 || GraciaSeedsManager.getInstance().getSoIState() == 4);
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
			spawn.scheduleRespawn(RESPAWN+Rnd.get(RANDOM_RESPAWN_OFFSET));
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
				if (Rnd.get(100) < 33)
				{
					caster.sendPacket(new SystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED));
					caster.addItem("Loot", itemId, Rnd.get(RATE + 1, 2 * RATE), null, true);
				}
				else
				{
					caster.sendPacket(new SystemMessage(SystemMessageId.THE_COLLECTION_HAS_SUCCEEDED));
					caster.addItem("Loot", itemId, Rnd.get(1, RATE), null, true);
				}
				seedCollectEvent(caster, npc, spawn._seedId);
			}
		}
		
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("DeSpawnTask"))
		{
			if (npc.isInCombat())
				startQuestTimer("DeSpawnTask", 30000, npc, null);
			else
				npc.deleteMe();
		}
		return null;
	}
	
	@Override
	public String onFirstTalk (L2Npc npc, L2PcInstance player)
	{
		if (npc.getNpcId() == TEMPORARY_TELEPORTER)
			player.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
		player.sendPacket(ActionFailed.STATIC_PACKET);
		return null;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if (_spawnedNpcs.containsKey(npc) && _spawns.containsKey(_spawnedNpcs.get(npc)))
		{
			ESSpawn spawn = _spawns.get(_spawnedNpcs.get(npc));
			spawn.scheduleRespawn(spawn.getRespawn() + Rnd.get(spawn.getRespawnRnd()));
			_spawnedNpcs.remove(npc);
		}
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.getInstanceId() != 0)
			return super.onEnterZone(character,zone);
		
		if (character instanceof L2PcInstance)
		{
			switch(zone.getId())
			{
				case SOD_ZONE:
					if (!isSeedActive(GraciaSeedTypes.DESTRUCTION) && !character.isGM())
						character.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
					break;
			}
		}
		return super.onEnterZone(character,zone);
	}
	
	public void startAI()
	{
		// spawn all NPCs
		for (ESSpawn spawn : _spawns.values())
			if (isSeedActive(spawn._seedId))
				spawn.scheduleRespawn(0);
	}
	
	public void startAI(GraciaSeedTypes type)
	{
		stopAI(type);
		// spawn all NPCs
		for (ESSpawn spawn : _spawns.values())
			if (spawn._seedId == type)
				spawn.scheduleRespawn(0);
		
		if (type == GraciaSeedTypes.DESTRUCTION)
		{
			for (int doorId : SEED_OF_DESTRUCTION_DOORS)
			{
				L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
				if (doorInstance != null)
					doorInstance.openMe();
			}
		}
	}
	
	public void stopAI(GraciaSeedTypes type)
	{
		for (L2Npc seed : _spawnedNpcs.keySet())
			if (type == _spawns.get(_spawnedNpcs.get(seed))._seedId)
				seed.deleteMe();
		
		if (type == GraciaSeedTypes.DESTRUCTION)
		{
			for (int doorId : SEED_OF_DESTRUCTION_DOORS)
			{
				L2DoorInstance doorInstance = DoorTable.getInstance().getDoor(doorId);
				if (doorInstance != null)
					doorInstance.closeMe();
			}
			for(L2Character chars : ZoneManager.getInstance().getZoneById(SOD_ZONE).getCharactersInside(). values())
				if (chars instanceof L2PcInstance)
					chars.teleToLocation(SOD_EXIT_POINT[0], SOD_EXIT_POINT[1], SOD_EXIT_POINT[2]);
		}
	}
	
	public void seedCollectEvent(L2PcInstance player, L2Npc seedEnergy, GraciaSeedTypes seedType)
	{
		if (player == null)
			return;
		QuestState st = player.getQuestState(HOWTOOPPOSEEVIL);
		switch(seedType)
		{
			case INFINITY_SUFFERING:
			case INFINITY_EROSION:
			case INFINITY_INFINITY:
				if (st != null && st.getInt("cond") == 3)
					handleQuestDrop(st, 13798);
				break;
			case DESTRUCTION:
				if (st != null && st.getInt("cond") == 3)
					handleQuestDrop(st, 13867);
				break;
		}
	}
	
	private void handleQuestDrop(QuestState st, int itemId)
	{
		double chance = HOWTOOPPOSEEVIL_CHANCE * Config.RATE_QUEST_DROP;
		int numItems = (int) (chance / 100);
		chance = chance % 100;
		if (st.getRandom(100) < chance)
			numItems++;
		if (numItems > 0)
		{
			st.giveItems(itemId,numItems);
			st.playSound("ItemSound.quest_itemget");
		}
	}
	
	private void addSpawnsToList()
	{
		// Seed of Destruction
		//Temporary Teleporters
		_spawns.put(1, new ESSpawn(1, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245790,220320,-12104}, new int[]{TEMPORARY_TELEPORTER}));
		_spawns.put(2, new ESSpawn(2, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249770,207300,-11952}, new int[]{TEMPORARY_TELEPORTER}));
		//Energy Seeds
		_spawns.put(3, new ESSpawn(3, GraciaSeedTypes.DESTRUCTION, new int[]{0,-248360,219272,-12448}, new int[]{18678,18679,18680}));
		_spawns.put(4, new ESSpawn(4, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249448,219256,-12448}, new int[]{18678,18679,18680}));
		_spawns.put(5, new ESSpawn(5, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249432,220872,-12448}, new int[]{18678,18679,18680}));
		_spawns.put(6, new ESSpawn(6, GraciaSeedTypes.DESTRUCTION, new int[]{0,-248360,220888,-12448}, new int[]{18678,18679,18680}));
		
		_spawns.put(7, new ESSpawn(7, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250088,219256,-12448}, new int[]{18681,18682}));
		_spawns.put(8, new ESSpawn(8, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250600,219272,-12448}, new int[]{18681,18682}));
		_spawns.put(9, new ESSpawn(9, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250584,220904,-12448}, new int[]{18681,18682}));
		_spawns.put(10, new ESSpawn(10, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250072,220888,-12448}, new int[]{18681,18682}));
		
		_spawns.put(11, new ESSpawn(11, GraciaSeedTypes.DESTRUCTION, new int[]{0,-253096,217704,-12296}, new int[]{18683,18678}));
		_spawns.put(12, new ESSpawn(12, GraciaSeedTypes.DESTRUCTION, new int[]{0,-253112,217048,-12288}, new int[]{18683,18678}));
		_spawns.put(13, new ESSpawn(13, GraciaSeedTypes.DESTRUCTION, new int[]{0,-251448,217032,-12288}, new int[]{18683,18678}));
		_spawns.put(14, new ESSpawn(14, GraciaSeedTypes.DESTRUCTION, new int[]{0,-251416,217672,-12296}, new int[]{18683,18678}));
		
		_spawns.put(15, new ESSpawn(15, GraciaSeedTypes.DESTRUCTION, new int[]{0,-251416,217672,-12296}, new int[]{18679,18680}));
		_spawns.put(16, new ESSpawn(16, GraciaSeedTypes.DESTRUCTION, new int[]{0,-251416,217016,-12280}, new int[]{18679,18680}));
		_spawns.put(17, new ESSpawn(17, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249752,217016,-12280}, new int[]{18679,18680}));
		_spawns.put(18, new ESSpawn(18, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249736,217688,-12296}, new int[]{18679,18680}));
		
		_spawns.put(19, new ESSpawn(19, GraciaSeedTypes.DESTRUCTION, new int[]{0,-252472,215208,-12120}, new int[]{18681,18682}));
		_spawns.put(20, new ESSpawn(20, GraciaSeedTypes.DESTRUCTION, new int[]{0,-252552,216760,-12248}, new int[]{18681,18682}));
		_spawns.put(21, new ESSpawn(21, GraciaSeedTypes.DESTRUCTION, new int[]{0,-253160,216744,-12248}, new int[]{18681,18682}));
		_spawns.put(22, new ESSpawn(22, GraciaSeedTypes.DESTRUCTION, new int[]{0,-253128,215160,-12096}, new int[]{18681,18682}));
		
		_spawns.put(23, new ESSpawn(23, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250392,215208,-12120}, new int[]{18683,18678}));
		_spawns.put(24, new ESSpawn(24, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250264,216744,-12248}, new int[]{18683,18678}));
		_spawns.put(25, new ESSpawn(25, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249720,216744,-12248}, new int[]{18683,18678}));
		_spawns.put(26, new ESSpawn(26, GraciaSeedTypes.DESTRUCTION, new int[]{0,-249752,215128,-12096}, new int[]{18683,18678}));
		
		_spawns.put(27, new ESSpawn(27, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250280,216760,-12248}, new int[]{18679,18680,18681}));
		_spawns.put(28, new ESSpawn(28, GraciaSeedTypes.DESTRUCTION, new int[]{0,-250344,216152,-12248}, new int[]{18679,18680,18681}));
		_spawns.put(29, new ESSpawn(29, GraciaSeedTypes.DESTRUCTION, new int[]{0,-252504,216152,-12248}, new int[]{18679,18680,18681}));
		_spawns.put(30, new ESSpawn(30, GraciaSeedTypes.DESTRUCTION, new int[]{0,-252520,216792,-12248}, new int[]{18679,18680,18681}));
		
		_spawns.put(31, new ESSpawn(31, GraciaSeedTypes.DESTRUCTION, new int[]{0,-242520,217272,-12384}, new int[]{18681,18682,18683}));
		_spawns.put(32, new ESSpawn(32, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241432,217288,-12384}, new int[]{18681,18682,18683}));
		_spawns.put(33, new ESSpawn(33, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241432,218936,-12384}, new int[]{18681,18682,18683}));
		_spawns.put(34, new ESSpawn(34, GraciaSeedTypes.DESTRUCTION, new int[]{0,-242536,218936,-12384}, new int[]{18681,18682,18683}));
		
		_spawns.put(35, new ESSpawn(35, GraciaSeedTypes.DESTRUCTION, new int[]{0,-240808,217272,-12384}, new int[]{18678,18679}));
		_spawns.put(36, new ESSpawn(36, GraciaSeedTypes.DESTRUCTION, new int[]{0,-240280,217272,-12384}, new int[]{18678,18679}));
		_spawns.put(37, new ESSpawn(37, GraciaSeedTypes.DESTRUCTION, new int[]{0,-240280,218952,-12384}, new int[]{18678,18679}));
		_spawns.put(38, new ESSpawn(38, GraciaSeedTypes.DESTRUCTION, new int[]{0,-240792,218936,-12384}, new int[]{18678,18679}));
		
		_spawns.put(39, new ESSpawn(39, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239576,217240,-12640}, new int[]{18680,18681,18682}));
		_spawns.put(40, new ESSpawn(40, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239560,216168,-12640}, new int[]{18680,18681,18682}));
		_spawns.put(41, new ESSpawn(41, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237896,216152,-12640}, new int[]{18680,18681,18682}));
		_spawns.put(42, new ESSpawn(42, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237912,217256,-12640}, new int[]{18680,18681,18682}));
		
		_spawns.put(43, new ESSpawn(43, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237896,215528,-12640}, new int[]{18683,18678}));
		_spawns.put(44, new ESSpawn(44, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239560,215528,-12640}, new int[]{18683,18678}));
		_spawns.put(45, new ESSpawn(45, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239560,214984,-12640}, new int[]{18683,18678}));
		_spawns.put(46, new ESSpawn(46, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237896,215000,-12640}, new int[]{18683,18678}));
		
		_spawns.put(47, new ESSpawn(47, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237896,213640,-12768}, new int[]{18678,18679,18680}));
		_spawns.put(48, new ESSpawn(48, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239560,213640,-12768}, new int[]{18678,18679,18680}));
		_spawns.put(49, new ESSpawn(49, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239544,212552,-12768}, new int[]{18678,18679,18680}));
		_spawns.put(50, new ESSpawn(50, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237912,212552,-12768}, new int[]{18678,18679,18680}));
		
		_spawns.put(51, new ESSpawn(51, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237912,211912,-12768}, new int[]{18681,18682}));
		_spawns.put(52, new ESSpawn(52, GraciaSeedTypes.DESTRUCTION, new int[]{0,-237912,211400,-12768}, new int[]{18681,18682}));
		_spawns.put(53, new ESSpawn(53, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239560,211400,-12768}, new int[]{18681,18682}));
		_spawns.put(54, new ESSpawn(54, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239560,211912,-12768}, new int[]{18681,18682}));
		
		_spawns.put(55, new ESSpawn(55, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241960,214536,-12512}, new int[]{18683,18678,18679}));
		_spawns.put(56, new ESSpawn(56, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241976,213448,-12512}, new int[]{18683,18678,18679}));
		_spawns.put(57, new ESSpawn(57, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,213448,-12512}, new int[]{18683,18678,18679}));
		_spawns.put(58, new ESSpawn(58, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,214520,-12512}, new int[]{18683,18678,18679}));
		
		_spawns.put(59, new ESSpawn(59, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241976,212808,-12504}, new int[]{18680,18681}));
		_spawns.put(60, new ESSpawn(60, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241960,212280,-12504}, new int[]{18680,18681}));
		_spawns.put(61, new ESSpawn(61, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,212264,-12504}, new int[]{18680,18681}));
		_spawns.put(62, new ESSpawn(62, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,212792,-12504}, new int[]{18680,18681}));
		
		_spawns.put(63, new ESSpawn(63, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243640,210920,-12640}, new int[]{18682,18683,18678}));
		_spawns.put(64, new ESSpawn(64, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,209832,-12640}, new int[]{18682,18683,18678}));
		_spawns.put(65, new ESSpawn(65, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241976,209832,-12640}, new int[]{18682,18683,18678}));
		_spawns.put(66, new ESSpawn(66, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241976,210920,-12640}, new int[]{18682,18683,18678}));
		
		_spawns.put(67, new ESSpawn(67, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241976,209192,-12640}, new int[]{18679,18680}));
		_spawns.put(68, new ESSpawn(68, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241976,208664,-12640}, new int[]{18679,18680}));
		_spawns.put(69, new ESSpawn(69, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,208664,-12640}, new int[]{18679,18680}));
		_spawns.put(70, new ESSpawn(70, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243624,209192,-12640}, new int[]{18679,18680}));
		
		_spawns.put(71, new ESSpawn(71, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241256,208664,-12896}, new int[]{18681,18682,18683}));
		_spawns.put(72, new ESSpawn(72, GraciaSeedTypes.DESTRUCTION, new int[]{0,-240168,208648,-12896}, new int[]{18681,18682,18683}));
		_spawns.put(73, new ESSpawn(73, GraciaSeedTypes.DESTRUCTION, new int[]{0,-240168,207000,-12896}, new int[]{18681,18682,18683}));
		_spawns.put(74, new ESSpawn(74, GraciaSeedTypes.DESTRUCTION, new int[]{0,-241256,207000,-12896}, new int[]{18681,18682,18683}));
		
		_spawns.put(75, new ESSpawn(75, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239528,208648,-12896}, new int[]{18678,18679}));
		_spawns.put(76, new ESSpawn(76, GraciaSeedTypes.DESTRUCTION, new int[]{0,-238984,208664,-12896}, new int[]{18678,18679}));
		_spawns.put(77, new ESSpawn(77, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239000,207000,-12896}, new int[]{18678,18679}));
		_spawns.put(78, new ESSpawn(78, GraciaSeedTypes.DESTRUCTION, new int[]{0,-239512,207000,-12896}, new int[]{18678,18679}));
		
		_spawns.put(79, new ESSpawn(79, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245064,213144,-12384}, new int[]{18680,18681,18682}));
		_spawns.put(80, new ESSpawn(80, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245064,212072,-12384}, new int[]{18680,18681,18682}));
		_spawns.put(81, new ESSpawn(81, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246696,212072,-12384}, new int[]{18680,18681,18682}));
		_spawns.put(82, new ESSpawn(82, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246696,213160,-12384}, new int[]{18680,18681,18682}));
		
		_spawns.put(83, new ESSpawn(83, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245064,211416,-12384}, new int[]{18683,18678}));
		_spawns.put(84, new ESSpawn(84, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245048,210904,-12384}, new int[]{18683,18678}));
		_spawns.put(85, new ESSpawn(85, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246712,210888,-12384}, new int[]{18683,18678}));
		_spawns.put(86, new ESSpawn(86, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246712,211416,-12384}, new int[]{18683,18678}));
		
		_spawns.put(87, new ESSpawn(87, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245048,209544,-12512}, new int[]{18679,18680,18681}));
		_spawns.put(88, new ESSpawn(88, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245064,208456,-12512}, new int[]{18679,18680,18681}));
		_spawns.put(89, new ESSpawn(89, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246696,208456,-12512}, new int[]{18679,18680,18681}));
		_spawns.put(90, new ESSpawn(90, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246712,209544,-12512}, new int[]{18679,18680,18681}));
		
		_spawns.put(91, new ESSpawn(91, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245048,207816,-12512}, new int[]{18682,18683}));
		_spawns.put(92, new ESSpawn(92, GraciaSeedTypes.DESTRUCTION, new int[]{0,-245048,207288,-12512}, new int[]{18682,18683}));
		_spawns.put(93, new ESSpawn(93, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246696,207304,-12512}, new int[]{18682,18683}));
		_spawns.put(94, new ESSpawn(94, GraciaSeedTypes.DESTRUCTION, new int[]{0,-246712,207816,-12512}, new int[]{18682,18683}));
		
		_spawns.put(95, new ESSpawn(95, GraciaSeedTypes.DESTRUCTION, new int[]{0,-244328,207272,-12768}, new int[]{18678,18679,18680}));
		_spawns.put(96, new ESSpawn(96, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243256,207256,-12768}, new int[]{18678,18679,18680}));
		_spawns.put(97, new ESSpawn(97, GraciaSeedTypes.DESTRUCTION, new int[]{0,-243256,205624,-12768}, new int[]{18678,18679,18680}));
		_spawns.put(98, new ESSpawn(98, GraciaSeedTypes.DESTRUCTION, new int[]{0,-244328,205608,-12768}, new int[]{18678,18679,18680}));
		
		_spawns.put(99, new ESSpawn(99, GraciaSeedTypes.DESTRUCTION, new int[]{0,-242616,207272,-12768}, new int[]{18681,18682}));
		_spawns.put(100, new ESSpawn(100, GraciaSeedTypes.DESTRUCTION, new int[]{0,-242104,207272,-12768}, new int[]{18681,18682}));
		_spawns.put(101, new ESSpawn(101, GraciaSeedTypes.DESTRUCTION, new int[]{0,-242088,205624,-12768}, new int[]{18681,18682}));
		_spawns.put(102, new ESSpawn(102, GraciaSeedTypes.DESTRUCTION, new int[]{0,-242600,205608,-12768}, new int[]{18681,18682}));
		
		//Seed of Infinity
		// respawn seeds /2
		_spawns.put(400, new ESSpawn(400, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-179538,211313,-15488,16384}, new int[]{ 32547 },60000,0));//npc_echimus
		_spawns.put(401, new ESSpawn(401, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-179779,212540,-15520,49151}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(402, new ESSpawn(402, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6237,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy 70m,20m
		_spawns.put(403, new ESSpawn(403, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6237,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(404, new ESSpawn(404, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6237,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(405, new ESSpawn(405, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-177028,211135,-15520,36863}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(406, new ESSpawn(406, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6238,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(407, new ESSpawn(407, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6238,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(408, new ESSpawn(408, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6238,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(409, new ESSpawn(409, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-176355,208043,-15520,28672}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(410, new ESSpawn(410, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6239,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(411, new ESSpawn(411, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6239,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(412, new ESSpawn(412, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6239,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(413, new ESSpawn(413, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-179284,205990,-15520,16384}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(414, new ESSpawn(414, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6240,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(415, new ESSpawn(415, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6240,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(416, new ESSpawn(416, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6240,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(417, new ESSpawn(417, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-182268,208218,-15520,4096}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(418, new ESSpawn(418, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6241,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(419, new ESSpawn(419, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6241,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(420, new ESSpawn(420, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6241,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(421, new ESSpawn(421, GraciaSeedTypes.INFINITY_INFINITY, new int[]{0,-182069,211140,-15520,61439}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(422, new ESSpawn(422, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6242,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(423, new ESSpawn(423, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6242,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		_spawns.put(424, new ESSpawn(424, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6242,0,0,0,-1}, new int[]{ 18682,18683 },2100000,600000));//seed_energe_holy,seed_energe_unholy
		
		_spawns.put(500, new ESSpawn(500, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-179659,211061,-12784,49151}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(501, new ESSpawn(501, GraciaSeedTypes.INFINITY_EROSION, new int[]{6243,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(502, new ESSpawn(502, GraciaSeedTypes.INFINITY_EROSION, new int[]{6243,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(503, new ESSpawn(503, GraciaSeedTypes.INFINITY_EROSION, new int[]{6243,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(504, new ESSpawn(504, GraciaSeedTypes.INFINITY_EROSION, new int[]{6243,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(505, new ESSpawn(505, GraciaSeedTypes.INFINITY_EROSION, new int[]{6243,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(506, new ESSpawn(506, GraciaSeedTypes.INFINITY_EROSION, new int[]{6243,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(507, new ESSpawn(507, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-178418,211653,-12029,49151}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(508, new ESSpawn(508, GraciaSeedTypes.INFINITY_EROSION, new int[]{6244,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(509, new ESSpawn(509, GraciaSeedTypes.INFINITY_EROSION, new int[]{6244,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(510, new ESSpawn(510, GraciaSeedTypes.INFINITY_EROSION, new int[]{6244,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(511, new ESSpawn(511, GraciaSeedTypes.INFINITY_EROSION, new int[]{6244,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(512, new ESSpawn(512, GraciaSeedTypes.INFINITY_EROSION, new int[]{6244,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(513, new ESSpawn(513, GraciaSeedTypes.INFINITY_EROSION, new int[]{6244,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(514, new ESSpawn(514, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-178417,206558,-12032,16384}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(515, new ESSpawn(515, GraciaSeedTypes.INFINITY_EROSION, new int[]{6245,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(516, new ESSpawn(516, GraciaSeedTypes.INFINITY_EROSION, new int[]{6245,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(517, new ESSpawn(517, GraciaSeedTypes.INFINITY_EROSION, new int[]{6245,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(518, new ESSpawn(518, GraciaSeedTypes.INFINITY_EROSION, new int[]{6245,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(519, new ESSpawn(519, GraciaSeedTypes.INFINITY_EROSION, new int[]{6245,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(520, new ESSpawn(520, GraciaSeedTypes.INFINITY_EROSION, new int[]{6245,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(521, new ESSpawn(521, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-180911,206551,-12028,16384}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(522, new ESSpawn(522, GraciaSeedTypes.INFINITY_EROSION, new int[]{6246,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(523, new ESSpawn(523, GraciaSeedTypes.INFINITY_EROSION, new int[]{6246,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(524, new ESSpawn(524, GraciaSeedTypes.INFINITY_EROSION, new int[]{6246,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(525, new ESSpawn(525, GraciaSeedTypes.INFINITY_EROSION, new int[]{6246,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(526, new ESSpawn(526, GraciaSeedTypes.INFINITY_EROSION, new int[]{6246,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(527, new ESSpawn(527, GraciaSeedTypes.INFINITY_EROSION, new int[]{6246,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(528, new ESSpawn(528, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-180911,211652,-12028,49151}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(529, new ESSpawn(529, GraciaSeedTypes.INFINITY_EROSION, new int[]{6247,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(530, new ESSpawn(530, GraciaSeedTypes.INFINITY_EROSION, new int[]{6247,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(531, new ESSpawn(531, GraciaSeedTypes.INFINITY_EROSION, new int[]{6247,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(532, new ESSpawn(532, GraciaSeedTypes.INFINITY_EROSION, new int[]{6247,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(533, new ESSpawn(533, GraciaSeedTypes.INFINITY_EROSION, new int[]{6247,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(534, new ESSpawn(534, GraciaSeedTypes.INFINITY_EROSION, new int[]{6247,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(535, new ESSpawn(535, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-176036,210002,-11948,36863}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(536, new ESSpawn(536, GraciaSeedTypes.INFINITY_EROSION, new int[]{6248,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(537, new ESSpawn(537, GraciaSeedTypes.INFINITY_EROSION, new int[]{6248,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(538, new ESSpawn(538, GraciaSeedTypes.INFINITY_EROSION, new int[]{6248,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(539, new ESSpawn(539, GraciaSeedTypes.INFINITY_EROSION, new int[]{6248,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(540, new ESSpawn(540, GraciaSeedTypes.INFINITY_EROSION, new int[]{6248,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(541, new ESSpawn(541, GraciaSeedTypes.INFINITY_EROSION, new int[]{6248,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(542, new ESSpawn(542, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-176039,208203,-11949,28672}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(543, new ESSpawn(543, GraciaSeedTypes.INFINITY_EROSION, new int[]{6249,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(544, new ESSpawn(544, GraciaSeedTypes.INFINITY_EROSION, new int[]{6249,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(545, new ESSpawn(545, GraciaSeedTypes.INFINITY_EROSION, new int[]{6249,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(546, new ESSpawn(546, GraciaSeedTypes.INFINITY_EROSION, new int[]{6249,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(547, new ESSpawn(547, GraciaSeedTypes.INFINITY_EROSION, new int[]{6249,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(548, new ESSpawn(548, GraciaSeedTypes.INFINITY_EROSION, new int[]{6249,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(549, new ESSpawn(549, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-183288,208205,-11939,4096}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(550, new ESSpawn(550, GraciaSeedTypes.INFINITY_EROSION, new int[]{6250,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(551, new ESSpawn(551, GraciaSeedTypes.INFINITY_EROSION, new int[]{6250,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(552, new ESSpawn(552, GraciaSeedTypes.INFINITY_EROSION, new int[]{6250,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(553, new ESSpawn(553, GraciaSeedTypes.INFINITY_EROSION, new int[]{6250,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(554, new ESSpawn(554, GraciaSeedTypes.INFINITY_EROSION, new int[]{6250,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(555, new ESSpawn(555, GraciaSeedTypes.INFINITY_EROSION, new int[]{6250,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(556, new ESSpawn(556, GraciaSeedTypes.INFINITY_EROSION, new int[]{0,-183290,210004,-11939,61439}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(557, new ESSpawn(557, GraciaSeedTypes.INFINITY_EROSION, new int[]{6251,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(558, new ESSpawn(558, GraciaSeedTypes.INFINITY_EROSION, new int[]{6251,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(559, new ESSpawn(559, GraciaSeedTypes.INFINITY_EROSION, new int[]{6251,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(560, new ESSpawn(560, GraciaSeedTypes.INFINITY_EROSION, new int[]{6251,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(561, new ESSpawn(561, GraciaSeedTypes.INFINITY_EROSION, new int[]{6251,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(562, new ESSpawn(562, GraciaSeedTypes.INFINITY_EROSION, new int[]{6251,0,0,0,-1}, new int[]{ 18678,18679,18680,18681 },1800000,600000));//seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		
		_spawns.put(600, new ESSpawn(600, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-186327,208286,-9536,-16096}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(601, new ESSpawn(601, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(602, new ESSpawn(602, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(603, new ESSpawn(603, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(604, new ESSpawn(604, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(605, new ESSpawn(605, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(606, new ESSpawn(606, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(607, new ESSpawn(607, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(608, new ESSpawn(608, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(609, new ESSpawn(609, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(610, new ESSpawn(610, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(611, new ESSpawn(611, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(612, new ESSpawn(612, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6252,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(613, new ESSpawn(613, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-184429,211155,-9536,-27768}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(614, new ESSpawn(614, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(615, new ESSpawn(615, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(616, new ESSpawn(616, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(617, new ESSpawn(617, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(618, new ESSpawn(618, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(619, new ESSpawn(619, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(620, new ESSpawn(620, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(621, new ESSpawn(621, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(622, new ESSpawn(622, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(623, new ESSpawn(623, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(624, new ESSpawn(624, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(625, new ESSpawn(625, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6253,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(626, new ESSpawn(626, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-182811,213871,-9504,-16376}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(627, new ESSpawn(627, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(628, new ESSpawn(628, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(629, new ESSpawn(629, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(630, new ESSpawn(630, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(631, new ESSpawn(631, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(632, new ESSpawn(632, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(633, new ESSpawn(633, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(634, new ESSpawn(634, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(635, new ESSpawn(635, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(636, new ESSpawn(636, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(637, new ESSpawn(637, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(638, new ESSpawn(638, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6254,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(639, new ESSpawn(639, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-180921,216789,-9536,-28008}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(640, new ESSpawn(640, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(641, new ESSpawn(641, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(642, new ESSpawn(642, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(643, new ESSpawn(643, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(644, new ESSpawn(644, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(645, new ESSpawn(645, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(646, new ESSpawn(646, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(647, new ESSpawn(647, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(648, new ESSpawn(648, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(649, new ESSpawn(649, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(650, new ESSpawn(650, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(651, new ESSpawn(651, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6255,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(652, new ESSpawn(652, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-177264,217760,-9536,0}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(653, new ESSpawn(653, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(654, new ESSpawn(654, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(655, new ESSpawn(655, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(656, new ESSpawn(656, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(657, new ESSpawn(657, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(658, new ESSpawn(658, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(659, new ESSpawn(659, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(660, new ESSpawn(660, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(661, new ESSpawn(661, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(662, new ESSpawn(662, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(663, new ESSpawn(663, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(664, new ESSpawn(664, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6256,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(665, new ESSpawn(665, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-173727,218169,-9536,-16384}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(666, new ESSpawn(666, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(667, new ESSpawn(667, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(668, new ESSpawn(668, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(669, new ESSpawn(669, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(670, new ESSpawn(670, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(671, new ESSpawn(671, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(672, new ESSpawn(672, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(673, new ESSpawn(673, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(674, new ESSpawn(674, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(675, new ESSpawn(675, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(676, new ESSpawn(676, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(677, new ESSpawn(677, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6257,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(678, new ESSpawn(678, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{0,-187776,205696,-9536,0}, new int[]{ 32542 },60000,0));//lifeseed_stab_spc
		_spawns.put(679, new ESSpawn(679, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(680, new ESSpawn(680, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(681, new ESSpawn(681, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(682, new ESSpawn(682, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(683, new ESSpawn(683, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(684, new ESSpawn(684, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(685, new ESSpawn(685, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(686, new ESSpawn(686, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(687, new ESSpawn(687, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(688, new ESSpawn(688, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(689, new ESSpawn(689, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		_spawns.put(690, new ESSpawn(690, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6258,0,0,0,-1}, new int[]{ 18682,18683,18678,18679,18680,18681 },1500000,600000));//seed_energe_holy,seed_energe_unholy,seed_energe_water,seed_energe_fire,seed_energe_wind,seed_energe_earth
		
		_spawns.put(700, new ESSpawn(700, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6259,0,0,0,-1}, new int[]{ 25652 },5400000,1800000));//eventboss_lich_b
		_spawns.put(701, new ESSpawn(701, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6260,0,0,0,-1}, new int[]{ 25651 },5400000,1800000));//eventboss_lich_b
		
		_spawns.put(702, new ESSpawn(702, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6262,0,0,0,-1}, new int[]{ 25650 },2700000,900000));//eventboss_deathslayer_b
		_spawns.put(703, new ESSpawn(703, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6263,0,0,0,-1}, new int[]{ 25650 },5400000,1800000));//eventboss_deathslayer_b
		_spawns.put(704, new ESSpawn(704, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6264,0,0,0,-1}, new int[]{ 25649 },2700000,900000));//eventboss_deathslayer_a
		_spawns.put(705, new ESSpawn(705, GraciaSeedTypes.INFINITY_SUFFERING, new int[]{6265,0,0,0,-1}, new int[]{ 25649 },5400000,1800000));//eventboss_deathslayer_a
		
		_spawns.put(706, new ESSpawn(706, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6268,0,0,0,-1}, new int[]{ 25648 },1200000,600000));//eventboss_deathknight_b
		_spawns.put(707, new ESSpawn(707, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6269,0,0,0,-1}, new int[]{ 25648 },2700000,900000));//eventboss_deathknight_b
		_spawns.put(708, new ESSpawn(708, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6270,0,0,0,-1}, new int[]{ 25648 },5400000,1800000));//eventboss_deathknight_b
		_spawns.put(709, new ESSpawn(709, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6271,0,0,0,-1}, new int[]{ 25647 },1200000,600000));//eventboss_deathknight_a
		_spawns.put(710, new ESSpawn(710, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6272,0,0,0,-1}, new int[]{ 25647 },2700000,900000));//eventboss_deathknight_a
		_spawns.put(711, new ESSpawn(711, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6273,0,0,0,-1}, new int[]{ 25647 },5400000,1800000));//eventboss_deathknight_a
		
		_spawns.put(712, new ESSpawn(712, GraciaSeedTypes.INFINITY_EROSION, new int[]{6278,0,0,0,-1}, new int[]{ 25646 },720000,240000));//eventboss_soulwagon
		_spawns.put(713, new ESSpawn(713, GraciaSeedTypes.INFINITY_EROSION, new int[]{6279,0,0,0,-1}, new int[]{ 25646 },1200000,600000));//eventboss_soulwagon
		_spawns.put(714, new ESSpawn(714, GraciaSeedTypes.INFINITY_EROSION, new int[]{6280,0,0,0,-1}, new int[]{ 25646 },2700000,900000));//eventboss_soulwagon
		_spawns.put(715, new ESSpawn(715, GraciaSeedTypes.INFINITY_EROSION, new int[]{6281,0,0,0,-1}, new int[]{ 25646 },5400000,1800000));//eventboss_soulwagon
		
		_spawns.put(716, new ESSpawn(716, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6282,0,0,0,-1}, new int[]{ 25645 },420000,120000));//eventboss_soulharvester
		_spawns.put(717, new ESSpawn(717, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6282,0,0,0,-1}, new int[]{ 25645 },720000,240000));//eventboss_soulharvester
		_spawns.put(718, new ESSpawn(718, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6282,0,0,0,-1}, new int[]{ 25645 },1200000,600000));//eventboss_soulharvester
		_spawns.put(719, new ESSpawn(719, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6282,0,0,0,-1}, new int[]{ 25645 },2700000,900000));//eventboss_soulharvester
		_spawns.put(720, new ESSpawn(720, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6282,0,0,0,-1}, new int[]{ 25645 },5400000,1800000));//eventboss_soulharvester
		
		_spawns.put(721, new ESSpawn(721, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6283,0,0,0,-1}, new int[]{ 25644 },420000,120000));//eventboss_bonecreeper
		_spawns.put(722, new ESSpawn(722, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6284,0,0,0,-1}, new int[]{ 25644 },720000,240000));//eventboss_bonecreeper
		_spawns.put(723, new ESSpawn(723, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6285,0,0,0,-1}, new int[]{ 25644 },1200000,600000));//eventboss_bonecreeper
		_spawns.put(724, new ESSpawn(724, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6286,0,0,0,-1}, new int[]{ 25644 },2700000,900000));//eventboss_bonecreeper
		_spawns.put(725, new ESSpawn(725, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6287,0,0,0,-1}, new int[]{ 25644 },5400000,1800000));//eventboss_bonecreeper
		
		_spawns.put(726, new ESSpawn(726, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6289,0,0,0,-1}, new int[]{ 25643 },420000,120000));//eventboss_deathscout
		_spawns.put(727, new ESSpawn(727, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6290,0,0,0,-1}, new int[]{ 25643 },720000,240000));//eventboss_deathscout
		_spawns.put(728, new ESSpawn(728, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6291,0,0,0,-1}, new int[]{ 25643 },1200000,600000));//eventboss_deathscout
		_spawns.put(729, new ESSpawn(729, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6292,0,0,0,-1}, new int[]{ 25643 },2700000,900000));//eventboss_deathscout
		_spawns.put(730, new ESSpawn(730, GraciaSeedTypes.INFINITY_INFINITY, new int[]{6293,0,0,0,-1}, new int[]{ 25643 },5400000,1800000));//eventboss_deathscout
	}
	
	public static void main(String[] args)
	{
		new EnergySeeds(-1, qn, "engines");
	}
}
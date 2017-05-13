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
package instances.CavernOfThePirateCaptain;

import java.util.Calendar;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.GameTimeController;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.instancemanager.InstanceManager;
import ct25.xtreme.gameserver.instancemanager.InstanceManager.InstanceWorld;
import ct25.xtreme.gameserver.model.L2CommandChannel;
import ct25.xtreme.gameserver.model.L2World;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.entity.Instance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.SystemMessageId;
import ct25.xtreme.gameserver.network.serverpackets.MagicSkillUse;
import ct25.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct25.xtreme.gameserver.util.Util;
import ct25.xtreme.util.Rnd;

/**
 * Zaken AI (NightMare)
 */
public class CavernOfThePirateCaptainNightmare extends Quest
{

	private class ZakenNightWorld extends InstanceWorld
	{

		public ZakenNightWorld()
		{
			InstanceManager.getInstance();
		}
	}

	private static final String qn = "CavernOfThePirateCaptainNightmare";
	private static final int INSTANCEID = 114; // this is the client number
	private final boolean debug = false;
	private boolean teleported = false;
	
	private final int[][] SPAWNS =
	{
		// Floor 1
		{
			54240,
			220133,
			-3498
		},
		{
			54240,
			218073,
			-3498
		},
		{
			55265,
			219095,
			-3498
		},
		{
			56289,
			220133,
			-3498
		},
		{
			56289,
			218073,
			-3498
		},
		// Floor 2
		{
			54240,
			220133,
			-3226
		},
		{
			54240,
			218073,
			-3226
		},
		{
			55265,
			219095,
			-3226
		},
		{
			56289,
			220133,
			-3226
		},
		{
			56289,
			218073,
			-3226
		},
		// Floor 3
		{
			54240,
			220133,
			-2954
		},
		{
			54240,
			218073,
			-2954
		},
		{
			55265,
			219095,
			-2954
		},
		{
			56289,
			220133,
			-2954
		},
		{
			56289,
			218073,
			-2954
		}
	};

	private static final int ZAKEN = 29022;
	private static final int doll_blader_b = 29023;
	private static final int vale_master_b = 29024;
	private static final int pirates_zombie_captain_b = 29026;
	private static final int pirates_zombie_b = 29027;
	private static final int EXIT_TIME = 5;
	private static final int TELEPORTER = 32713;

	public CavernOfThePirateCaptainNightmare(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(TELEPORTER);
		addTalkId(TELEPORTER);
		addKillId(ZAKEN);
		addAttackId(ZAKEN);
	}
	
	private void teleportPlayer(final L2PcInstance player, final ZakenNightWorld world)
	{
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.setInstanceId(world.instanceId);
		player.teleToLocation(52680, 219088, -3232);
		if (player.getPet() != null)
		{
			player.getPet().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			player.getPet().setInstanceId(world.instanceId);
			player.getPet().teleToLocation(52680, 219088, -3232);
		}
		return;
	}

	private int getTimeHour()
	{
		return GameTimeController.getInstance().getGameTime() / 60 % 24;
	}

	private boolean checkConditions(final L2PcInstance player)
	{
		if (debug || player.isGM())
			return true;
		if (getTimeHour() > 4 || getTimeHour() < 24)
		{
			player.sendPacket(SystemMessage.getSystemMessage(1940));
			return false;
		}
		if (player.getParty() == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		if (player.getParty().getCommandChannel() == null)
		{
			player.getParty().broadcastToPartyMembers(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER));
			return false;
		}
		final L2CommandChannel CC = player.getParty().getCommandChannel();
		if (CC.getMemberCount() < Config.Min_Nightime_Zaken_Players || CC.getMemberCount() > 450)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
			return false;
		}
		if (CC.getChannelLeader() != player)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		for (final L2PcInstance plr : CC.getMembers())
		{
			if (plr.getQuestState("") == null)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(plr);
				CC.broadcastToChannelMembers(sm);
			}

			if (!Util.checkIfInRange(1000, player, plr, true))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(plr);
				CC.broadcastToChannelMembers(sm);
				return false;
			}
			final Long reentertime = InstanceManager.getInstance().getInstanceTime(plr.getObjectId(), INSTANCEID);
			if (System.currentTimeMillis() < reentertime)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(plr);
				CC.broadcastToChannelMembers(sm);
				return false;
			}
		}
		return true;
	}

	protected int enterInstance(final L2PcInstance player, final String template)
	{
		int instanceId = 0;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		// existing instance
		if (world != null)
		{
			if (!(world instanceof ZakenNightWorld))
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER));
				return 0;
			}
			teleportPlayer(player, (ZakenNightWorld) world);
			return world.instanceId;
		}
		// New instance
		if (!checkConditions(player))
			return 0;
		instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		world = new ZakenNightWorld();
		world.instanceId = instanceId;
		world.templateId = INSTANCEID;
		world.status = 0;
		InstanceManager.getInstance().addWorld(world);
		addSpawn(ZAKEN, 55312, 219168, -3223, 0, false, 0, false, instanceId);
		((L2Attackable) addSpawn(pirates_zombie_b, 54228, 217504, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54181, 217168, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 54714, 217123, -3168, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 55298, 217127, -3073, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 55787, 217130, -2993, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 56284, 217216, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 56963, 218080, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 56267, 218826, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56294, 219482, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 56094, 219113, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56364, 218967, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 56276, 220783, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 57173, 220234, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54885, 220144, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55264, 219860, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 55399, 220263, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55679, 220129, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 54236, 220948, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 54464, 219095, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54226, 218797, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54394, 219067, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54139, 219253, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 54262, 219480, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 53412, 218077, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 54280, 217200, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55440, 218081, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 55202, 217940, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55225, 218236, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54973, 218075, -2944, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 52675, 219371, -3290, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 52687, 219596, -3368, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 52672, 219740, -3418, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 52857, 219992, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 52959, 219997, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 53381, 220151, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 54236, 220948, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54885, 220144, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55264, 219860, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 55399, 220263, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55679, 220129, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 56276, 220783, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 57173, 220234, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 56267, 218826, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56294, 219482, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 56094, 219113, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56364, 218967, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 57113, 218079, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56186, 217153, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55440, 218081, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 55202, 217940, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55225, 218236, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54973, 218075, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 53412, 218077, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54226, 218797, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54394, 219067, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54139, 219253, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 54262, 219480, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 53412, 218077, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54413, 217132, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 54841, 217132, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 55372, 217128, -3343, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 55893, 217122, -3488, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 56282, 217237, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 56963, 218080, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 56267, 218826, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56294, 219482, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 56094, 219113, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 56364, 218967, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 56276, 220783, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 57173, 220234, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54885, 220144, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55264, 219860, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 55399, 220263, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55679, 220129, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 54236, 220948, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 54464, 219095, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54226, 218797, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(vale_master_b, 54394, 219067, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54139, 219253, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(doll_blader_b, 54262, 219480, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 53412, 218077, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55440, 218081, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_captain_b, 55202, 217940, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 55225, 218236, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		((L2Attackable) addSpawn(pirates_zombie_b, 54973, 218075, -3216, Rnd.get(65536), false, 0, false, instanceId)).setIsRaidMinion(true);
		
		_log.info("Zaken Night started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
		// teleport players
		if (debug || player.isGM())
		{
			teleportPlayer(player, (ZakenNightWorld) world);
			world.allowed.add(player.getObjectId());
		}
		else
			for (final L2PcInstance plr : player.getParty().getCommandChannel().getMembers())
			{
				teleportPlayer(player, (ZakenNightWorld) world);
				world.allowed.add(plr.getObjectId());
			}
		return instanceId;
	}
	
	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final int i = Rnd.get(SPAWNS.length);
		if (npc.getId() == ZAKEN && !npc.isDead())
			if (event.equalsIgnoreCase("teleport"))
			{
				((L2Attackable) npc).reduceHate(player, 9999);
				((L2Attackable) npc).abortAttack();
				((L2Attackable) npc).abortCast();
				npc.broadcastPacket(new MagicSkillUse(npc, 4222, 1, 1000, 0));
				startQuestTimer("finish_teleport", 1500, npc, player);
			}
			else if (event.equalsIgnoreCase("finish_teleport"))
			{
				npc.teleToLocation(SPAWNS[i][0], SPAWNS[i][1], SPAWNS[i][2]);
				npc.getSpawn().setLocx(SPAWNS[i][0]);
				npc.getSpawn().setLocy(SPAWNS[i][1]);
				npc.getSpawn().setLocz(SPAWNS[i][2]);
				teleported = false;
			}
		return event;
	}

	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (npc.getId() == ZAKEN)
			if (!teleported)
			{
				startQuestTimer("teleport", 300000, npc, attacker);
				teleported = true;
			}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final InstanceWorld tmpWorld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if (tmpWorld instanceof ZakenNightWorld)
		{
			final ZakenNightWorld world = (ZakenNightWorld) tmpWorld;
			final int npcId = npc.getId();
			if (npcId == ZAKEN)
				finishInstance(world);
		}
		return super.onKill(npc, killer, isPet);
	}

	private void finishInstance(final ZakenNightWorld world)
	{
		final Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, 30);
		reenter.set(Calendar.HOUR_OF_DAY, 6);
		// if time is >= RESET_HOUR - roll to the next day
		if (reenter.getTimeInMillis() <= System.currentTimeMillis())
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		if (reenter.get(Calendar.DAY_OF_WEEK) <= Calendar.WEDNESDAY)
			while (reenter.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY)
				reenter.add(Calendar.DAY_OF_MONTH, 1);
		else
			while (reenter.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY)
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(INSTANCEID));
		
		// set instance reenter time for all allowed players
		for (final int objectId : world.allowed)
		{
			final L2PcInstance player = L2World.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, INSTANCEID, reenter.getTimeInMillis());
			if (player != null && player.isOnline())
				player.sendPacket(sm);
		}
		// destroy instance after EXIT_TIME
		final Instance inst = InstanceManager.getInstance().getInstance(world.instanceId);
		inst.setDuration(EXIT_TIME * 60000);
		inst.setEmptyDestroyTime(0);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		QuestState st = player.getQuestState(qn);
		if (st == null)
			st = newQuestState(player);
		if (npcId == TELEPORTER)
			enterInstance(player, "CavernOfThePirateCaptainNightmare.xml");
		return "";
	}
	
	public static void main(final String[] args)
	{
		new CavernOfThePirateCaptainNightmare(-1, qn, "instances");
	}
}
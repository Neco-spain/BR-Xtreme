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
package ai.individual.grandboss;

import static ct25.xtreme.gameserver.ai.CtrlIntention.AI_INTENTION_FOLLOW;
import static ct25.xtreme.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.Config;
import ct25.xtreme.gameserver.GeoData;
import ct25.xtreme.gameserver.ThreadPoolManager;
import ct25.xtreme.gameserver.datatables.SkillTable;
import ct25.xtreme.gameserver.instancemanager.GrandBossManager;
import ct25.xtreme.gameserver.model.L2Effect;
import ct25.xtreme.gameserver.model.L2Object;
import ct25.xtreme.gameserver.model.L2Skill;
import ct25.xtreme.gameserver.model.actor.L2Character;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.L2Playable;
import ct25.xtreme.gameserver.model.actor.instance.L2DecoyInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2GrandBossInstance;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.QuestTimer;
import ct25.xtreme.gameserver.model.zone.type.L2BossZone;
import ct25.xtreme.gameserver.network.serverpackets.Earthquake;
import ct25.xtreme.gameserver.network.serverpackets.MoveToPawn;
import ct25.xtreme.gameserver.network.serverpackets.PlaySound;
import ct25.xtreme.gameserver.templates.StatsSet;
import ct25.xtreme.gameserver.util.Util;
import javolution.util.FastList;

/**
 * Baium AI Note1: if the server gets rebooted while players are still fighting Baium, there is no lock, but players also lose their ability to wake baium up. However, should another person enter the room and wake him up, the players who had stayed inside may join the raid. This can be helpful for
 * players who became victims of a reboot (they only need 1 new player to enter and wake up baium) and is not too exploitable since any player wishing to exploit it would have to suffer 5 days of being parked in an empty room. Note2: Neither version of Baium should be a permanent spawn. This script
 * is fully capable of spawning the statue-version when the lock expires and switching it to the mob version promptly. Additional notes ( source http://aleenaresron.blogspot.com/2006_08_01_archive.html ): * Baium only first respawns five days after his last death. And from those five days he will
 * respawn within 1-8 hours of his last death. So, you have to know his last time of death. * If by some freak chance you are the only one in Baium's chamber and NO ONE comes in [ha, ha] you or someone else will have to wake Baium. There is a good chance that Baium will automatically kill whoever
 * wakes him. There are some people that have been able to wake him and not die, however if you've already gone through the trouble of getting the bloody fabric and camped him out and researched his spawn time, are you willing to take that chance that you'll wake him and not be able to finish your
 * quest? Doubtful. [ this powerful attack vs the player who wakes him up is NOT yet implemented here] * once someone starts attacking Baium no one else can port into the chamber where he is. Unlike with the other raid bosses, you can just show up at any time as long as you are there when they die.
 * Not true with Baium. Once he gets attacked, the port to Baium closes. byebye, see you in 5 days. If nobody attacks baium for 30 minutes, he auto-despawns and unlocks the vortex
 * @author Fulminus version 0.1
 */
public class Baium extends L2AttackableAIScript
{
	private L2Character _target;
	private L2Skill _skill;

	// Npcs Ids
	private static final int STONE_BAIUM = 29025;
	private static final int ANGELIC_VORTEX = 31862;
	private static final int LIVE_BAIUM = 29020;
	private static final int ARCHANGEL = 29021;

	// Baium status tracking
	private static final byte ASLEEP = 0; // baium is in the stone version, waiting to be woken up. Entry is unlocked
	private static final byte AWAKE = 1; // baium is awake and fighting. Entry is locked.
	private static final byte DEAD = 2; // baium has been killed and has not yet spawned. Entry is locked

	// fixed archangel spawnloc
	private final static int ANGEL_LOCATION[][] =
	{
		{
			114239,
			17168,
			10080,
			63544
		},
		{
			115780,
			15564,
			10080,
			13620
		},
		{
			114880,
			16236,
			10080,
			5400
		},
		{
			115168,
			17200,
			10080,
			0
		},
		{
			115792,
			16608,
			10080,
			0
		},
	};

	private long _LastAttackVsBaiumTime = 0;
	protected final List<L2Npc> _Minions = new ArrayList<>(5);
	private L2BossZone _Zone;

	public Baium(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);

		final int[] mob =
		{
			LIVE_BAIUM
		};
		this.registerMobs(mob);

		// Quest NPC starter initialization
		addStartNpc(STONE_BAIUM);
		addStartNpc(ANGELIC_VORTEX);
		addTalkId(STONE_BAIUM);
		addTalkId(ANGELIC_VORTEX);
		_Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
		final int status = GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM);
		if (status == DEAD)
		{
			// load the unlock date and time for baium from DB
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if (temp > 0)
				// the unlock time has not yet expired. Mark Baium as currently locked (dead). Setup a timer
				// to fire at the correct time (calculate the time between now and the unlock time,
				// setup a timer to fire after that many msec)
				startQuestTimer("baium_unlock", temp, null, null);
			else
			{
				// the time has already expired while the server was offline. Delete the saved time and
				// immediately spawn the stone-baium. Also the state need not be changed from ASLEEP
				addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			}
		}
		else if (status == AWAKE)
		{
			final int loc_x = info.getInt("loc_x");
			final int loc_y = info.getInt("loc_y");
			final int loc_z = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final int hp = info.getInt("currentHP");
			final int mp = info.getInt("currentMP");
			final L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM, loc_x, loc_y, loc_z, heading, false, 0);
			GrandBossManager.getInstance().addBoss(baium);
			final L2Npc _baium = baium;
			ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						_baium.setCurrentHpMp(hp, mp);
						_baium.setIsInvul(true);
						_baium.setIsImmobilized(true);
						_baium.setRunning();
						_baium.broadcastSocialAction(2);
						startQuestTimer("baium_wakeup", 15000, _baium, null);
					}
					catch (final Exception e)
					{
						e.printStackTrace();
					}
				}
			}, 100L);
		}
		else
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		if (event.equalsIgnoreCase("baium_unlock"))
		{
			GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP);
			addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0);
		}
		else if (event.equalsIgnoreCase("skill_range") && npc != null)
			callSkillAI(npc);
		else if (event.equalsIgnoreCase("clean_player"))
			_target = getRandomTarget(npc);
		else if (event.equalsIgnoreCase("baium_wakeup") && npc != null)
		{
			if (npc.getId() == LIVE_BAIUM)
			{
				npc.broadcastSocialAction(1);
				npc.broadcastPacket(new Earthquake(npc.getX(), npc.getY(), npc.getZ(), 40, 5));
				// start monitoring baium's inactivity
				_LastAttackVsBaiumTime = System.currentTimeMillis();
				startQuestTimer("baium_despawn", 60000, npc, null, true);
				startQuestTimer("skill_range", 500, npc, null, true);
				final L2Npc baium = npc;
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							baium.setIsInvul(false);
							baium.setIsImmobilized(false);
							for (final L2Npc minion : _Minions)
								minion.setShowSummonAnimation(false);
						}
						catch (final Exception e)
						{
							_log.log(Level.WARNING, "", e);
						}
					}
				}, 11100L);
				// TODO: the person who woke baium up should be knocked across the room, onto a wall, and
				// lose massive amounts of HP.
				for (final int[] element : ANGEL_LOCATION)
				{
					final L2Npc angel = addSpawn(ARCHANGEL, element[0], element[1], element[2], element[3], false, 0, true);
					angel.setIsInvul(true);
					_Minions.add(angel);
				}
			}
			// despawn the live baium after 30 minutes of inactivity
			// also check if the players are cheating, having pulled Baium outside his zone...
		}
		else if (event.equalsIgnoreCase("baium_despawn") && npc != null)
			if (npc.getId() == LIVE_BAIUM)
			{
				// just in case the zone reference has been lost (somehow...), restore the reference
				if (_Zone == null)
					_Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
				if (_LastAttackVsBaiumTime + 1800000 < System.currentTimeMillis())
				{
					npc.deleteMe(); // despawn the live-baium
					for (final L2Npc minion : _Minions)
						if (minion != null)
						{
							minion.getSpawn().stopRespawn();
							minion.deleteMe();
						}
					_Minions.clear();
					addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0); // spawn stone-baium
					GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, ASLEEP); // mark that Baium is not awake any more
					_Zone.oustAllPlayers();
					cancelQuestTimer("baium_despawn", npc, null);
				}
				else if (_LastAttackVsBaiumTime + 300000 < System.currentTimeMillis() && npc.getCurrentHp() < npc.getMaxHp() * 3 / 4.0)
				{
					npc.setIsCastingNow(false); // just in case
					npc.setTarget(npc);
					npc.doCast(SkillTable.getInstance().getInfo(4135, 1));
					npc.setIsCastingNow(true);
				}
				else if (!_Zone.isInsideZone(npc))
					npc.teleToLocation(116033, 17447, 10104);
			}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();
		String htmltext = "";
		if (_Zone == null)
			_Zone = GrandBossManager.getInstance().getZone(113100, 14500, 10077);
		if (_Zone == null)
			return "<html><body>Angelic Vortex:<br>You may not enter while admin disabled this zone</body></html>";
		if (npcId == STONE_BAIUM && GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP)
		{
			if (_Zone.isPlayerAllowed(player))
			{
				// once Baium is awaken, no more people may enter until he dies, the server reboots, or
				// 30 minutes pass with no attacks made against Baium.
				GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, AWAKE);
				npc.deleteMe();
				final L2GrandBossInstance baium = (L2GrandBossInstance) addSpawn(LIVE_BAIUM, npc, true);
				GrandBossManager.getInstance().addBoss(baium);
				final L2Npc _baium = baium;
				ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							_baium.setIsInvul(true);
							_baium.setRunning();
							_baium.broadcastSocialAction(2);
							startQuestTimer("baium_wakeup", 15000, _baium, null);
							_baium.setShowSummonAnimation(false);
						}
						catch (final Throwable e)
						{
							_log.log(Level.WARNING, "", e);
						}
					}
				}, 100L);
			}
			else
				htmltext = "Conditions are not right to wake up Baium";
		}
		else if (npcId == ANGELIC_VORTEX)
		{
			if (player.isFlying())
				// print "Player "+player.getName()+" attempted to enter Baium's lair while flying!";
				return "<html><body>Angelic Vortex:<br>You may not enter while flying a wyvern</body></html>";

			if (GrandBossManager.getInstance().getBossStatus(LIVE_BAIUM) == ASLEEP && player.getQuestState("baium").getQuestItemsCount(4295) > 0) // bloody fabric
			{
				player.getQuestState("baium").takeItems(4295, 1);
				// allow entry for the player for the next 30 secs (more than enough time for the TP to happen)
				// Note: this just means 30secs to get in, no limits on how long it takes before we get out.
				_Zone.allowPlayerEntry(player, 30);
				player.teleToLocation(113100, 14500, 10077);
			}
			else
				npc.showChatWindow(player, 1);
		}
		return htmltext;
	}

	@Override
	public String onSpellFinished(final L2Npc npc, final L2PcInstance player, final L2Skill skill)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		else if (npc.getId() == LIVE_BAIUM && !npc.isInvul())
			callSkillAI(npc);
		return super.onSpellFinished(npc, player, skill);
	}
	
	@Override
	public String onSpawn(final L2Npc npc)
	{
		npc.disableCoreAI(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onAttack(final L2Npc npc, final L2PcInstance attacker, final int damage, final boolean isPet)
	{
		if (!_Zone.isInsideZone(attacker))
		{
			attacker.reduceCurrentHp(attacker.getCurrentHp(), attacker, false, false, null);
			return super.onAttack(npc, attacker, damage, isPet);
		}
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return super.onAttack(npc, attacker, damage, isPet);
		}
		else if (npc.getId() == LIVE_BAIUM && !npc.isInvul())
		{
			if (attacker.getMountType() == 1)
			{
				int sk_4258 = 0;
				final L2Effect[] effects = attacker.getAllEffects();
				if (effects != null && effects.length != 0)
					for (final L2Effect e : effects)
						if (e.getSkill().getId() == 4258)
							sk_4258 = 1;
				if (sk_4258 == 0)
				{
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getInfo(4258, 1));
				}
			}
			// update a variable with the last action against baium
			_LastAttackVsBaiumTime = System.currentTimeMillis();
			callSkillAI(npc);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		cancelQuestTimer("baium_despawn", npc, null);
		npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		// spawn the "Teleportation Cubic" for 15 minutes (to allow players to exit the lair)
		addSpawn(29055, 115203, 16620, 10078, 0, false, 900000); //// should we teleport everyone out if the cubic despawns??
		// "lock" baium for 5 days and 1 to 8 hours [i.e. 432,000,000 + 1*3,600,000 + random-less-than(8*3,600,000) millisecs]
		final long respawnTime = (long) Config.Interval_Of_Baium_Spawn + getRandom(Config.Random_Of_Baium_Spawn);
		GrandBossManager.getInstance().setBossStatus(LIVE_BAIUM, DEAD);
		startQuestTimer("baium_unlock", respawnTime, null, null);
		// also save the respawn time so that the info is maintained past reboots
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(LIVE_BAIUM);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(LIVE_BAIUM, info);
		for (final L2Npc minion : _Minions)
			if (minion != null)
			{
				minion.getSpawn().stopRespawn();
				minion.deleteMe();
			}
		_Minions.clear();
		if (getQuestTimer("skill_range", npc, null) != null)
			getQuestTimer("skill_range", npc, null).cancel();
		return super.onKill(npc, killer, isPet);
	}

	public L2Character getRandomTarget(final L2Npc npc)
	{
		final FastList<L2Character> result = FastList.newInstance();
		final Collection<L2Object> objs = npc.getKnownList().getKnownObjects().values();
		{
			for (final L2Object obj : objs)
			{
				if (obj instanceof L2Playable || obj instanceof L2DecoyInstance)
				{
					if (obj instanceof L2PcInstance)
						if (((L2PcInstance) obj).getAppearance().getInvisible())
							continue;
						
					if (((L2Character) obj).getZ() < npc.getZ() - 100 && ((L2Character) obj).getZ() > npc.getZ() + 100 || !GeoData.getInstance().canSeeTarget(((L2Character) obj).getX(), ((L2Character) obj).getY(), ((L2Character) obj).getZ(), npc.getX(), npc.getY(), npc.getZ()))
						continue;
				}
				if (obj instanceof L2Playable || obj instanceof L2DecoyInstance)
					if (Util.checkIfInRange(9000, npc, obj, true) && !((L2Character) obj).isDead())
						result.add((L2Character) obj);
			}
		}
		if (result.isEmpty())
			for (final L2Npc minion : _Minions)
				if (minion != null)
					result.add(minion);
				
		if (result.isEmpty())
		{
			FastList.recycle(result);
			return null;
		}

		final Object[] characters = result.toArray();
		final QuestTimer timer = getQuestTimer("clean_player", npc, null);
		if (timer != null)
			timer.cancel();
		startQuestTimer("clean_player", 20000, npc, null);
		final L2Character target = (L2Character) characters[getRandom(characters.length)];
		FastList.recycle(result);
		return target;

	}

	public synchronized void callSkillAI(final L2Npc npc)
	{
		if (npc.isInvul() || npc.isCastingNow())
			return;

		if (_target == null || _target.isDead() || !_Zone.isInsideZone(_target))
		{
			_target = getRandomTarget(npc);
			if (_target != null)
				_skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
		}

		final L2Character target = _target;
		L2Skill skill = _skill;
		if (skill == null)
			skill = SkillTable.getInstance().getInfo(getRandomSkill(npc), 1);
		if (target == null || target.isDead() || !_Zone.isInsideZone(target))
		{
			npc.setIsCastingNow(false);
			return;
		}

		if (Util.checkIfInRange(skill.getCastRange(), npc, target, true))
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			npc.setTarget(target);
			npc.setIsCastingNow(true);
			_target = null;
			_skill = null;
			if (getDist(skill.getCastRange()) > 0)
				npc.broadcastPacket(new MoveToPawn(npc, target, getDist(skill.getCastRange())));
			try
			{
				Thread.sleep(1000);
				npc.stopMove(null);
				npc.doCast(skill);
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			npc.getAI().setIntention(AI_INTENTION_FOLLOW, target, null);
			npc.setIsCastingNow(false);
		}
	}

	public int getRandomSkill(final L2Npc npc)
	{
		int skill;
		if (npc.getCurrentHp() > npc.getMaxHp() * 3 / 4.0)
		{
			if (getRandom(100) < 10)
				skill = 4128;
			else if (getRandom(100) < 10)
				skill = 4129;
			else
				skill = 4127;
		}
		else if (npc.getCurrentHp() > npc.getMaxHp() * 2 / 4.0)
		{
			if (getRandom(100) < 10)
				skill = 4131;
			else if (getRandom(100) < 10)
				skill = 4128;
			else if (getRandom(100) < 10)
				skill = 4129;
			else
				skill = 4127;
		}
		else if (npc.getCurrentHp() > npc.getMaxHp() * 1 / 4.0)
		{
			if (getRandom(100) < 10)
				skill = 4130;
			else if (getRandom(100) < 10)
				skill = 4131;
			else if (getRandom(100) < 10)
				skill = 4128;
			else if (getRandom(100) < 10)
				skill = 4129;
			else
				skill = 4127;
		}
		else if (getRandom(100) < 10)
			skill = 4130;
		else if (getRandom(100) < 10)
			skill = 4131;
		else if (getRandom(100) < 10)
			skill = 4128;
		else if (getRandom(100) < 10)
			skill = 4129;
		else
			skill = 4127;
		return skill;
	}

	@Override
	public String onSkillSee(final L2Npc npc, final L2PcInstance caster, final L2Skill skill, final L2Object[] targets, final boolean isPet)
	{
		if (npc.isInvul())
		{
			npc.getAI().setIntention(AI_INTENTION_IDLE);
			return null;
		}
		npc.setTarget(caster);
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}

	public int getDist(final int range)
	{
		int dist = 0;
		switch (range)
		{
			case -1:
				break;
			case 100:
				dist = 85;
				break;
			default:
				dist = range - 85;
				break;
		}
		return dist;
	}

	public static void main(final String[] args)
	{
		// Quest class and state definition
		new Baium(-1, "baium", "ai");
	}
}
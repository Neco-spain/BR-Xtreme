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
package ai.group_template;

import ai.engines.L2AttackableAIScript;
import ct25.xtreme.gameserver.ai.CtrlIntention;
import ct25.xtreme.gameserver.model.actor.L2Attackable;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.network.serverpackets.NpcSay;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;
import javolution.util.FastList;
import javolution.util.FastMap;

public class SummonMinions extends L2AttackableAIScript
{
	private static int HasSpawned;
	private static TIntHashSet myTrackingSet = new TIntHashSet(); // Used to track instances of npcs
	private final FastMap<Integer, FastList<L2PcInstance>> _attackersList = new FastMap<Integer, FastList<L2PcInstance>>().shared();
	private static final TIntObjectHashMap<int[]> MINIONS = new TIntObjectHashMap<>();

	static
	{
		MINIONS.put(20767, new int[]
		{
			20768,
			20769,
			20770
		}); // Timak Orc Troop
		MINIONS.put(21524, new int[]
		{
			21525
		}); // Blade of Splendor
		MINIONS.put(21531, new int[]
		{
			21658
		}); // Punishment of Splendor
		MINIONS.put(21539, new int[]
		{
			21540
		}); // Wailing of Splendor
		MINIONS.put(22257, new int[]
		{
			18364,
			18364
		}); // Island Guardian
		MINIONS.put(22258, new int[]
		{
			18364,
			18364
		}); // White Sand Mirage
		MINIONS.put(22259, new int[]
		{
			18364,
			18364
		}); // Muddy Coral
		MINIONS.put(22260, new int[]
		{
			18364,
			18364
		}); // Kleopora
		MINIONS.put(22261, new int[]
		{
			18365,
			18365
		}); // Seychelles
		MINIONS.put(22262, new int[]
		{
			18365,
			18365
		}); // Naiad
		MINIONS.put(22263, new int[]
		{
			18365,
			18365
		}); // Sonneratia
		MINIONS.put(22264, new int[]
		{
			18366,
			18366
		}); // Castalia
		MINIONS.put(22265, new int[]
		{
			18366,
			18366
		}); // Chrysocolla
		MINIONS.put(22266, new int[]
		{
			18366,
			18366
		}); // Pythia
		MINIONS.put(22774, new int[]
		{
			22768,
			22768
		}); // Tanta Lizardman Summoner
	}
	
	public SummonMinions(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		final int[] temp =
		{
			20767,
			21524,
			21531,
			21539,
			22257,
			22258,
			22259,
			22260,
			22261,
			22262,
			22263,
			22264,
			22265,
			22266,
			22774
		};
		this.registerMobs(temp, QuestEventType.ON_ATTACK, QuestEventType.ON_KILL);
	}

	@Override
	public String onAttack(final L2Npc npc, L2PcInstance attacker, final int damage, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int npcObjId = npc.getObjectId();
		if (MINIONS.containsKey(npcId))
		{
			if (!myTrackingSet.contains(npcObjId)) // this allows to handle multiple instances of npc
			{
				synchronized (myTrackingSet)
				{
					myTrackingSet.add(npcObjId);
				}

				HasSpawned = npcObjId;
			}
			if (HasSpawned == npcObjId)
				switch (npcId)
				{
					case 22030: // mobs that summon minions only on certain hp
					case 22032:
					case 22038:
					{
						if (npc.getCurrentHp() < npc.getMaxHp() / 2.0)
						{
							HasSpawned = 0;
							if (getRandom(100) < 33) // mobs that summon minions only on certain chance
							{
								int[] minions = MINIONS.get(npcId);
								for (final int val : minions)
								{
									final L2Attackable newNpc = (L2Attackable) Quest.addSpawn(val, npc.getX() + getRandom(-150, 150), npc.getY() + getRandom(-150, 150), npc.getZ(), 0, false, 0);
									newNpc.setRunning();
									newNpc.addDamageHate(attacker, 0, 999);
									newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
								}
								minions = null;
							}
						}
						break;
					}
					case 22257:
					case 22258:
					case 22259:
					case 22260:
					case 22261:
					case 22262:
					case 22263:
					case 22264:
					case 22265:
					case 22266:
					{
						if (isPet)
							attacker = attacker.getPet().getOwner();
						if (attacker.getParty() != null)
						{
							for (final L2PcInstance member : attacker.getParty().getPartyMembers())
								if (_attackersList.get(npcObjId) == null)
								{
									final FastList<L2PcInstance> player = new FastList<>();
									player.add(member);
									_attackersList.put(npcObjId, player);
								}
								else if (!_attackersList.get(npcObjId).contains(member))
									_attackersList.get(npcObjId).add(member);
						}
						else if (_attackersList.get(npcObjId) == null)
						{
							final FastList<L2PcInstance> player = new FastList<>();
							player.add(attacker);
							_attackersList.put(npcObjId, player);
						}
						else if (!_attackersList.get(npcObjId).contains(attacker))
							_attackersList.get(npcObjId).add(attacker);
						if (attacker.getParty() != null && attacker.getParty().getMemberCount() > 2 || _attackersList.get(npcObjId).size() > 2) // Just to make sure..
						{
							HasSpawned = 0;
							for (final int val : MINIONS.get(npcId))
							{
								final L2Attackable newNpc = (L2Attackable) Quest.addSpawn(val, npc.getX() + getRandom(-150, 150), npc.getY() + getRandom(-150, 150), npc.getZ(), 0, false, 0);
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
							}
						}
						break;
					}
					default: // mobs without special conditions
					{
						HasSpawned = 0;
						if (npcId != 20767)
							for (final int val : MINIONS.get(npcId))
							{
								final L2Attackable newNpc = (L2Attackable) Quest.addSpawn(val, npc.getX() + getRandom(-150, 150), npc.getY() + getRandom(-150, 150), npc.getZ(), 0, false, 0);
								newNpc.setRunning();
								newNpc.addDamageHate(attacker, 0, 999);
								newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
							}
						else
							for (final int val : MINIONS.get(npcId))
								Quest.addSpawn(val, npc.getX() + getRandom(-100, 100), npc.getY() + getRandom(-100, 100), npc.getZ(), 0, false, 0);
						if (npcId == 20767)
							npc.broadcastPacket(new NpcSay(npcObjId, 0, npcId, 1000294)); // Come out, you children of darkness!
						break;
					}
				}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final int npcId = npc.getId();
		final int npcObjId = npc.getObjectId();
		if (MINIONS.containsKey(npcId))
			synchronized (myTrackingSet)
			{
				myTrackingSet.remove(npcObjId);
			}
		if (_attackersList.get(npcObjId) != null)
			_attackersList.get(npcObjId).clear();
		return super.onKill(npc, killer, isPet);
	}

	public static void main(final String[] args)
	{
		// now call the constructor (starts up the ai)
		new SummonMinions(-1, SummonMinions.class.getSimpleName(), "ai/group_template");
	}
}
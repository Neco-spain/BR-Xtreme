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

import static ct23.xtreme.gameserver.ai.CtrlIntention.AI_INTENTION_ATTACK;
import ct23.xtreme.gameserver.ai.CtrlEvent;
import ct23.xtreme.gameserver.ai.CtrlIntention;
import ct23.xtreme.gameserver.datatables.NpcTable;
import ct23.xtreme.gameserver.instancemanager.DimensionalRiftManager;
import ct23.xtreme.gameserver.model.L2Object;
import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.model.actor.L2Attackable;
import ct23.xtreme.gameserver.model.actor.L2Character;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.L2Playable;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.actor.instance.L2RiftInvaderInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.jython.QuestJython;
import ct23.xtreme.gameserver.templates.chars.L2NpcTemplate;

/**
 * 
 * Overarching Superclass for all mob AI
 * @author Fulminus
 *
 */
public class L2AttackableAIScript extends QuestJython
{
	
	/**
	 * This is used to register all monsters contained in mobs for a particular script
	 * @param mobs
	 */
	public void registerMobs (int[] mobs)
	{
		for (int id : mobs)
		{
			this.addEventId(id, Quest.QuestEventType.ON_ATTACK);
			this.addEventId(id, Quest.QuestEventType.ON_KILL);
			this.addEventId(id, Quest.QuestEventType.ON_SPAWN);
			this.addEventId(id, Quest.QuestEventType.ON_SPELL_FINISHED);
			this.addEventId(id, Quest.QuestEventType.ON_SKILL_SEE);
			this.addEventId(id, Quest.QuestEventType.ON_FACTION_CALL);
			this.addEventId(id, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);			
		}
	}
	
	/**
	 * This is used simply for convenience of replacing
	 * jython 'element in list' boolean method.
	 */
	public static <T> boolean contains(T[] array, T obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}

	public static boolean contains(int[] array, int obj)
	{
		for (int i = 0; i < array.length; i++)
		{
			if (array[i] == obj)
			{
				return true;
			}
		}
		return false;
	}
	
	public L2AttackableAIScript (int questId, String name, String descr)
	{
		super(questId, name, descr);
	}
	
	@Override
	public String onAdvEvent (String event, L2Npc npc, L2PcInstance player)
	{
		return null;
	}
	
	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	@Override
	public String onSkillSee (L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet) 
    { 
    	if (caster == null) 
    	{
    		return null;
    	}
    	if (!(npc instanceof L2Attackable))
    	{
    		return null;
    	}
    	
    	L2Attackable attackable = (L2Attackable)npc; 
    	
    	int skillAggroPoints = skill.getAggroPoints();
    	
    	if (caster.getPet() != null)
    	{
    		if (targets.length == 1 && contains(targets, caster.getPet()))
    			skillAggroPoints = 0;
    	}
    	
		if (skillAggroPoints > 0)
		{
			if ( attackable.hasAI() && (attackable.getAI().getIntention() == AI_INTENTION_ATTACK))
			{
				L2Object npcTarget = attackable.getTarget();
				for (L2Object skillTarget : targets)
				{
					if (npcTarget == skillTarget || npc == skillTarget)
					{
						L2Character originalCaster = isPet? caster.getPet(): caster;
						attackable.addDamageHate(originalCaster, 0, (skillAggroPoints*150)/(attackable.getLevel()+7));
					}
				}
			}
		}
    	
    	return null;
    }
    
    @Override
	public String onFactionCall (L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet) 
    { 
    	L2Character originalAttackTarget = (isPet? attacker.getPet(): attacker);
		if ( attacker.isInParty()
				&& attacker.getParty().isInDimensionalRift())
		{
			byte riftType = attacker.getParty().getDimensionalRift().getType();
			byte riftRoom = attacker.getParty().getDimensionalRift().getCurrentRoom();

			if (caller instanceof L2RiftInvaderInstance
					&& !DimensionalRiftManager.getInstance().getRoom(riftType, riftRoom).checkIfInZone(npc.getX(), npc.getY(), npc.getZ()))
			{
				return null;
			}
		}    	
    	
		// By default, when a faction member calls for help, attack the caller's attacker.
    	// Notify the AI with EVT_AGGRESSION
		npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, originalAttackTarget, 1);
    	
    	return null;
    }
    
    @Override
	public String onAggroRangeEnter (L2Npc npc, L2PcInstance player, boolean isPet) 
    { 
    	L2Character target = isPet ? player.getPet() : player;
    	
    	((L2Attackable) npc).addDamageHate(target, 0, 1);

		// Set the intention to the L2Attackable to AI_INTENTION_ACTIVE
		if (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
			npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    	return null; 
    }

    @Override
	public String onSpawn (L2Npc npc) 
    { 
    	return null; 
    }
    
    @Override
	public String onAttack (L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
    {	
    	if ((attacker != null) && (npc instanceof L2Attackable))
    	{
	    	L2Attackable attackable = (L2Attackable)npc; 
	
	    	L2Character originalAttacker = isPet? attacker.getPet(): attacker;
	    	attackable.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, originalAttacker);
	    	attackable.addDamageHate(originalAttacker, damage, (damage*100)/(attackable.getLevel()+7));
    	}
    	return null;
    }

    @Override
	public String onKill (L2Npc npc, L2PcInstance killer, boolean isPet) 
    { 
    	return null; 
    }
	
	/**
	 * Monster is running and attacking the playable.
	 * @param npc
	 * @param playable
	 */
	protected void attackPlayer(L2Attackable npc, L2Playable playable)
	{
		npc.setIsRunning(true);
		npc.addDamageHate(playable, 0, 999);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, playable);
	}
    public static void main(String[] args)
    {
    	L2AttackableAIScript ai = new L2AttackableAIScript(-1,"L2AttackableAIScript","L2AttackableAIScript");
		// register all mobs here...
		for (int level =1; level<100; level++)
		{
			L2NpcTemplate[] templates = NpcTable.getInstance().getAllOfLevel(level);
			if ((templates != null) && (templates.length > 0))
			{
				for (L2NpcTemplate t: templates)
				{
					try
					{
						if ( L2Attackable.class.isAssignableFrom(Class.forName("ct23.xtreme.gameserver.model.actor.instance."+t.type+"Instance")))
						{
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_ATTACK);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_KILL);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_SPAWN);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_SKILL_SEE);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_FACTION_CALL);
							ai.addEventId(t.npcId, Quest.QuestEventType.ON_AGGRO_RANGE_ENTER);
						}
					}
					catch(ClassNotFoundException ex)
					{
						System.out.println("Class not found "+t.type+"Instance");
					}
				}
			}
		}
    }
}
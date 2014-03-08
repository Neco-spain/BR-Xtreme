/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import ct26.xtreme.gameserver.ai.CtrlIntention;
import ct26.xtreme.gameserver.datatables.ManorData;
import ct26.xtreme.gameserver.enums.QuestSound;
import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.actor.L2Character;
import ct26.xtreme.gameserver.model.actor.instance.L2MonsterInstance;
import ct26.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.skills.BuffInfo;
import ct26.xtreme.gameserver.network.SystemMessageId;
import ct26.xtreme.gameserver.network.serverpackets.SystemMessage;
import ct26.xtreme.util.Rnd;

/**
 * Sow effect implementation.
 * @author Adry_85, l3x
 */
public final class Sow extends AbstractEffect
{
	public Sow(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (!info.getEffector().isPlayer() || !info.getEffected().isMonster())
		{
			return;
		}
		
		final L2PcInstance player = info.getEffector().getActingPlayer();
		final L2MonsterInstance target = (L2MonsterInstance) info.getEffected();
		
		if (target.isDead() || (!target.getTemplate().canBeSown()) || target.isSeeded() || (target.getSeederId() != player.getObjectId()))
		{
			return;
		}
		
		final int seedId = target.getSeedType();
		if (seedId == 0)
		{
			return;
		}
		
		// Consuming used seed
		if (!player.destroyItemByItemId("Consume", seedId, 1, target, false))
		{
			return;
		}
		
		final SystemMessage sm;
		if (calcSuccess(player, target, seedId))
		{
			player.sendPacket(QuestSound.ITEMSOUND_QUEST_ITEMGET.getPacket());
			target.setSeeded(player.getActingPlayer());
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN);
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.THE_SEED_WAS_NOT_SOWN);
		}
		
		if (player.getParty() == null)
		{
			player.sendPacket(sm);
		}
		else
		{
			player.getParty().broadcastPacket(sm);
		}
		
		// TODO: Mob should not aggro on player, this way doesn't work really nice
		target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}
	
	private static boolean calcSuccess(L2Character activeChar, L2Character target, int seedId)
	{
		// TODO: check all the chances
		int basicSuccess = (ManorData.getInstance().isAlternative(seedId) ? 20 : 90);
		final int minlevelSeed = ManorData.getInstance().getSeedMinLevel(seedId);
		final int maxlevelSeed = ManorData.getInstance().getSeedMaxLevel(seedId);
		final int levelPlayer = activeChar.getLevel(); // Attacker Level
		final int levelTarget = target.getLevel(); // target Level
		
		// seed level
		if (levelTarget < minlevelSeed)
		{
			basicSuccess -= 5 * (minlevelSeed - levelTarget);
		}
		if (levelTarget > maxlevelSeed)
		{
			basicSuccess -= 5 * (levelTarget - maxlevelSeed);
		}
		
		// 5% decrease in chance if player level
		// is more than +/- 5 levels to _target's_ level
		int diff = (levelPlayer - levelTarget);
		if (diff < 0)
		{
			diff = -diff;
		}
		if (diff > 5)
		{
			basicSuccess -= 5 * (diff - 5);
		}
		
		// chance can't be less than 1%
		Math.max(basicSuccess, 1);
		return Rnd.nextInt(99) < basicSuccess;
	}
}

package handlers.effecthandlers;

import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;

/**
 * Change Fishing Mastery dummy effect implementation.
 * @author Zoey76
 */
public final class ChangeFishingMastery extends AbstractEffect
{
	public ChangeFishingMastery(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
}

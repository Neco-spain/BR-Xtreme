package handlers.effecthandlers;

import ct26.xtreme.gameserver.model.StatsSet;
import ct26.xtreme.gameserver.model.conditions.Condition;
import ct26.xtreme.gameserver.model.effects.AbstractEffect;
import ct26.xtreme.gameserver.model.effects.L2EffectType;
import ct26.xtreme.gameserver.model.skills.BuffInfo;

/**
 * Cubic Mastery effect implementation.
 * @author Zoey76
 */
public final class CubicMastery extends AbstractEffect
{
	public CubicMastery(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return (info.getEffector() != null) && (info.getEffected() != null) && info.getEffected().isPlayer();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CUBIC_MASTERY;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		return info.getSkill().isPassive();
	}
}

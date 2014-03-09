package ct23.xtreme.gameserver.skills.conditions;

import ct23.xtreme.gameserver.model.L2Skill;
import ct23.xtreme.gameserver.skills.Env;

/**
 * 
 * @author  DrHouse
 */
public class ConditionPlayerActiveSkillId extends Condition
{
	private final int _skillId;
	private final int _skillLevel;
    
    public ConditionPlayerActiveSkillId(int skillId)
    {
        _skillId = skillId;
        _skillLevel = -1;
    }
    
    public ConditionPlayerActiveSkillId(int skillId, int skillLevel)
    {
        _skillId = skillId;
        _skillLevel = skillLevel;
    }
    
    @Override
    public boolean testImpl(Env env)
    {
        for (L2Skill sk : env.player.getAllSkills())
        {
            if (sk != null)
            {
                if (sk.getId() == _skillId)
                {
                	if (_skillLevel == -1 || _skillLevel <= sk.getLevel())
                		return true;
                }
            }
        }
        return false;
    }
}
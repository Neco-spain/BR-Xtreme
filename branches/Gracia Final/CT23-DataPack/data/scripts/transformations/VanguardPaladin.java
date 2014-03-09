package transformations;

import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.instancemanager.TransformationManager;
import ct23.xtreme.gameserver.model.L2Transformation;

public class VanguardPaladin extends L2Transformation
{
	public VanguardPaladin()
	{
		// id
		super(312);
	}

	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 312 || getPlayer().isCursedWeaponEquipped())
			return;

		transformedSkills();
	}

	public void transformedSkills()
	{
		if (getPlayer().getLevel() > 43)
		{
			// Power Divide
			getPlayer().addSkill(SkillTable.getInstance().getInfo(816, getPlayer().getLevel() - 43), false);
			// Full Swing
			getPlayer().addSkill(SkillTable.getInstance().getInfo(814, getPlayer().getLevel() - 43), false);
			// Two handed mastery
			getPlayer().addSkill(SkillTable.getInstance().getInfo(293, getPlayer().getLevel() - 43), false);
			getPlayer().setTransformAllowedSkills(new int[]{838,5491,816,814,293,28,18,406,400,196,197});
		}
		else
			getPlayer().setTransformAllowedSkills(new int[]{838,5491,28,18,406,400,196,197});
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false); 
		// Switch Stance
		getPlayer().addSkill(SkillTable.getInstance().getInfo(838, 1), false);
	}

	public void onUntransform()
	{
		removeSkills();
	}

	public void removeSkills()
	{
		// Power Divide
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(816, getPlayer().getLevel() - 43), false);
		// Full Swing
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(814, getPlayer().getLevel() - 43), false);
		// Two handed mastery
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(293, getPlayer().getLevel() - 43), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false); 
		// Switch Stance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(838, 1), false);

		getPlayer().setTransformAllowedSkills(new int[]{});
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardPaladin());
	}
}

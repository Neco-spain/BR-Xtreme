package transformations;

import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.instancemanager.TransformationManager;
import ct23.xtreme.gameserver.model.L2Transformation;

public class VanguardDarkAvenger extends L2Transformation
{
	public VanguardDarkAvenger()
	{
		// id
		super(313);
	}

	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 313 || getPlayer().isCursedWeaponEquipped())
			return;

		transformedSkills();
	}

	public void transformedSkills()
	{
		if (getPlayer().getLevel() > 43)
		{
			// Double Strike
			getPlayer().addSkill(SkillTable.getInstance().getInfo(817, getPlayer().getLevel() - 43), false);
			// Blade Hurricane
			getPlayer().addSkill(SkillTable.getInstance().getInfo(815, getPlayer().getLevel() - 43), false);
			// Dual Weapon Mastery
			getPlayer().addSkill(SkillTable.getInstance().getInfo(144, getPlayer().getLevel() - 43), false);
			getPlayer().setTransformAllowedSkills(new int[]{838,5491,817,815,144,28,18,283,65,401,86});
		}
		else
			getPlayer().setTransformAllowedSkills(new int[]{838,5491,28,18,283,65,401,86});
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
		// Double Strike
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(817, getPlayer().getLevel() - 43), false);
		// Blade Hurricane
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(815, getPlayer().getLevel() - 43), false);
		// Dual Weapon Mastery
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(144, getPlayer().getLevel() - 43), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false); 
		// Switch Stance
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(838, 1), false);

		getPlayer().setTransformAllowedSkills(new int[]{});
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new VanguardDarkAvenger());
	}
}
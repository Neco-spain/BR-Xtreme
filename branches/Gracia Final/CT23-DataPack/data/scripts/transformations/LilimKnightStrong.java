package transformations;

import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.instancemanager.TransformationManager;
import ct23.xtreme.gameserver.model.L2Transformation;

public class LilimKnightStrong extends L2Transformation
{
	public LilimKnightStrong()
	{
		// id, colRadius, colHeight
		super(207, 12, 25.5);
	}

	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 207 || getPlayer().isCursedWeaponEquipped())
			return;

		transformedSkills();
	}

	public void transformedSkills()
	{
		// Attack Buster (up to 4 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(568, 4), false);
		// Attack Storm (up to 4 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(569, 4), false);
		// Attack Rage (up to 4 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(570, 4), false);
		// Poison Dust (up to 4 levels)
		getPlayer().addSkill(SkillTable.getInstance().getInfo(571, 4), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(new int[]{568,569,570,571,5491,619});
	}

	public void onUntransform()
	{
		removeSkills();
	}

	public void removeSkills()
	{
		// Attack Buster (up to 4 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(568, 4), false);
		// Attack Storm (up to 4 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(569, 4), false);
		// Attack Rage (up to 4 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(570, 4), false);
		// Poison Dust (up to 4 levels)
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(571, 4), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(new int[]{});
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new LilimKnightStrong());
	}
}
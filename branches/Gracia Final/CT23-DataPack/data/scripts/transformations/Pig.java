package transformations;

import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.instancemanager.TransformationManager;
import ct23.xtreme.gameserver.model.L2Transformation;

public class Pig extends L2Transformation
{
	public Pig()
	{
		// id, colRadius, colHeight
		super(104, 15, 18.3);
	}

	public void onTransform()
	{
		if (getPlayer().getTransformationId() != 104 || getPlayer().isCursedWeaponEquipped())
			return;

		transformedSkills();
	}

	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(new int[]{5491,619});
	}

	public void onUntransform()
	{
		removeSkills();
	}

	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);

		getPlayer().setTransformAllowedSkills(new int[]{});
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Pig());
	}
}
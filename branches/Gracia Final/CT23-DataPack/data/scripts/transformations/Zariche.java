package transformations;

import ct23.xtreme.gameserver.model.L2Transformation;
import ct23.xtreme.gameserver.instancemanager.TransformationManager;

public class Zariche extends L2Transformation
{
	public Zariche()
	{
		// TODO: Unhardcode Akamanah and Zariche transformations as much as we can
		// id, colRadius, colHeight
		super(301, 12, 31.58);
	}

	public void onTransform()
	{
		// Set charachter name to transformed name
		getPlayer().getAppearance().setVisibleName("Zariche");
		getPlayer().getAppearance().setVisibleTitle("");
	}

	public void onUntransform()
	{
	// set character back to true name.
		getPlayer().getAppearance().setVisibleName(null);
		getPlayer().getAppearance().setVisibleTitle(null);
	}

	public static void main(String[] args)
	{
		TransformationManager.getInstance().registerTransformation(new Zariche());
	}
}

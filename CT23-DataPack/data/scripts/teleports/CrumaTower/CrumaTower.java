package teleports.CrumaTower;

import ct23.xtreme.gameserver.model.Location;
import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;

public class CrumaTower extends Quest
{
	// NPC
	private static final int MOZELLA = 30483;
	// Locations
	private static final Location TELEPORT_LOC1 = new Location(17776, 113968, -11671);
	private static final Location TELEPORT_LOC2 = new Location(17680, 113968, -11671);
	// Misc
	private static final int MAX_LEVEL = 55;
	
	public CrumaTower()
	{
		super(-1,"Cruma Tower", "teleports");
		addFirstTalkId(MOZELLA);
		addStartNpc(MOZELLA);
		addTalkId(MOZELLA);
	}
	
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if (player.getLevel() <= MAX_LEVEL)
		{
			player.teleToLocation(getRandomBoolean() ? TELEPORT_LOC1 : TELEPORT_LOC2, false);
			return null;
		}
		return "30483-1.htm";
	}

	public static void main(String[] args)
	{
		new CrumaTower();
	}
}
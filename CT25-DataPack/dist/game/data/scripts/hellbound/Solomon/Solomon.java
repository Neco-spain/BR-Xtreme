package hellbound.Solomon;

import ct25.xtreme.gameserver.instancemanager.HellboundManager;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;

public class Solomon extends Quest
{
	private static final int SOLOMON = 32355;

	@Override
	public final String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		if (HellboundManager.getInstance().getLevel() == 5)
			return "32355-01.htm";
		else if (HellboundManager.getInstance().getLevel() > 5)
			return "32355-01a.htm";
			
		return null;
	}

	public Solomon(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addFirstTalkId(SOLOMON);
	}

	public static void main(String[] args)
	{
		new Solomon(-1, Solomon.class.getSimpleName(), "hellbound");
	}
}

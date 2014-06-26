package custom.NonTalkingNpcs;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.network.serverpackets.ActionFailed;

public class NonTalkingNpcs extends Quest
{	
	public NonTalkingNpcs()
	{
		super(-1, NonTalkingNpcs.class.getSimpleName(), "custom");
		addFirstTalkId(18684,18685,18686,18687,18688,18689,18690,19691,18692,31557,31606,31671,31672,31673,31674,32026,32030,32031,32032,32619,32620,32621);
	}
	@Override
	public String onFirstTalk (L2Npc npc,L2PcInstance player)
	{
        player.sendPacket(ActionFailed.STATIC_PACKET);
        return null;
	}

	public static void main(String[] args)
	{
		new NonTalkingNpcs();
	}
}


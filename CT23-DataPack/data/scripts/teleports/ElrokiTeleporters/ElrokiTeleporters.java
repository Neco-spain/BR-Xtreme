package teleports.ElrokiTeleporters;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;

public class ElrokiTeleporters extends Quest
{
	private final static int Orahochin = 32111;
	private final static int Gariachin = 32112;
	
	public ElrokiTeleporters(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(Orahochin,Gariachin);
		addTalkId(Orahochin,Gariachin);
	}
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = "";
		QuestState st = player.getQuestState(getName());
		int npcId = npc.getNpcId();
		
	    if (npcId == Orahochin)
	    {
	        if (player.isInCombat())
	        	htmltext = "32111-no.htm";
	        player.teleToLocation(4990,-1879,-3178);
	    }
	    else if (npcId == Gariachin)
	        player.teleToLocation(7557,-5513,-3221);
	    
	    st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new ElrokiTeleporters(-1, "ElrokiTeleporters", "teleports");
	}
}
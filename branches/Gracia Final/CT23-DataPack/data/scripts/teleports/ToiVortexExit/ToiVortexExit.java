package teleports.ToiVortexExit;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;

public class ToiVortexExit extends Quest
{
	private final static int NPC= 29055;
	
	public ToiVortexExit(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(NPC);
		addTalkId(NPC);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		int chance = st.getRandom(3);
	    
		if (chance == 0)
		{
			int X = 108784+st.getRandom(100);
			int Y = 16000+st.getRandom(100);
			int Z = -4928;
			player.teleToLocation(X, Y, Z);
		}
		else if (chance == 1)
		{
			int X = 113824+st.getRandom(100);
			int Y = 10448+st.getRandom(100);
			int Z = -5164;
			player.teleToLocation(X, Y, Z);
		}
		else
		{
			int X = 115488+st.getRandom(100);
			int Y = 22096+st.getRandom(100);
			int Z = -5168;
			player.teleToLocation(X,Y,Z);
		}
		return null;	
	}

	public static void main(String[] args)
	{
		new ToiVortexExit(-1, "ToiVortexExit", "teleports");
	}
}
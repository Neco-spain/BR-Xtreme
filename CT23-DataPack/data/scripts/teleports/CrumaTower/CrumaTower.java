package teleports.CrumaTower;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;

public class CrumaTower extends Quest
{
	public CrumaTower(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(30483);
		addTalkId(30483);
	}
	
	@Override
	public String onTalk (L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = "";
		
		if (player.getLevel() > 55)
			htmltext = "30483.htm";
		else
			player.teleToLocation(17724,114004,-11672);
		st.exitQuest(true);
		return htmltext;
	}

	public static void main(String[] args)
	{
		new CrumaTower(-1, "CrumaTower", "teleports");
	}
}
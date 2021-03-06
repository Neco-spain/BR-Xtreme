package custom.Echo;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.util.Util;

public class Echo extends Quest
{
	private static final String qn = "Echo";

	private static final int[] NPCS =
	{
		31042,
		31043
	};
	private static final int ADENA = 57;
	private static final int COST = 200;

	private static final String[][] LIST =
	{
		{
			"4410",
			"4411",
			"01",
			"02",
			"03"
		},
		{
			"4409",
			"4412",
			"04",
			"05",
			"06"
		},
		{
			"4408",
			"4413",
			"07",
			"08",
			"09"
		},
		{
			"4420",
			"4414",
			"10",
			"11",
			"12"
		},
		{
			"4421",
			"4415",
			"13",
			"14",
			"15"
		},
		{
			"4419",
			"4417",
			"16",
			"05",
			"06"
		},
		{
			"4418",
			"4416",
			"17",
			"05",
			"06"
		}
	};

	public Echo(final int id, final String name, final String descr)
	{
		super(id, name, descr);

		for (final int i : NPCS)
		{
			addStartNpc(i);
			addTalkId(i);
			addFirstTalkId(i);
		}
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		String htmltext = "<html><body>?? ?? ??????????? ? ???? ??????, ? ??????? ???????????? ???? NPC, ???? ?? ????????? ??? ??????????? ???????????.</body></html>";
		final QuestState st = player.getQuestState(getName());
		if (st == null || !Util.isDigit(event))
			return htmltext;

		final int npcId = npc.getId();
		final int score = Integer.parseInt(event);
		for (final String[] val : LIST)
		{
			if (score != Integer.parseInt(val[0]))
				continue;

			if (st.getQuestItemsCount(score) == 0)
				htmltext = npcId + "-" + val[4] + ".htm";
			else if (st.getQuestItemsCount(ADENA) < COST)
				htmltext = npcId + "-" + val[3] + ".htm";
			else
			{
				st.takeItems(ADENA, COST);
				st.giveItems(Integer.parseInt(val[1]), 1);
				htmltext = npcId + "-" + val[2] + ".htm";
			}
			break;
		}
		st.exitQuest(true);
		return htmltext;
	}

	@Override
	public String onFirstTalk(final L2Npc npc, final L2PcInstance player)
	{
		final int npcId = npc.getId();

		if (npcId == 31042)
			return "1.htm";

		if (npcId == 31043)
			return "2.htm";
		return null;
	}

	public static void main(final String[] args)
	{
		new Echo(-1, qn, "custom");
	}
}

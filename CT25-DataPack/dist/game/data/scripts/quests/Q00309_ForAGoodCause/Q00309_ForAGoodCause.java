/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00309_ForAGoodCause;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.network.serverpackets.RadarControl;
import ct25.xtreme.gameserver.util.Util;
import quests.Q00239_WontYouJoinUs.Q00239_WontYouJoinUs;
import quests.Q00308_ReedFieldMaintenance.Q00308_ReedFieldMaintenance;

/**
 * For A Good Cause (309)
 * @author nonom, Zoey76, Joxit
 * @version 2011/09/30 based on official server Naia
 */
public class Q00309_ForAGoodCause extends Quest
{
	// NPC
	private static final int ATRA = 32647;
	// Mobs
	private static final int CORRUPTED_MUCROKIAN = 22654;
	private static final Map<Integer, Integer> MUCROKIANS = new HashMap<>();
	static
	{
		MUCROKIANS.put(22650, 218); // Mucrokian Fanatic
		MUCROKIANS.put(22651, 258); // Mucrokian Ascetic
		MUCROKIANS.put(22652, 248); // Mucrokian Savior
		MUCROKIANS.put(22653, 290); // Mucrokian Preacher
		MUCROKIANS.put(22654, 124); // Contaminated Mucrokian
		MUCROKIANS.put(22655, 220); // Awakened Mucrokian
	}

	// Items
	private static final int MUCROKIAN_HIDE = 14873;
	private static final int FALLEN_MUCROKIAN_HIDE = 14874;
	// Rewards
	private static final int REC_DYNASTY_EARRINGS_70 = 9985;
	private static final int REC_DYNASTY_NECKLACE_70 = 9986;
	private static final int REC_DYNASTY_RING_70 = 9987;
	private static final int REC_DYNASTY_SIGIL_60 = 10115;

	private static final int[] MOIRAI_RECIPES =
	{
		15777,
		15780,
		15783,
		15786,
		15789,
		15790,
		15814,
		15813,
		15812
	};

	private static final int[] MOIRAI_PIECES =
	{
		15647,
		15650,
		15653,
		15656,
		15659,
		15692,
		15772,
		15773,
		15774
	};

	// Misc
	private static final int MIN_LEVEL = 82;

	public Q00309_ForAGoodCause(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(ATRA);
		addTalkId(ATRA);
		for (final int id : MUCROKIANS.keySet())
			super.addKillId(id);
	}

	private boolean canGiveItem(final QuestState st, final int quanty)
	{
		final long mucrokian = st.getQuestItemsCount(MUCROKIAN_HIDE);
		final long fallen = st.getQuestItemsCount(FALLEN_MUCROKIAN_HIDE);
		if (fallen > 0)
		{
			if (fallen >= quanty / 2)
			{
				st.takeItems(FALLEN_MUCROKIAN_HIDE, quanty / 2);
				return true;
			}
			else if (mucrokian >= quanty - fallen * 2)
			{
				st.takeItems(FALLEN_MUCROKIAN_HIDE, fallen);
				st.takeItems(MUCROKIAN_HIDE, quanty - fallen * 2);
				return true;
			}
		}
		else if (mucrokian >= quanty)
		{
			st.takeItems(MUCROKIAN_HIDE, quanty);
			return true;
		}
		return false;
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
			return null;

		String htmltext = null;
		switch (event)
		{
			case "32647-02.htm":
			case "32647-03.htm":
			case "32647-04.htm":
			case "32647-08.html":
			case "32647-10.html":
			case "32647-12.html":
			case "32647-13.html":
				htmltext = event;
				break;
			case "32647-05.html":
				st.startQuest();
				player.sendPacket(new RadarControl(0, 2, 77325, 205773, -3432));
				htmltext = event;
				break;
			case "claimreward":
				final QuestState q239 = player.getQuestState(Q00239_WontYouJoinUs.class.getSimpleName());
				htmltext = q239 != null && q239.isCompleted() ? "32647-11.html" : "32647-09.html";
				break;
			case "100":
			case "120":
				htmltext = onItemExchangeRequest(st, MOIRAI_PIECES[getRandom(MOIRAI_PIECES.length - 1)], Integer.parseInt(event));
				break;
			case "192":
			case "230":
				htmltext = onItemExchangeRequest(st, REC_DYNASTY_EARRINGS_70, Integer.parseInt(event));
				break;
			case "256":
			case "308":
				htmltext = onItemExchangeRequest(st, REC_DYNASTY_NECKLACE_70, Integer.parseInt(event));
				break;
			case "128":
			case "154":
				htmltext = onItemExchangeRequest(st, REC_DYNASTY_RING_70, Integer.parseInt(event));
				break;
			case "206":
			case "246":
				htmltext = onItemExchangeRequest(st, REC_DYNASTY_SIGIL_60, Integer.parseInt(event));
				break;
			case "180":
			case "216":
				htmltext = onItemExchangeRequest(st, MOIRAI_RECIPES[getRandom(MOIRAI_RECIPES.length - 1)], Integer.parseInt(event));
				break;
			case "32647-14.html":
			case "32647-07.html":
				st.exitQuest(true, true);
				htmltext = event;
				break;
		}
		return htmltext;
	}

	private String onItemExchangeRequest(final QuestState st, final int item, final int quanty)
	{
		String htmltext;
		if (canGiveItem(st, quanty))
		{
			if (Util.contains(MOIRAI_PIECES, item))
				st.giveItems(item, getRandom(1, 4));
			else
				st.giveItems(item, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			htmltext = "32646-16.htm";
		}
		else
			htmltext = "32646-15.htm";
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(killer, 1);
		if (partyMember != null)
		{
			final QuestState st = partyMember.getQuestState(getName());
			final float chance = MUCROKIANS.get(npc.getId()) * Config.RATE_QUEST_DROP;
			if (getRandom(1000) < chance)
			{
				if (npc.getId() == CORRUPTED_MUCROKIAN)
				{
					st.giveItems(FALLEN_MUCROKIAN_HIDE, 1);
					st.rewardItems(FALLEN_MUCROKIAN_HIDE, 1);
				}
				else
					st.giveItems(MUCROKIAN_HIDE, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		final QuestState st = talker.getQuestState(getName());
		if (st == null)
			return htmltext;

		final QuestState q308 = talker.getQuestState(Q00308_ReedFieldMaintenance.class.getSimpleName());
		if (q308 != null && q308.isStarted())
			htmltext = "32647-17.html";
		else if (st.isStarted())
			htmltext = st.hasQuestItems(MUCROKIAN_HIDE) || st.hasQuestItems(FALLEN_MUCROKIAN_HIDE) ? "32647-08.html" : "32647-06.html";
		else
			htmltext = talker.getLevel() >= MIN_LEVEL ? "32647-01.htm" : "32647-00.html";
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00309_ForAGoodCause(309, Q00309_ForAGoodCause.class.getSimpleName(), "For A Good Cause");
	}
}

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
package quests.Q00290_ThreatRemoval;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.Config;
import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00251_NoSecrets.Q00251_NoSecrets;

/**
 * Threat Removal (290)
 * @author Adry_85
 */
public class Q00290_ThreatRemoval extends Quest
{
	// NPC
	private static final int PINAPS = 30201;
	// Items
	private static final int ENCHANT_WEAPON_S = 959;
	private static final int ENCHANT_ARMOR_S = 960;
	private static final int FIRE_CRYSTAL = 9552;
	private static final int SEL_MAHUM_ID_TAG = 15714;
	// Misc
	private static final int MIN_LEVEL = 82;

	private static final Map<Integer, Integer> MOBS_TAG = new HashMap<>();

	static
	{
		MOBS_TAG.put(22775, 932); // Sel Mahum Drill Sergeant
		MOBS_TAG.put(22776, 397); // Sel Mahum Training Officer
		MOBS_TAG.put(22777, 932); // Sel Mahum Drill Sergeant
		MOBS_TAG.put(22778, 932); // Sel Mahum Drill Sergeant
		MOBS_TAG.put(22780, 363); // Sel Mahum Recruit
		MOBS_TAG.put(22781, 483); // Sel Mahum Soldier
		MOBS_TAG.put(22782, 363); // Sel Mahum Recruit
		MOBS_TAG.put(22783, 352); // Sel Mahum Soldier
		MOBS_TAG.put(22784, 363); // Sel Mahum Recruit
		MOBS_TAG.put(22785, 169); // Sel Mahum Soldier
	}

	public Q00290_ThreatRemoval(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(PINAPS);
		addTalkId(PINAPS);
		for (final int id : MOBS_TAG.keySet())
			super.addKillId(id);
		registerQuestItems(SEL_MAHUM_ID_TAG);
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
			case "30201-02.html":
			{
				st.startQuest();
				htmltext = event;
				break;
			}
			case "30201-06.html":
			{
				if (st.isCond(1))
				{
					st.takeItems(SEL_MAHUM_ID_TAG, 400);
					switch (getRandom(10))
					{
						case 0:
						{
							st.rewardItems(ENCHANT_WEAPON_S, 1);
							break;
						}
						case 1:
						case 2:
						case 3:
						{
							st.rewardItems(ENCHANT_ARMOR_S, 1);
							break;
						}
						case 4:
						case 5:
						{
							st.rewardItems(ENCHANT_ARMOR_S, 2);
							break;
						}
						case 6:
						{
							st.rewardItems(ENCHANT_ARMOR_S, 3);
							break;
						}
						case 7:
						case 8:
						{
							st.rewardItems(FIRE_CRYSTAL, 1);
							break;
						}
						case 9:
						case 10:
						{
							st.rewardItems(FIRE_CRYSTAL, 2);
							break;
						}
					}
					htmltext = event;
				}
				break;
			}
			case "30201-07.html":
			{
				if (st.isCond(1))
					htmltext = event;
				break;
			}
			case "exit":
			{
				if (st.isCond(1))
					if (st.hasQuestItems(SEL_MAHUM_ID_TAG))
						htmltext = "30201-08.html";
					else
					{
						st.exitQuest(true, true);
						htmltext = "30201-09.html";
					}
				break;
			}
			case "30201-10.html":
			{
				if (st.isCond(1))
				{
					st.exitQuest(true, true);
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance player, final boolean isPet)
	{
		final L2PcInstance partyMember = getRandomPartyMember(player, 1);
		if (partyMember == null)
			return super.onKill(npc, player, isPet);

		final QuestState st = partyMember.getQuestState(getName());
		final int npcId = npc.getId();
		final float chance = MOBS_TAG.get(npcId) * Config.RATE_QUEST_DROP;
		if (getRandom(1000) < chance)
		{
			st.rewardItems(SEL_MAHUM_ID_TAG, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st == null)
			return htmltext;

		switch (st.getState())
		{
			case State.CREATED:
			{
				st = player.getQuestState(Q00251_NoSecrets.class.getSimpleName());
				htmltext = player.getLevel() >= MIN_LEVEL && st != null && st.isCompleted() ? "30201-01.htm" : "30201-03.html";
				break;
			}
			case State.STARTED:
			{
				if (st.isCond(1))
					htmltext = st.getQuestItemsCount(SEL_MAHUM_ID_TAG) < 400 ? "30201-04.html" : "30201-05.html";
				break;
			}
		}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00290_ThreatRemoval(290, Q00290_ThreatRemoval.class.getSimpleName(), "Threat Removal");
	}
}

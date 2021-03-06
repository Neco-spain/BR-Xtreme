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
package quests.Q00159_ProtectTheWaterSource;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;

/**
 * Protect the Water Source (159)
 * @author xban1x
 */
public class Q00159_ProtectTheWaterSource extends Quest
{
	// NPC
	private static final int ASTERIOS = 30154;
	// Monster
	private static final int PLAGUE_ZOMBIE = 27017;
	// Items
	private static final int PLAGUE_DUST = 1035;
	private static final int HYACINTH_CHARM = 1071;
	private static final int HYACINTH_CHARM2 = 1072;
	// Misc
	private static final int MIN_LVL = 12;

	public Q00159_ProtectTheWaterSource(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(ASTERIOS);
		addTalkId(ASTERIOS);
		addKillId(PLAGUE_ZOMBIE);
		registerQuestItems(PLAGUE_DUST, HYACINTH_CHARM, HYACINTH_CHARM2);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equals("30154-04.htm"))
		{
			st.startQuest();
			st.giveItems(HYACINTH_CHARM, 1);
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null)
			switch (st.getCond())
			{
				case 1:
				{
					if (getRandom(100) < 40 && st.hasQuestItems(HYACINTH_CHARM) && !st.hasQuestItems(PLAGUE_DUST))
					{
						st.giveItems(PLAGUE_DUST, 1);
						st.setCond(2, true);
					}
					break;
				}
				case 3:
				{
					long dust = st.getQuestItemsCount(PLAGUE_DUST);
					if (getRandom(100) < 40 && dust < 5 && st.hasQuestItems(HYACINTH_CHARM2))
					{
						st.giveItems(PLAGUE_DUST, 1);
						if (++dust >= 5)
							st.setCond(4, true);
						else
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					}
					break;
				}
			}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st != null)
			switch (st.getState())
			{
				case State.CREATED:
				{
					htmltext = player.getRace() == Race.Elf ? player.getLevel() >= MIN_LVL ? "30154-03.htm" : "30154-02.htm" : "30154-01.htm";
					break;
				}
				case State.STARTED:
				{
					switch (st.getCond())
					{
						case 1:
						{
							if (st.hasQuestItems(HYACINTH_CHARM) && !st.hasQuestItems(PLAGUE_DUST))
								htmltext = "30154-05.html";
							break;
						}
						case 2:
						{
							if (st.hasQuestItems(HYACINTH_CHARM, PLAGUE_DUST))
							{
								st.takeItems(HYACINTH_CHARM, -1);
								st.takeItems(PLAGUE_DUST, -1);
								st.giveItems(HYACINTH_CHARM2, 1);
								st.setCond(3, true);
								htmltext = "30154-06.html";
							}
							break;
						}
						case 3:
						{
							if (st.hasQuestItems(HYACINTH_CHARM2))
								htmltext = "30154-07.html";
							break;
						}
						case 4:
						{
							if (st.hasQuestItems(HYACINTH_CHARM2) && st.getQuestItemsCount(PLAGUE_DUST) >= 5)
							{
								st.giveAdena(18250, true);
								st.exitQuest(false, true);
								htmltext = "30154-08.html";
							}
							break;
						}
					}
					break;
				}
				case State.COMPLETED:
				{
					htmltext = getAlreadyCompletedMsg(player);
					break;
				}
			}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00159_ProtectTheWaterSource(159, Q00159_ProtectTheWaterSource.class.getSimpleName(), "Protect the Water Source");
	}
}

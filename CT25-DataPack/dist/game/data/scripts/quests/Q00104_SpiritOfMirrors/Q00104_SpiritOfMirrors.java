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
package quests.Q00104_SpiritOfMirrors;

import java.util.HashMap;
import java.util.Map;

import ct25.xtreme.gameserver.model.actor.L2Npc;
import ct25.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct25.xtreme.gameserver.model.base.Race;
import ct25.xtreme.gameserver.model.holders.ItemHolder;
import ct25.xtreme.gameserver.model.itemcontainer.Inventory;
import ct25.xtreme.gameserver.model.quest.Quest;
import ct25.xtreme.gameserver.model.quest.QuestState;
import ct25.xtreme.gameserver.model.quest.State;
import quests.Q00281_HeadForTheHills.Q00281_HeadForTheHills;

/**
 * Spirit of Mirrors (104)
 * @author xban1x
 */
public final class Q00104_SpiritOfMirrors extends Quest
{
	// NPCs
	private static final int GALLINT = 30017;
	private static final int ARNOLD = 30041;
	private static final int JOHNSTONE = 30043;
	private static final int KENYOS = 30045;
	// Items
	private static final int GALLINTS_OAK_WAND = 748;
	private static final int SPIRITBOUND_WAND1 = 1135;
	private static final int SPIRITBOUND_WAND2 = 1136;
	private static final int SPIRITBOUND_WAND3 = 1137;
	// Monsters
	private static final Map<Integer, Integer> MONSTERS = new HashMap<>();
	static
	{
		MONSTERS.put(27003, SPIRITBOUND_WAND1); // Spirit Of Mirrors
		MONSTERS.put(27004, SPIRITBOUND_WAND2); // Spirit Of Mirrors
		MONSTERS.put(27005, SPIRITBOUND_WAND3); // Spirit Of Mirrors
	}
	// Rewards
	private static final ItemHolder[] REWARDS =
	{
		new ItemHolder(1060, 100), // Lesser Healing Potion
		new ItemHolder(4412, 10), // Echo Crystal - Theme of Battle
		new ItemHolder(4413, 10), // Echo Crystal - Theme of Love
		new ItemHolder(4414, 10), // Echo Crystal - Theme of Solitude
		new ItemHolder(4415, 10), // Echo Crystal - Theme of Feast
		new ItemHolder(4416, 10), // Echo Crystal - Theme of Celebration
		new ItemHolder(747, 1), // Wand of Adept
	};
	// Misc
	private static final int MIN_LVL = 10;

	private Q00104_SpiritOfMirrors(final int questId, final String name, final String descr)
	{
		super(questId, name, descr);
		addStartNpc(GALLINT);
		addTalkId(ARNOLD, GALLINT, JOHNSTONE, KENYOS);
		for (final int id : MONSTERS.keySet())
			super.addKillId(id);
		registerQuestItems(GALLINTS_OAK_WAND, SPIRITBOUND_WAND1, SPIRITBOUND_WAND2, SPIRITBOUND_WAND3);
	}

	@Override
	public String onAdvEvent(final String event, final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st != null && event.equalsIgnoreCase("30017-04.htm"))
		{
			st.startQuest();
			st.giveItems(GALLINTS_OAK_WAND, 3);
			return event;
		}
		return null;
	}

	@Override
	public String onKill(final L2Npc npc, final L2PcInstance killer, final boolean isPet)
	{
		final QuestState st = killer.getQuestState(getName());
		if (st != null && (st.isCond(1) || st.isCond(2)) && st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == GALLINTS_OAK_WAND && !st.hasQuestItems(MONSTERS.get(npc.getId())))
		{
			st.takeItems(GALLINTS_OAK_WAND, 1);
			st.giveItems(MONSTERS.get(npc.getId()), 1);
			if (st.hasQuestItems(SPIRITBOUND_WAND1, SPIRITBOUND_WAND2, SPIRITBOUND_WAND3))
				st.setCond(3, true);
			else
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(final L2Npc npc, final L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		String htmltext = getNoQuestMsg(player);
		if (st != null)
			switch (npc.getId())
			{
				case GALLINT:
				{
					switch (st.getState())
					{
						case State.CREATED:
						{
							htmltext = player.getRace() == Race.Human ? player.getLevel() >= MIN_LVL ? "30017-03.htm" : "30017-02.htm" : "30017-01.htm";
							break;
						}
						case State.STARTED:
						{
							if (st.isCond(3) && st.hasQuestItems(SPIRITBOUND_WAND1, SPIRITBOUND_WAND2, SPIRITBOUND_WAND3))
							{
								Q00281_HeadForTheHills.giveNewbieReward(player);
								for (final ItemHolder reward : REWARDS)
									st.giveItems(reward);
								st.addExpAndSp(39750, 3407);
								st.giveAdena(16866, true);
								st.exitQuest(false, true);
								htmltext = "30017-06.html";
							}
							else
								htmltext = "30017-05.html";
							break;
						}
						case State.COMPLETED:
						{
							htmltext = getAlreadyCompletedMsg(player);
							break;
						}
					}
					break;
				}
				case ARNOLD:
				case JOHNSTONE:
				case KENYOS:
				{
					if (st.isCond(1))
					{
						if (!st.isSet(npc.getName()))
							st.set(npc.getName(), "1");
						if (st.isSet("Arnold") && st.isSet("Johnstone") && st.isSet("Kenyos"))
							st.setCond(2, true);
					}
					htmltext = npc.getId() + "-01.html";
					break;
				}
			}
		return htmltext;
	}

	public static void main(final String[] args)
	{
		new Q00104_SpiritOfMirrors(104, Q00104_SpiritOfMirrors.class.getSimpleName(), "Spirit of Mirrors");
	}
}
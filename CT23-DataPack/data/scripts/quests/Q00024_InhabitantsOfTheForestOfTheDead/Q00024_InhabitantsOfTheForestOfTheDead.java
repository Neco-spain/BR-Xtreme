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
package quests.Q00024_InhabitantsOfTheForestOfTheDead;

import ct23.xtreme.gameserver.model.actor.L2Npc;
import ct23.xtreme.gameserver.model.actor.instance.L2PcInstance;
import ct23.xtreme.gameserver.model.quest.Quest;
import ct23.xtreme.gameserver.model.quest.QuestState;
import ct23.xtreme.gameserver.model.quest.State;

/**
 * Inhabitants of the Forest of the Dead (24)
 * @author malyelfik
 */
public class Q00024_InhabitantsOfTheForestOfTheDead extends Quest
{
	// NPCs
	private static final int DORIAN = 31389;
	private static final int MYSTERIOUS_WIZARD = 31522;
	private static final int TOMBSTONE = 31531;
	private static final int LIDIA_MAID = 31532;
	// Items
	private static final int LIDIA_LETTER = 7065;
	private static final int LIDIA_HAIRPIN = 7148;
	private static final int SUSPICIOUS_TOTEM_DOLL = 7151;
	private static final int FLOWER_BOUQUET = 7152;
	private static final int SILVER_CROSS_OF_EINHASAD = 7153;
	private static final int BROKEN_SILVER_CROSS_OF_EINHASAD = 7154;
	private static final int TOTEM = 7156;
	// Monsters
	// @formatter:off
	private static final int[] MOBS = { 21557, 21558, 21560, 21563, 21564, 21565, 21566, 21567 };
	// @formatter:on

	public Q00024_InhabitantsOfTheForestOfTheDead()
	{
		super(24, Q00024_InhabitantsOfTheForestOfTheDead.class.getSimpleName(), "Inhabitants of the Forest of the Dead");
		addStartNpc(DORIAN);
		addTalkId(DORIAN, MYSTERIOUS_WIZARD, TOMBSTONE, LIDIA_MAID);
		addKillId(MOBS);
		registerQuestItems(LIDIA_LETTER, LIDIA_HAIRPIN, SUSPICIOUS_TOTEM_DOLL, FLOWER_BOUQUET, SILVER_CROSS_OF_EINHASAD, BROKEN_SILVER_CROSS_OF_EINHASAD);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState st = player.getQuestState(getName());
		if (st == null)
		{
			return getNoQuestMsg();
		}
		
		String htmltext = event;
		switch (event)
		{
		// Dorian
			case "31389-02.htm":
				final QuestState qs = player.getQuestState("23_LidiasHeart");
				if ((player.getLevel() >= 65) && (qs != null) && qs.isCompleted())
				{
					st.startQuest();
					st.giveItems(FLOWER_BOUQUET, 1);
					return "31389-03.htm";
				}
				break;
			case "31389-08.htm":
				st.set("var", "1");
				break;
			case "31389-13.htm":
				st.giveItems(SILVER_CROSS_OF_EINHASAD, 1);
				st.setCond(3, true);
				st.unset("var");
				break;
			case "31389-18.htm":
				st.playSound(QuestSound.INTERFACESOUND_CHARSTAT_OPEN);
				break;
			case "31389-19.htm":
				if (!st.hasQuestItems(BROKEN_SILVER_CROSS_OF_EINHASAD))
				{
					return getNoQuestMsg();
				}
				st.takeItems(BROKEN_SILVER_CROSS_OF_EINHASAD, -1);
				st.setCond(5, true);
				break;
			case "31389-06.htm":
			case "31389-07.htm":
			case "31389-10.htm":
			case "31389-11.htm":
			case "31389-12.htm":
			case "31389-16.htm":
			case "31389-17.htm":
				break;
			// Lidia Maid
			case "31532-04.htm":
				st.giveItems(LIDIA_LETTER, 1);
				st.setCond(6, true);
				break;
			case "31532-07.htm":
				if (st.isCond(8))
				{
					if (!st.hasQuestItems(player, LIDIA_HAIRPIN, LIDIA_LETTER))
					{
						return getNoQuestMsg();
					}
					st.takeItems(LIDIA_HAIRPIN, -1);
					st.takeItems(LIDIA_LETTER, -1);
					st.set("var", "1");
					htmltext = "31532-06.htm";
				}
				else
				{
					if (st.isCond(6))
					{
						st.setCond(7, true);
					}
				}
				break;
			case "31532-10.htm":
				st.set("var", "2");
				break;
			case "31532-14.htm":
				st.set("var", "3");
				break;
			case "31532-19.htm":
				st.unset("var");
				st.setCond(9, true);
				break;
			case "31532-02.htm":
			case "31532-03.htm":
			case "31532-09.htm":
			case "31532-12.htm":
			case "31532-13.htm":
			case "31532-15.htm":
			case "31532-16.htm":
			case "31532-17.htm":
			case "31532-18.htm":
				break;
			// Mysterious Wizard
			case "31522-03.htm":
				if (!st.hasQuestItems(SUSPICIOUS_TOTEM_DOLL))
				{
					return getNoQuestMsg();
				}
				st.takeItems(SUSPICIOUS_TOTEM_DOLL, 1);
				st.set("var", "1");
				break;
			case "31522-08.htm":
				st.unset("var");
				st.setCond(11, true);
				break;
			case "31522-17.htm":
				st.set("var", "1");
				break;
			case "31522-21.htm":
				st.giveItems(TOTEM, 1);
				st.addExpAndSp(242105, 22529); // GoD: Harmony: 6191140 exp and 6118650 sp
				st.exitQuest(false);
				break;
			case "31522-02.htm":
			case "31522-05.htm":
			case "31522-06.htm":
			case "31522-07.htm":
			case "31522-10.htm":
			case "31522-11.htm":
			case "31522-12.htm":
			case "31522-13.htm":
			case "31522-14.htm":
			case "31522-15.htm":
			case "31522-16.htm":
			case "31522-19.htm":
			case "31522-20.htm":
				break;
			// Tombstone
			case "31531-02.htm":
				if (!st.hasQuestItems(FLOWER_BOUQUET))
				{
					return getNoQuestMsg();
				}
				st.takeItems(FLOWER_BOUQUET, -1);
				st.setCond(2, true);
				break;
			default:
				htmltext = null;
				break;
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		final QuestState st = player.getQuestState(getName());
		
		if ((st != null) && st.isCond(9) && (getRandom(100) < 10))
		{
			st.giveItems(SUSPICIOUS_TOTEM_DOLL, 1);
			st.setCond(10, true);
		}
		return super.onKill(npc, player, isPet);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg();
		final QuestState st = player.getQuestState(getName());
		
		if (st == null)
		{
			return htmltext;
		}
		
		switch (npc.getNpcId())
		{
			case DORIAN:
				switch (st.getState())
				{
					case State.CREATED:
						htmltext = "31389-01.htm";
						break;
					case State.STARTED:
						switch (st.getCond())
						{
							case 1:
								htmltext = "31389-04.htm";
								break;
							case 2:
								htmltext = (st.getInt("var") == 0) ? "31389-05.htm" : "31389-09.htm";
								break;
							case 3:
								htmltext = "31389-14.htm";
								break;
							case 4:
								htmltext = "31389-15.htm";
								break;
							case 5:
								htmltext = "31389-20.htm";
								break;
							case 6:
							case 8:
								htmltext = "31389-22.htm";
								break;
							case 7:
								st.giveItems(LIDIA_HAIRPIN, 1);
								st.setCond(8, true);
								htmltext = "31389-21.htm";
								break;
						}
						break;
					case State.COMPLETED:
						htmltext = getAlreadyCompletedMsg();
						break;
				}
				break;
			case MYSTERIOUS_WIZARD:
				if (st.isStarted())
				{
					if (st.isCond(10))
					{
						htmltext = (st.getInt("var") == 0) ? "31522-01.htm" : "31522-04.htm";
					}
					else if (st.isCond(11))
					{
						htmltext = (st.getInt("var") == 0) ? "31522-09.htm" : "31522-18.htm";
					}
				}
				else if (st.isCompleted())
				{
					final QuestState qs = player.getQuestState("25_HidingBehindTheTruth");
					if (!((qs != null) && (qs.isStarted() || qs.isStarted())))
					{
						htmltext = "31522-22.htm";
					}
				}
				break;
			case TOMBSTONE:
				if (st.isStarted())
				{
					if (st.isCond(1))
					{
						st.playSound(QuestSound.AMDSOUND_WIND_LOOT);
						htmltext = "31531-01.htm";
					}
					else if (st.isCond(2))
					{
						htmltext = "31531-03.htm";
					}
				}
				break;
			case LIDIA_MAID:
				if (st.isStarted())
				{
					switch (st.getCond())
					{
						case 5:
							htmltext = "31532-01.htm";
							break;
						case 6:
							htmltext = "31532-05.htm";
							break;
						case 7:
							htmltext = "31532-07a.htm";
							break;
						case 8:
							switch (st.getInt("var"))
							{
								case 0:
									htmltext = "31532-07a.htm";
									break;
								case 1:
									htmltext = "31532-08.htm";
									break;
								case 2:
									htmltext = "31532-11.htm";
									break;
								case 3:
									htmltext = "31532-15.htm";
									break;
							}
							break;
						case 9:
						case 10:
							htmltext = "31532-20.htm";
							break;
					}
				}
				break;
		}
		return htmltext;
	}

	public static void main(String[] args)
	{
		new Q00024_InhabitantsOfTheForestOfTheDead();
	}
}
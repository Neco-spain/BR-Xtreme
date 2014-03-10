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
package ct23.xtreme.gameserver.script;

import ct23.xtreme.gameserver.Announcements;
import ct23.xtreme.gameserver.GameTimeController;
import ct23.xtreme.gameserver.RecipeController;
import ct23.xtreme.gameserver.datatables.CharNameTable;
import ct23.xtreme.gameserver.datatables.CharTemplateTable;
import ct23.xtreme.gameserver.datatables.ClanTable;
import ct23.xtreme.gameserver.datatables.ItemTable;
import ct23.xtreme.gameserver.datatables.LevelUpData;
import ct23.xtreme.gameserver.datatables.MapRegionTable;
import ct23.xtreme.gameserver.datatables.NpcTable;
import ct23.xtreme.gameserver.datatables.SkillTable;
import ct23.xtreme.gameserver.datatables.SkillTreeTable;
import ct23.xtreme.gameserver.datatables.SpawnTable;
import ct23.xtreme.gameserver.datatables.TeleportLocationTable;
import ct23.xtreme.gameserver.idfactory.IdFactory;
import ct23.xtreme.gameserver.model.L2World;

/**
 * @author Luis Arias
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface EngineInterface
{
    //*  keep the references of Singletons to prevent garbage collection
    public CharNameTable charNametable = CharNameTable.getInstance();

    public IdFactory idFactory = IdFactory.getInstance();
    public ItemTable itemTable = ItemTable.getInstance();

    public SkillTable skillTable = SkillTable.getInstance();

    public RecipeController recipeController = RecipeController.getInstance();

    public SkillTreeTable skillTreeTable = SkillTreeTable.getInstance();
    public CharTemplateTable charTemplates = CharTemplateTable.getInstance();
    public ClanTable clanTable = ClanTable.getInstance();

    public NpcTable npcTable = NpcTable.getInstance();

    public TeleportLocationTable teleTable = TeleportLocationTable.getInstance();
    public LevelUpData levelUpData = LevelUpData.getInstance();
    public L2World world = L2World.getInstance();
    public SpawnTable spawnTable = SpawnTable.getInstance();
    public GameTimeController gameTimeController = GameTimeController.getInstance();
    public Announcements announcements = Announcements.getInstance();
    public MapRegionTable mapRegions = MapRegionTable.getInstance();



    //public ArrayList getAllPlayers();
    //public Player getPlayer(String characterName);
    public void addQuestDrop(int npcID, int itemID, int min, int max, int chance, String questID, String[] states);
    public void addEventDrop(int[] items, int[] count, double chance, DateRange range);
    public void onPlayerLogin(String[] message, DateRange range);

}
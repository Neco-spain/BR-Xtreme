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
package ct23.xtreme.gameserver.skills;

import java.io.File;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import ct23.xtreme.gameserver.Item;
import ct23.xtreme.gameserver.skills.conditions.Condition;
import ct23.xtreme.gameserver.templates.StatsSet;
import ct23.xtreme.gameserver.templates.item.L2Armor;
import ct23.xtreme.gameserver.templates.item.L2ArmorType;
import ct23.xtreme.gameserver.templates.item.L2EtcItem;
import ct23.xtreme.gameserver.templates.item.L2EtcItemType;
import ct23.xtreme.gameserver.templates.item.L2Item;
import ct23.xtreme.gameserver.templates.item.L2Weapon;
import ct23.xtreme.gameserver.templates.item.L2WeaponType;

/**
 * @author mkizub
 */
final class DocumentItem extends DocumentBase
{
    private Item _currentItem = null;
    private List<L2Item> _itemsInFile = new FastList<L2Item>();
    private Map<Integer, Item> _itemData = new FastMap<Integer, Item>();

    /**
     * @param armorData
     * @param f
     */
    public DocumentItem(Map<Integer, Item> pItemData, File file)
    {
        super(file);
        _itemData = pItemData;
    }

    /**
     * @param item
     */
    private void setCurrentItem(Item item)
    {
        _currentItem = item;
    }

    @Override
	protected StatsSet getStatsSet()
    {
        return _currentItem.set;
    }

    @Override
	protected String getTableValue(String name)
    {
        return _tables.get(name)[_currentItem.currentLevel];
    }

    @Override
	protected String getTableValue(String name, int idx)
    {
        return _tables.get(name)[idx - 1];
    }

    @Override
	protected void parseDocument(Document doc)
    {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if ("list".equalsIgnoreCase(n.getNodeName()))
            {

                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if ("item".equalsIgnoreCase(d.getNodeName()))
                    {
                        setCurrentItem(new Item());
                        parseItem(d);
                        _itemsInFile.add(_currentItem.item);
                        resetTable();
                    }
                }
            }
            else if ("item".equalsIgnoreCase(n.getNodeName()))
            {
                setCurrentItem(new Item());
                parseItem(n);
                _itemsInFile.add(_currentItem.item);
            }
        }
    }

    protected void parseItem(Node n)
    {
        int itemId = Integer.parseInt(n.getAttributes().getNamedItem("id").getNodeValue());
        String itemName = n.getAttributes().getNamedItem("name").getNodeValue();

        _currentItem.id = itemId;
        _currentItem.name = itemName;

        Item item;
        if ((item = _itemData.get(_currentItem.id)) == null)
        {
        	throw new IllegalStateException("No SQL data for Item ID: "+itemId+" - name: "+itemName);
        }
        _currentItem.set = item.set;
        _currentItem.type = item.type;

        Node first = n.getFirstChild();
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("table".equalsIgnoreCase(n.getNodeName())) parseTable(n);
        }
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("set".equalsIgnoreCase(n.getNodeName()))
                parseBeanSet(n, _itemData.get(_currentItem.id).set, 1);
        }
        for (n = first; n != null; n = n.getNextSibling())
        {
            if ("for".equalsIgnoreCase(n.getNodeName()))
            {
                makeItem();
                parseTemplate(n, _currentItem.item);
            }
        }
        for (n = first; n != null; n = n.getNextSibling())
        {
			if ("cond".equalsIgnoreCase(n.getNodeName()))
			{
				Condition condition = parseCondition(n.getFirstChild(), _currentItem.item );
				Node msg = n.getAttributes().getNamedItem("msg");
				Node msgId = n.getAttributes().getNamedItem("msgId");
				if (condition != null && msg != null)
					condition.setMessage(msg.getNodeValue());
				else if (condition != null && msgId != null)
				{
					condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
					Node addName = n.getAttributes().getNamedItem("addName");
					if (addName != null && Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)
						condition.addName();
				}
				_currentItem.item.attach(condition);
			}
        }
        for (n = first; n != null; n = n.getNextSibling())
        {
			if ("skill".equalsIgnoreCase(n.getNodeName()))
			{
				attachSkill(n, _currentItem.item, null);
			}
        }
    }

    private void makeItem()
    {
        if (_currentItem.item != null) return;
        if (_currentItem.type instanceof L2ArmorType) _currentItem.item = new L2Armor(
        		(L2ArmorType) _currentItem.type, _currentItem.set);
        else if (_currentItem.type instanceof L2WeaponType) _currentItem.item = new L2Weapon(
        		(L2WeaponType) _currentItem.type, _currentItem.set);
        else if (_currentItem.type instanceof L2EtcItemType) _currentItem.item = new L2EtcItem(
        		(L2EtcItemType) _currentItem.type, _currentItem.set);
        else throw new Error("Unknown item type " + _currentItem.type);
    }

    /**
     * @return
     */
    public List<L2Item> getItemList()
    {
        return _itemsInFile;
    }
}

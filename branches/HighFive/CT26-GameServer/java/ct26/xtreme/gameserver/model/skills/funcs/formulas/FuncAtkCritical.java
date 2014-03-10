/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ct26.xtreme.gameserver.model.skills.funcs.formulas;

import ct26.xtreme.gameserver.model.skills.funcs.Func;
import ct26.xtreme.gameserver.model.stats.BaseStats;
import ct26.xtreme.gameserver.model.stats.Env;
import ct26.xtreme.gameserver.model.stats.Stats;

/**
 * @author UnAfraid
 */
public class FuncAtkCritical extends Func
{
	private static final FuncAtkCritical _fac_instance = new FuncAtkCritical();
	
	public static Func getInstance()
	{
		return _fac_instance;
	}
	
	private FuncAtkCritical()
	{
		super(Stats.CRITICAL_RATE, 0x09, null, null);
	}
	
	@Override
	public void calc(Env env)
	{
		env.mulValue(BaseStats.DEX.calcBonus(env.getCharacter()) * 10);
		env.setBaseValue(env.getValue());
	}
}
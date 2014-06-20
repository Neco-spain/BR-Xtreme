/*
 * Copyright (C) 2004-2013 L2J Server
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
package ct23.xtreme.communityserver.communityboard.boards;

import ct23.xtreme.communityserver.communityboard.CommunityBoard;
import ct23.xtreme.communityserver.communityboard.CommunityBoardManager;

public class AdminBoard extends CommunityBoard
{
	public AdminBoard(final CommunityBoardManager mgr)
	{
		super(mgr);
	}
	
	@Override
	public final void parseCmd(final int playerObjId, final String cmd)
	{
		
	}
	
	@Override
	public final void parseWrite(final int playerObjId, final String ar1, final String ar2, final String ar3, final String ar4, final String ar5)
	{
		
	}
}

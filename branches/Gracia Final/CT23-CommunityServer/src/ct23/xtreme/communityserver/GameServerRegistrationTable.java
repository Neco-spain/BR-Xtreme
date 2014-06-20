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
package ct23.xtreme.communityserver;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javolution.util.FastMap;
import ct23.xtreme.communityserver.util.Rnd;

public final class GameServerRegistrationTable
{
	private static Logger _log = Logger.getLogger(GameServerRegistrationTable.class.getName());
	// SQL
	private static final String GET_GAMESERVERS = "SELECT hex_id FROM registered_gameservers";
	
	private static final int KEYS_SIZE = 10;
	
	private final Map<byte[], Boolean> _registeredGameServers;
	private final KeyPair[] _keyPairs;
	
	protected GameServerRegistrationTable()
	{
		_registeredGameServers = loadRegisteredGameServers();
		_log.info("Loaded " + _registeredGameServers.size() + " registered GameServers");
		
		_keyPairs = loadRSAKeys();
		_log.info("Cached " + _keyPairs.length + " RSA keys for GameServer communication.");
	}
	
	private static final KeyPair[] loadRSAKeys()
	{
		final KeyPair[] keyPairs = new KeyPair[KEYS_SIZE];
		try
		{
			final KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
			final RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(512, RSAKeyGenParameterSpec.F4);
			keyGen.initialize(spec);
			
			for (int i = 0; i < KEYS_SIZE; i++)
			{
				keyPairs[i] = keyGen.genKeyPair();
			}
		}
		catch (Exception e)
		{
			
		}
		return keyPairs;
	}
	
	private final Map<byte[], Boolean> loadRegisteredGameServers()
	{
		final Map<byte[], Boolean> registeredGameServers = new FastMap<>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			Statement statement = con.createStatement();
			ResultSet rset = statement.executeQuery(GET_GAMESERVERS))
		{
			while (rset.next())
			{
				// for (byte b : stringToHex(rset.getString("hex_id")))
				registeredGameServers.put(stringToHex(rset.getString("hex_id")), false);
			}
		}
		catch (Exception e)
		{
			_log.info(getClass().getName() + ": Failed getting gameservers from database");
		}
		return registeredGameServers;
	}
	
	private static final byte[] stringToHex(final String string)
	{
		return new BigInteger(string, 16).toByteArray();
	}
	
	public final KeyPair getRandomKeyPair()
	{
		return _keyPairs[Rnd.get(_keyPairs.length)];
	}
	
	public final boolean isHexIdOk(final byte[] hexId)
	{
		for (final byte[] hex : _registeredGameServers.keySet())
		{
			if (Arrays.equals(hex, hexId))
			{
				return true;
			}
		}
		return false;
	}
	
	public final boolean isHexIdInUse(final byte[] hexId)
	{
		for (Map.Entry<byte[], Boolean> entry : _registeredGameServers.entrySet())
		{
			if (entry.getValue() && Arrays.equals(entry.getKey(), hexId))
			{
				return true;
			}
		}
		return false;
	}
	
	public final void setHexIdInUse(final byte[] hexId)
	{
		for (Entry<byte[], Boolean> entry : _registeredGameServers.entrySet())
		{
			if (Arrays.equals(entry.getKey(), hexId))
			{
				entry.setValue(false);
				break;
			}
		}
	}
	
	public static GameServerRegistrationTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final GameServerRegistrationTable _instance = new GameServerRegistrationTable();
	}
}

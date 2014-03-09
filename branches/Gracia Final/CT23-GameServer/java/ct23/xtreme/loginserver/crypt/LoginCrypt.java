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
package ct23.xtreme.loginserver.crypt;

import java.io.IOException;

import ct23.xtreme.util.Rnd;

/**
 *
 * @author  KenM
 */
public class LoginCrypt
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b, (byte) 0x60, (byte) 0xcb, (byte) 0x5b,
		(byte) 0x82, (byte) 0xce, (byte) 0x90, (byte) 0xb1,
		(byte) 0xcc, (byte) 0x2b, (byte) 0x6c, (byte) 0x55,
		(byte) 0x6c, (byte) 0x6c, (byte) 0x6c, (byte) 0x6c
	};

	private NewCrypt _staticCrypt = new NewCrypt(STATIC_BLOWFISH_KEY);
    private NewCrypt _crypt;
    private boolean _static = true;

    public void setKey(byte[] key)
    {
        _crypt = new NewCrypt(key);
    }

    public boolean decrypt(byte[] raw, final int offset, final int size) throws IOException
    {
    	_crypt.decrypt(raw, offset, size);
    	return NewCrypt.verifyChecksum(raw, offset, size);
    }

    public int encrypt(byte[] raw, final int offset, int size) throws IOException
    {
    	// reserve checksum
    	size += 4;


    	if (_static)
    	{
    		// reserve for XOR "key"
    		size += 4;

    		// padding
        	size += 8 - size%8;
    		NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
    		_staticCrypt.crypt(raw, offset, size);

    		_static = false;
    	}
    	else
    	{
    		// padding
        	size += 8 - size%8;
    		NewCrypt.appendChecksum(raw, offset, size);
    		_crypt.crypt(raw, offset, size);
    	}
    	return size;
    }
}

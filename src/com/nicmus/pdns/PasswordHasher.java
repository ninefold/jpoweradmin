/**
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 * 
 * Control panel for PowerDNS (http://powerdns.com)
 * Copyright (C) 2010 nicmus inc.
 * Jivko Sabev (jivko.sabev@gmail.com) jsabev@nicmus.com
 * 
 * @author jsabev 
 */
package com.nicmus.pdns;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;

/**
 * A bean to provide password hashing capabilities 
 * @author jsabev
 *
 */
@Name("passwordHasher")
public class PasswordHasher {
	private String hashingAlgorithm;
	private MessageDigest messageDigest;
	
	/**
	 * Instantiate the PasswordHasher with the default hashing algorithm
	 */
	public PasswordHasher(){}
	
	@Create
	public void init(){
		try {
			this.messageDigest = MessageDigest.getInstance(this.hashingAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the hashingAlgorithm used
	 */
	public String getHashingAlgorithm() {
		return this.hashingAlgorithm;
	}
	
	/**
	 * @param hashingAlgorithm the hashingAlgorithm to set
	 */
	public void setHashingAlgorithm(String hashingAlgorithm) {
		this.hashingAlgorithm = hashingAlgorithm;
	}

	/**
	 * Get the message digest of the string passed in.
	 * @param string string whose message digest to return.
	 * @return
	 */
	public String getHash(String string){
		this.messageDigest.update(string.getBytes(), 0, string.length());
		byte[] digestBytes = this.messageDigest.digest();
		BigInteger digest = new BigInteger(1,digestBytes);
		return digest.toString(16);
	}
}

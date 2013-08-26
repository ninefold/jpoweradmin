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

import java.io.Serializable;
import java.util.Random;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * Provides password generation functionality
 * @author jsabev
 *
 */
@Name("passwordGenerator")
@Scope(ScopeType.EVENT)
public class PasswordGenerator implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int MIN_ASCII_INDEX = 33;
	private static final int MAX_ASCII_INDEX = 126;
	private static final int DEFAULT_PASSWORD_LENGTH = 8;
	private String password;
	private String passwordHash;
	
	@In(create=true)
	private PasswordHasher passwordHasher;
	
	public void generatePassword(){
		this.generatePassword(DEFAULT_PASSWORD_LENGTH);
	}
	
	/**
	 * Generate a random password of the specified length. The password consists
	 * of ascii characters in the range of [33,126]
	 * @param length length of password to generate
	 */
	public void generatePassword(int length){
		//generate the password
		Random randomGenerator = new Random();
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i<length; i++){
			int asciiIndex = randomGenerator.nextInt((MAX_ASCII_INDEX-MIN_ASCII_INDEX+1))+MIN_ASCII_INDEX;
			char c = (char)asciiIndex;
			stringBuilder.append(c);
		}
		this.password = stringBuilder.toString();
		this.passwordHash = this.passwordHasher.getHash(this.password);
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return this.password;
	}

	/**
	 * @return the passwordHash
	 */
	public String getPasswordHash() {
		return this.passwordHash;
	}
	
	
	
}

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
package com.nicmus.pdns.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import com.nicmus.pdns.PasswordHasher;
import com.nicmus.pdns.entities.User;

/**
 * Manage user aspects of the api
 * @author jsabev
 *
 */
@Name("userActionAPI")
@Path("/")
public class UserActionAPI {
	
	@HeaderParam("X-JPowerAdmin-API-Key")
	private String apiKey;
	
	@In(create=true)
	private ApiDao apiDao;
	
	
	@In(create=true)
	private PasswordHasher passwordHasher;
	
	
	/**
	 * 
	 * @return
	 */
	@GET
	@Path("/user")
	@Produces("application/xml")
	public User getUser(){
		return this.apiDao.getUser(this.apiKey);
	}
	
	/**
	 * 
	 * @param user
	 */
	@PUT
	@Path("/user")
	@Consumes("application/xml")
	public void updateUser(User user){
		User userToUpdate = this.apiDao.getUser(this.apiKey);
		String newPass = user.getPassword();
		userToUpdate.setFirstName(user.getFirstName());
		userToUpdate.setLastName(user.getLastName());
		userToUpdate.setEmail(user.getEmail());
		String oldPass = userToUpdate.getPassword();
		//change the password if not the same
		if(!oldPass.equals(newPass)){
			String hash = this.passwordHasher.getHash(newPass);
			userToUpdate.setPassword(hash);
		}
		//update the billing address
		this.apiDao.saveObject(userToUpdate);
	}
}

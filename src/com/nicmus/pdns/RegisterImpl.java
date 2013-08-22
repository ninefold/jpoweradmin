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


import java.util.Locale;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import com.nicmus.pdns.entities.User;
import com.nicmus.pdns.entities.UserProperties;

@Stateless
@Name("register")
public class RegisterImpl implements Register {
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private PasswordChecker passwordChecker;
	
	@In(create=true)
	private PasswordHasher passwordHasher;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@In
	private TimeZone timeZone;
	
	@In
	private Locale locale;
	
	
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.Register#register()
	 */
	public String register(User user) {
		if(!isValid(user)){
			return "failure";
		}
		
		String hashedPassword = this.passwordHasher.getHash(this.passwordChecker.getPassword());
		user.setPassword(hashedPassword);
		UserProperties userProperties = new UserProperties();
		userProperties.setPreferredTimeZoneId(this.timeZone.getID());
		userProperties.setPreferredLanguageCode(this.locale.getLanguage());
		
		user.setUserProperties(userProperties);
		userProperties.setUser(user);

		this.entityManager.persist(user);

		this.facesMessages.addFromResourceBundle(Severity.INFO, "Register.SuccessfullRegistration", user.getUserName());
		
		return "success";
	}
	
	/**
	 * 
	 * @param user
	 * @return true on valid/false otherwise
	 */
	private boolean isValid(User user){
		//verify that the passwords match
		if(!this.passwordChecker.getPassword().equals(this.passwordChecker.getConfirmPassword())){
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "Register.PasswordsDoNotMatch");
			return false;
		}
		
		//verify that the user name request does not clash with another
		Query userQuery = this.entityManager.createQuery("from User u where u.userName = :userName");
		userQuery.setParameter("userName", user.getUserName());
		try {
			User existingUser = (User) userQuery.getSingleResult();
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "Register.UserTaken",existingUser.getUserName());
			return false;
		} catch (NoResultException e){
			//everything is good. 
		}
		return true;
	}

}

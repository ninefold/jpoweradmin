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

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.PasswordResetToken;
import com.nicmus.pdns.entities.User;

@Name("passwordResetter")
@Scope(ScopeType.CONVERSATION)
@Stateful
public class PasswordResetterImpl implements Serializable, PasswordResetter {
	private static final long serialVersionUID = -6459211022708787461L;

	@RequestParameter
	private String id;
	
	@In(create=true)
	private PasswordChecker passwordChecker;

	@In(create=true)
	private PasswordHasher passwordHasher;

	@In
	private FacesMessages facesMessages;
	
	private boolean verified = false;
	
	@Out(required=false)
	private User user;
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	@Logger
	private Log logger;
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.PasswordResetter#resetPassword()
	 */
	@Override
	public String resetPassword() {
		if(!this.passwordChecker.getPassword().equals(this.passwordChecker.getConfirmPassword())){
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "Register.PasswordsDoNotMatch");
			return "failure";
		}
		//get the user
		
		String hash = this.passwordHasher.getHash(this.passwordChecker.getPassword());
		this.user.setPassword(hash);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "PasswordChanger.PasswordChanged", this.user.getUserName());
		this.facesMessages.addFromResourceBundle(Severity.INFO, "PasswordResetter.NowYouCanLogin");
		//delete the reset token
		return "success";
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.PasswordResetter#verifyGuid()
	 */
	@Override
	public void verifyGuid(){
		if(this.verified){
			return;
		}
		if(this.id == null){
			throw new MissingRequiredParameterException();
		}
		//verify that the user exists for the given guid
		//verify that the user exists for the given guid
		
		Query guidQuery = this.entityManager.createQuery("from PasswordResetToken where guid = :guid");
		Query userQuery = this.entityManager.createQuery("from User u where u.userName = :userName");
		guidQuery.setParameter("guid", this.id);
		try {
			PasswordResetToken passwordResetToken = (PasswordResetToken) guidQuery.getSingleResult();
			//now find the user name for the given password reset token
			userQuery.setParameter("userName", passwordResetToken.getUserName());
			this.user = (User) userQuery.getSingleResult();
			
		} catch (NoResultException e){
			throw new JPowerAdminException("BAD GUID");
		}
		
		if(this.user == null){
			throw new MissingRequiredParameterException();
		}
		this.verified = true;
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.PasswordResetter#remove()
	 */
	@Override
	@Remove
	@Destroy
	public void remove(){
		this.logger.debug("Destroying {0}", PasswordResetterImpl.class.getName());
	}
	
}

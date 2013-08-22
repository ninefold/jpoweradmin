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
import java.util.Map;
import java.util.UUID;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
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
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.international.TimeZoneSelector;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import org.jboss.seam.security.Identity;

import com.nicmus.pdns.entities.User;


@Name("userAction")
@Scope(ScopeType.CONVERSATION)
@Stateful
public class UserActionImpl implements  Serializable, UserAction {
	private static final long serialVersionUID = 4303371801836352333L;

	@In
	private int userId;
	
	@In
	private Credentials credentials;
	
	@In
	private Identity identity;
	
	@In(create=true)
	@Out(required=false)
	private User user;
	
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private LocaleSelector localeSelector;
	
	@In
	private TimeZoneSelector timeZoneSelector;
	
	//localisation messages
	@In
	private Map<String, String> messages;
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;

	@Logger
	private Log logger;
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.UserAction#init()
	 */
	@Override
	public void init(){
		//initialise the user to be edited
		this.user = this.entityManager.find(User.class, this.userId);
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.UserAction#update()
	 */
	@Override
	public String update() {
		if(!this.user.getUserName().equals(this.credentials.getUsername())){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "UserAction.Name");
			this.entityManager.refresh(this.user);
			return null;
		}
		
		this.timeZoneSelector.selectTimeZone(this.user.getUserProperties().getPreferredTimeZoneId());
		this.localeSelector.selectLanguage(this.user.getUserProperties().getPreferredLanguageCode());
		this.timeZoneSelector.select();
		this.localeSelector.select();
		
		this.entityManager.flush();
		this.facesMessages.addFromResourceBundle("UserAction.UserUpdated", this.user.getUserName());
		return "success";
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.UserAction#passwordChange()
	 */
	@Override
	public String passwordChange(){
		return "/passchange.xhtml";
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.UserAction#changeAPICode()
	 */
	@Override
	public void changeAPICode() {
		this.user.setApiCode(UUID.randomUUID().toString());
		this.entityManager.flush();
	}


	/* (non-Javadoc)
	 * @see com.nicmus.pdns.UserAction#delete()
	 */
	@Override
	public boolean delete(){
		if(this.user.getUserProperties().getConfirmDelete().equalsIgnoreCase(this.messages.get("UserAction.Affirmative"))){
			this.entityManager.remove(this.user);
			this.facesMessages.addFromResourceBundle("UserAction.UserDeleted", this.user.getUserName());
			this.identity.unAuthenticate();
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.UserAction#destroy()
	 */
	@Override
	@Remove
	@Destroy
	public void destroy(){
		this.logger.debug("Destroying #0", UserActionImpl.class.getName());
	}

}

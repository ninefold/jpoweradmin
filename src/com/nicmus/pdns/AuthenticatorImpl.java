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

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;

import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.TimeZoneSelector;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Credentials;
import com.nicmus.pdns.entities.User;
import com.nicmus.pdns.entities.UserProperties;


@Name("authenticator")
@Stateless
public class AuthenticatorImpl implements Authenticator {
    @Logger 
    private Log logger;

    @In 
    private Credentials credentials;
    
    @In
    private Locale locale;

    
    @In
    private LocaleSelector localeSelector;
    
    @In
    private TimeZone timeZone;
    
    @In
    private TimeZoneSelector timeZoneSelector;
    
    @In
    private FacesMessages facesMessages;
    
    @In(create=true)
    private PasswordHasher passwordHasher;
    
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Out(required=false)
    private User user;
    
    public boolean authenticate() {
        this.logger.info("Trying to authenticate {0}", credentials.getUsername());
        Query userQuery = this.entityManager.createQuery("from User u where u.userName = :userName and u.password = :password");
        String passwordHash = this.passwordHasher.getHash(this.credentials.getPassword()); 
        userQuery.setParameter("userName", this.credentials.getUsername());
        userQuery.setParameter("password", passwordHash);
        try {
        	this.user = (User)userQuery.getSingleResult();
        	this.logger.info("User #0  authenticated successfully", this.user);
        	
        	UserProperties userProperties = this.user.getUserProperties();
        	
        	if(userProperties.getLastLoginDate() != null){
        		String lastLoginIpAddress = this.user.getUserProperties().getLastloginIp();
        		Date lastLoginDate = this.user.getUserProperties().getLastLoginDate();
        		DateFormat df = DateFormat.getDateTimeInstance();
        		String date = df.format(lastLoginDate);
        		this.facesMessages.addFromResourceBundle(Severity.INFO, "Authenticator.LastLoginMessage", lastLoginIpAddress, date);
        	}
        	
        	HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        	userProperties.setLastLoginDate(new Date());
        	userProperties.setLastloginIp(request.getRemoteAddr());
        	this.entityManager.persist(userProperties);
        	
        	//change the time zone of the user if different
        	if(this.user.getUserProperties().getPreferredTimeZoneId() != null){
        		String timeZoneId = this.user.getUserProperties().getPreferredTimeZoneId();
        		String serverTimeZone = this.timeZone.getID();
    			this.timeZoneSelector.setTimeZoneId(this.user.getUserProperties().getPreferredTimeZoneId());
        		if(!timeZoneId.equals(serverTimeZone)){
	        		this.timeZoneSelector.select();
        		}
        	}
        	//change the locale of the user if different
        	if(this.user.getUserProperties().getPreferredLanguageCode() != null){
        		String preferredLanguage = this.user.getUserProperties().getPreferredLanguageCode();
    			this.localeSelector.setLanguage(preferredLanguage);
        		if(!preferredLanguage.equals(this.locale.getLanguage())){
        			this.localeSelector.select();
        		}
        	}
        	Contexts.getSessionContext().set("userId", this.user.getId());
        	return true;
        } catch (NoResultException e){
        	
        }
        return false;
    }

}

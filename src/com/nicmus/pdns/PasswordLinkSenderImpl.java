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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.LocaleSelector;
import org.jboss.seam.international.StatusMessage.Severity;

import com.nicmus.pdns.entities.PasswordResetToken;
import com.nicmus.pdns.entities.User;

@Name("passwordLinkSender")
@Stateless
public class PasswordLinkSenderImpl implements Serializable,PasswordLinkSender {
	private static final long serialVersionUID = -978812265105711416L;

	@Out(required=false)
	private User user;
	
	@In
	private LocaleSelector localeSelector;
	
	@In 
	private FacesMessages facesMessages;

	@In(create=true)
	private UserName userName;
	
	@In(create=true)
	private EmailService emailService;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.marquedor.pdns.passwordReset.PasswordReset#isPasswordResetable(java.lang.String)
	 */
	public String sendPasswordLink(){
		String sql = "from User user where user.userName = :userName";
		Query userQuery = this.entityManager.createQuery(sql);
		
		userQuery.setParameter("userName", this.userName.getUserName());
		try {
			this.user = (User) userQuery.getSingleResult();
			String preferredLanguageCode = this.user.getUserProperties().getPreferredLanguageCode();
			if(preferredLanguageCode != null){
				this.localeSelector.setLanguage(preferredLanguageCode);
				this.localeSelector.select();
			}
			if(user.getEmail() == null || this.user.getEmail().isEmpty()){
				this.facesMessages.addFromResourceBundle(Severity.WARN, "PasswordResetter.NoEmail", this.user.getUserName());
				this.facesMessages.addFromResourceBundle(Severity.WARN, "PasswordResetter.NoEmailDetail");
				this.facesMessages.addFromResourceBundle(Severity.INFO, "PasswordResetter.ContactUs");
			} else {
				//save the link for the uiid to the db
				PasswordResetToken passwordResetToken = (PasswordResetToken) Component.getInstance(PasswordResetToken.class);
				String uiid = UUID.randomUUID().toString();
				passwordResetToken.setUserName(this.user.getUserName());
				passwordResetToken.setGuid(uiid);
				this.entityManager.persist(passwordResetToken);
				FacesContext facesContext = FacesContext.getCurrentInstance();
				HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
				String serverName = request.getServerName();
				String contextPath = request.getContextPath();
				String passwordPage = "/resetpass.html";
				URI url = new URI("http", serverName, contextPath.concat(passwordPage), "id=" + uiid, null);
				String passwordResetLink = url.toURL().toString();
				this.user.getUserProperties().setNewPasswordLink(passwordResetLink);
				this.emailService.sendMessage(1000, "/emails/password_" +this.localeSelector.getLanguage() + ".xhtml", this.user);
				return "success";
			}
		} catch (NoResultException e){
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "PasswordResetter.NoSuchUser", this.userName.getUserName());
		} catch (NonUniqueResultException e){
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "PasswordResetter.SomethingUnexpected");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "failure";
	}
	
	
}

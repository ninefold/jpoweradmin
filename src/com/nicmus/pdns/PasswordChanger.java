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

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import com.nicmus.pdns.entities.User;

@Name("passwordChanger")
public class PasswordChanger {
	@In
	private User user;
	
	@In(create=true)
	private PasswordHasher passwordHasher;
	
	@In(create=true)
	private PasswordChecker passwordChecker;
	
	private String oldPassword;

	@In
	private FacesMessages facesMessages;
	
	/**
	 * 
	 * @return
	 */
	public String changePassword(){
		String passHash = this.passwordHasher.getHash(oldPassword);
		if(!this.user.getPassword().equals(passHash)){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "PasswordChanger.PasswordDoesNotMatch");
			return null;
		}
		if(!this.passwordChecker.getPassword().equals(this.passwordChecker.getConfirmPassword())){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "PasswordChanger.PasswordsDoNotMatch");
			return null;
		}
		String newPassHash = this.passwordHasher.getHash(this.passwordChecker.getPassword());
		this.user.setPassword(newPassHash);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "PasswordChanger.PasswordChanged", this.user.getUserName());
		return "success";
	}
	
	/**
	 * @return the oldPassword
	 */
	@NotNull
	@NotEmpty
	public String getOldPassword() {
		return this.oldPassword;
	}

	/**
	 * @param oldPassword the oldPassword to set
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	
	
}

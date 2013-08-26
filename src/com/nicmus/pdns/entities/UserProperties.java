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
 * Copyright (C) 2010 Jivko Sabev
 * Jivko Sabev (jivko.sabev@gmail.com) jsabev@nicmus.com
 * 
 * @author jsabev 
 */
package com.nicmus.pdns.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name="user_properties")
public class UserProperties implements Serializable {
	private static final long serialVersionUID = 4559795046921974044L;
	private int id;
	private String preferredTimeZoneId;
	private String preferredLanguageCode;
	private String lastloginHostname;
	private String lastloginIp;
	private Date lastLoginDate;
	//mapping back to user
	private User user;
	private String newPasswordLink;
	private String confirmDelete;
	
	
	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public int getId() {
		return this.id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the preferredTimeZoneId
	 */
	public String getPreferredTimeZoneId() {
		return this.preferredTimeZoneId;
	}

	/**
	 * @param preferredTimeZoneId the preferredTimeZoneId to set
	 */
	public void setPreferredTimeZoneId(String preferredTimeZoneId) {
		this.preferredTimeZoneId = preferredTimeZoneId;
	}
	
	/**
	 * @return the preferredLanguageCode
	 */
	public String getPreferredLanguageCode() {
		return this.preferredLanguageCode;
	}
	/**
	 * @param preferredLanguageCode the preferredLanguageCode to set
	 */
	public void setPreferredLanguageCode(String preferredLanguageCode) {
		this.preferredLanguageCode = preferredLanguageCode;
	}
	
	/**
	 * @return the lastloginHostname
	 */
	public String getLastloginHostname() {
		return this.lastloginHostname;
	}
	
	/**
	 * @param lastloginHostname the lastloginHostname to set
	 */
	public void setLastloginHostname(String lastloginHostname) {
		this.lastloginHostname = lastloginHostname;
	}
	/**
	 * @return the lastloginIp
	 */
	public String getLastloginIp() {
		return this.lastloginIp;
	}
	
	/**
	 * @param lastloginIp the lastloginIp to set
	 */
	public void setLastloginIp(String lastloginIp) {
		this.lastloginIp = lastloginIp;
	}
	
	/**
	 * @return the lastLoginDate
	 */
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastLoginDate() {
		return this.lastLoginDate;
	}
	/**
	 * @param lastLoginDate the lastLoginDate to set
	 */
	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	
	/**
	 * @return the user
	 */
	@OneToOne(mappedBy="userProperties")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public User getUser() {
		return this.user;
	}
	
	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return the newPassword
	 */
	@Transient
	public String getNewPasswordLink() {
		return this.newPasswordLink;
	}

	/**
	 * @param newPassword the newPassword to set
	 */
	public void setNewPasswordLink(String newPassword) {
		this.newPasswordLink = newPassword;
	}

	/**
	 * @return the confirmDelete
	 */
	@Transient
	public String getConfirmDelete() {
		return this.confirmDelete;
	}

	/**
	 * @param confirmDelete the confirmDelete to set
	 */
	public void setConfirmDelete(String confirmDelete) {
		this.confirmDelete = confirmDelete;
	}
	
}

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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;


@Name("user")
@Entity
@Table(name="user")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class User extends PersistentObject implements Serializable{
	private static final long serialVersionUID = 3275570468891486164L;
	private String firstName;
	private String lastName;
	private String userName;
	private String password;
	private String apiCode;
	private String email;
	private UserProperties userProperties;
	//a user has a collection of domains
	private Set<Domain> domains = new LinkedHashSet<Domain>();
	
	/**
	 * @return the firstName
	 */
	@XmlElement
	public String getFirstName() {
		return this.firstName;
	}
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return the lastName
	 */
	@XmlElement
	public String getLastName() {
		return this.lastName;
	}
	
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	/**
	 * @return the userName
	 */
	@NotNull
	@Index(name="userNameIdx")
	@Length(min=4, max=128)
	@XmlElement
	public String getUserName() {
		return this.userName;
	}
	/**
	 * @param userName the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @return the password
	 */
	@NotNull
	@Length(min=4,max=256)
	@XmlElement
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	
	/**
	 * @return the apiCode
	 */
	public String getApiCode() {
		return this.apiCode;
	}
	/**
	 * @param apiCode the apiCode to set
	 */
	public void setApiCode(String apiCode) {
		this.apiCode = apiCode;
	}
	/**
	 * @return the email
	 */
	@Email
	@XmlElement
	public String getEmail() {
		return this.email;
	}
	
	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	
	/**
	 * @return the domains
	 */
	@OneToMany(mappedBy="user")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public Set<Domain> getDomains() {
		return this.domains;
	}
	/**
	 * @param domains the domains to set
	 */
	public void setDomains(Set<Domain> domains) {
		this.domains = domains;
	}

	/**
	 * @return the userProperties
	 */
	@OneToOne(cascade=CascadeType.ALL)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public UserProperties getUserProperties() {
		return this.userProperties;
	}

	/**
	 * @param userProperties the userProperties to set
	 */
	public void setUserProperties(UserProperties userProperties) {
		this.userProperties = userProperties;
	}
	

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((userName == null) ? 0 : userName.hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userName == null) {
			if (other.userName != null)
				return false;
		} else if (!userName.equals(other.userName))
			return false;
		return true;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.userName + "\t(" + this.firstName +  "," + this.lastName + ")\t" + "<" + this.email + ">"; 
	}
	
	@PrePersist
	public void onCreate(){
		super.onCreate();
		//generate a apiCode - basically a guid
		this.setApiCode(UUID.randomUUID().toString());
	}
}

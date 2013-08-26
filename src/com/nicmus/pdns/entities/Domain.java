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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.NotNull;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Role;
import org.jboss.seam.annotations.Roles;

@Entity
@Name("domain")
@Table(name="domains")
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
@Roles({@Role(name="newDomain",scope=ScopeType.EVENT)})
public class Domain extends PersistentObject implements Serializable{

	public enum Type{
		MASTER,
		SLAVE,
		NATIVE;
	}
	private static final long serialVersionUID = 7141054843608266657L;
	private String name;
	private String master;
	private int last_check;
	private Type type;
	private int notifiedSerial;
	private String account;
	//the given zone;
	private Set<Record> records = new LinkedHashSet<Record>();
	private User user; //mapping back to user
	
	
	/**
	 * @return the name
	 */
	@NotNull
	@Index(name="name_index")
	@XmlElement
	public String getName() {
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the master
	 */
	@XmlElement
	public String getMaster() {
		return this.master;
	}
	
	/**
	 * @param master the master to set
	 */
	public void setMaster(String master) {
		this.master = master;
	}
	
	/**
	 * @return the last_check
	 */
	public int getLast_check() {
		return this.last_check;
	}
	
	/**
	 * @param lastCheck the last_check to set
	 */
	public void setLast_check(int lastCheck) {
		this.last_check = lastCheck;
	}
	
	/**
	 * @return the type
	 */
	@Enumerated(EnumType.STRING)
	@NotNull
	@XmlElement
	public Type getType() {
		return this.type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the notifiedSerial
	 */
	@Column(name="notified_serial")
	@XmlElement
	public int getNotifiedSerial() {
		return this.notifiedSerial;
	}
	
	/**
	 * @param notifiedSerial the notifiedSerial to set
	 */
	public void setNotifiedSerial(int notifiedSerial) {
		this.notifiedSerial = notifiedSerial;
	}
	
	/**
	 * @return the account
	 */
	public String getAccount() {
		return this.account;
	}
	
	/**
	 * @param account the account to set
	 */
	public void setAccount(String account) {
		this.account = account;
	}
	
	
	/**
	 * @return the records
	 */
	@OneToMany(mappedBy="domain")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public Set<Record> getRecords() {
		return this.records;
	}
	
	/**
	 * @param records the records to set
	 */
	public void setRecords(Set<Record> records) {
		this.records = records;
	}
	
	/**
	 * @return the user
	 */
	@ManyToOne
	public User getUser() {
		return this.user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.toLowerCase().hashCode());
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
		Domain other = (Domain) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equalsIgnoreCase(other.name)) {
			return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.name;
	}

}

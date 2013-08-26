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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Index;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Role;
import org.jboss.seam.annotations.Roles;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Domain records bean - holds properties of a record
 * @author jsabev
 *
 */
@Entity
@Name("record")
@Table(name="records")
@Roles({@Role(name="primaryNS"),@Role(name="secondaryNS"), @Role(name="thernaryNS"), @Role(name="newRecord", scope=ScopeType.EVENT)})
@org.hibernate.annotations.Table(indexes={@Index(name="name_type_index", columnNames={"name","type"})}, appliesTo = "records")
@XmlRootElement
public class Record extends PersistentObject implements Serializable {
	private static final long serialVersionUID = 3684387704879755982L;

	public enum Type{
		SOA,
		NS,
		MX,
		A,
		AAAA,
		CNAME,
		HINFO,
		KEY,
		LOC,
		NAPTR,
		PTR,
		RP,
		SPF,
		SSHFP,
		SRV,
		TXT,
		MBOXFW,
		URL;
	}
	private int id;
	private String name;
	private Type type;
	private String content;
	private int ttl;
	private int prio;
	private long change_date;
	private Domain domain; //mapping back to domain
	
	/**
	 * @return the id
	 */
	@Override
	@XmlElement
	@Column(insertable=false,updatable=false)
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
	 * @return the name
	 */
	@NotNull
	@NotEmpty
	@Index(name="rec_name_index")
	@Length(min=1)
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
	 * @return the type
	 */
	@NotNull
	@Enumerated(EnumType.STRING)
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
	 * @return the content
	 */
	@NotNull
	@Length(min=1)
	@NotEmpty
	public String getContent() {
		return this.content;
	}
	
	/**
	 * @param content the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}
	
	/**
	 * @return the ttl
	 */
	@NotNull
	public int getTtl() {
		return this.ttl;
	}
	/**
	 * @param ttl the ttl to set
	 */
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	
	/**
	 * @return the prio
	 */
	@NotNull
	public int getPrio() {
		return this.prio;
	}
	
	/**
	 * @param prio the prio to set
	 */
	public void setPrio(int prio) {
		this.prio = prio;
	}

	/**
	 * @return the change_date
	 */
	public long getChange_date() {
		return this.change_date;
	}
	
	/**
	 * @param changeDate the change_date to set
	 */
	public void setChange_date(long changeDate) {
		this.change_date = changeDate;
	}

	/**
	 * @return the domain
	 */
	@ManyToOne
	@XmlTransient
	public Domain getDomain() {
		return this.domain;
	}
	
	/**
	 * @param domain the domain to set
	 */
	public void setDomain(Domain domain) {
		this.domain = domain;
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
		if (this == obj) {
			return true;
		}
		if (obj == null){
			return false;
		}
		if (getClass() != obj.getClass()){
			return false;
		}
		Record other = (Record) obj;
		
		if (name == null) {
			if (other.name != null){
				return false;
			}
		} else if (!name.equalsIgnoreCase(other.name)){
			return false;
		}
		
		if(this.name.equalsIgnoreCase(other.name) && this.content.equalsIgnoreCase(other.content) && this.type == other.type){
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	@BypassInterceptors
	public String toString() {
		return this.name + "\t" + this.type + "\t" + this.content;
	}
	
	@Override
	@PrePersist
	public void onCreate(){
		super.onCreate();
		this.setChange_date(this.getDateCreated().getTime());
	}
	
	@Override
	@PreUpdate
	public void onUpdate(){
		super.onUpdate();
		this.setChange_date(this.getDateModified().getTime());
	}
	
	
}

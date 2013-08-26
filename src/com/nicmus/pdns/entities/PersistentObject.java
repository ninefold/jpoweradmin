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

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Properties shared with all persistent objects
 * @author jsabev
 *
 */
@MappedSuperclass
public class PersistentObject {
	private int id;
	private Date dateCreated;
	private Date dateModified;
	
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
	protected void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the dateCreated
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@XmlElement
	public Date getDateCreated() {
		return this.dateCreated;
	}
	
	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	/**
	 * @return the dateModified
	 */
	@Temporal(TemporalType.TIMESTAMP)
	@XmlElement
	public Date getDateModified() {
		return this.dateModified;
	}
	
	/**
	 * @param dateModified the dateModified to set
	 */
	public void setDateModified(Date dateModified) {
		this.dateModified = dateModified;
	}
	
	@PrePersist
	public void onCreate(){
		this.dateCreated = new Date();
		this.dateModified = this.dateCreated;
	}
	
	@PreUpdate
	public void onUpdate(){
		this.dateModified = new Date();
	}
	
	
	
}

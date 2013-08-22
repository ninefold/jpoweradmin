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

import java.util.List;

import javax.ejb.Local;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;

/**
 * Zone DAO class providing functionality for managing data
 * @author jsabev
 *
 */
@Local
public interface ZoneDAO {
	
	
	/**
	 * Get all Zones for the given user id
	 * @param userId
	 * @return List of Domain objects corresponding to the list of of zones 
	 * for the given user id
	 */
	public List<Domain> getZones(int userId);
	
	/**
	 * Get the specified domain name.
	 * @param domainName
	 * @return The domain object corresponding the the given domain name, null 
	 * if it doesn't exist
	 */
	public Domain getDomain(String domainName);
	
	/**
	 * Create an SOA record for the given domain
	 * @param domainName the name of the domain whose SOA record needs to be created
	 */
	public void createSOA(String domainName);
	
	/**
	 * Convert the given domain from slave to master
	 * @param domainName
	 */
	public void convertToMaster(String domainName);
	
	/**
	 * Get all the records for the specific domain
	 * @param domainName
	 * @return
	 */
	public List<Record> getRecords(String domainName);
	
	/**
	 * Get the SOA Record for the given domain
	 */
	public Record getSOARecord(String domainName);

	/**
	 * Update the records of the children for the given parent. At the moment, 
	 * all linked domains' records are deleted and the whole zone is re-created 
	 * from scratch
	 * FIXME: Find a better way to manage the update of the parent domain name.  
	 * @param parentDomainName the parent domain whose records will be copied to the 
	 * children
	 */
	public abstract void updateChildrenDomains(String parentDomainName);
}

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
package com.nicmus.pdns.dao;

import java.util.List;

import javax.ejb.Local;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

/**
 * Zone DAO class providing functionality for managing data
 * @author jsabev
 *
 */
@Local
public interface ZoneDAO {
	
	/**
	 * Load the user based on the user id
	 * @param userId the userId of the user to load
	 * @return the user object corresponding to the user id
	 */
	public abstract User getUser(int userId);
	
	/**
	 * Update the given object
	 * @param object
	 */
	public abstract void updateObject(Object object);
	
	/**
	 * Create the given domain for the given user
	 * @param domain the domain to create
	 * @param userId the user id of the user in question
	 */
	public abstract void createDomain(Domain domain, int userId);
	
	/**
	 * Get the specified domain name.
	 * @param domainName
	 * @return The domain object corresponding the the given domain name, null 
	 * if it doesn't exist
	 */
	public Domain getDomain(String domainName);

	
	/**
	 * Create an SOA record for the given domain 
	 * @param domainName
	 */
	public void createSOA(String domainName);
	
	/**
	 * Convert the given domain from slave to master
	 * @param domainName
	 */
	public void convertToMaster(String domainName);
	
	/**
	 * Get the SOA Record for the given domain
	 */
	public Record getSOARecord(String domainName);


	/**
	 * Get the DNS records for the given zone name
	 * @param domainName the name of the zone
	 * @param offset the offset of the records 
	 * @param count the number of records to return
	 * @return the records matching the criteria
	 */
	public List<Record> getRecords(String domainName, int offset, int count);
	
	/**
	 * Get the number of DNS records for the given zone name
	 * @param domainName
	 * @return the number of DNS records for the given zone name
	 */
	public int getNumRecords(String domainName);
	
}

package com.nicmus.pdns.api;

import java.util.List;

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
import javax.ejb.Local;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

/**
 * Provide various data access operations for the api 
 * @author jsabev
 *
 */
@Local
public interface ApiDao {
	
	
	/**
	 * Get the number of zones for the given api key.
	 * @param apiKey the api key
	 * @return the total number of zones for the given api key
	 */
	public int getNumZones(String apiKey);
	
	/**
	 * Get the User corresponding to the given api key 
	 * @param apiKey
	 * @return the user corresponding to the given api key
	 */
	public User getUser(String apiKey);
	
	/**
	 * Get the given domain name
	 * @param domainName
	 * @return
	 */
	public Domain getDomain(String domainName);
	
	/**
	 * Get the list of domains matching the following criteria
	 * @param apiCode the apiCode of the user whose domains to get
	 * @param offset the 0 based offset of the records
	 * @param count the number of records to return
	 * @return the list of domains matching the criteria specified
	 */
	public List<Domain> getDomains(String apiCode, int offset, int count);
	
	/**
	 * Load the given record from the db
	 * @param recordId
	 * @return Record corresponding to id
	 */
	public Record getRecord(int recordId);
	
	/**
	 * Get the number of DNS records for the given zone
	 * @param apiCode the api code for the zone
	 * @param zoneName the zone name
	 * @return the number of records for the given zone
	 */
	public int getNumRecords(String apiCode, String zoneName);
	
	/**
	 * Persist the given object
	 * @param obj
	 */
	public void saveObject(Object obj);
	
	/**
	 * Delete the given object
	 * @param clazz the class of the object to delete
	 * @param id the id of the object to delete
	 */
	public void deleteObject(Class<? extends Object> clazz, int id);
	
	/**
	 * 
	 * @param obj
	 */
	public abstract void updateObject(Object obj);
}

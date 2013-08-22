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
package com.nicmus.pdns.api;

import javax.ejb.Local;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

/**
 * Wrapper DAO class around the entity manager. This is necessary as the RESTEASY
 * implementation does not support stateful EJBs!
 * @author jsabev
 *
 */
@Local
public interface ApiDao {
	
	/**
	 * Get the User corresponding to the given api key 
	 * @param apiKey
	 * @return 
	 */
	public User getUser(String apiKey);
	
	/**
	 * Get the given domain name
	 * @param domainName
	 * @return
	 */
	public Domain getDomain(String domainName);
	
	/**
	 * Load the given record from the db
	 * @param recordId
	 * @return Record corresponding to id
	 */
	public Record getRecord(int recordId);
	
	/**
	 * Persist the given object
	 * @param obj
	 */
	public void saveObject(Object obj);
	
	/**
	 * Delete the given object
	 * @param obj
	 */
	public void deleteObject(Object obj);
	
	/**
	 * 
	 */
	public void destroy();
}

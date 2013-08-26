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

/**
 * Provide the data access operations necessary for the pagination model
 * @author jsabev
 *
 */
@Local
public interface PaginationDAO<T> {
	
	/**
	 * Get the total number of records for the given model
	 * @return the total number of records for the given model
	 */
	public int getNumRecords();
	
	/**
	 * Get the number of records for filtered records
	 * @param filterBy the column to filter by
	 * @param filterValue the filter value
	 * @return
	 */
	public int getNumRecords(String filterBy, String filterValue);
	
	/**
	 * Get the record with the given id;
	 * @param id the database primary key for the given record
	 * @return the record corresponding to the given id
	 */
	public T getRecord(int id);

	/**
	 * 
	 * @param offset
	 * @param batchSize
	 * @param filterColumn
	 * @param filterValue
	 * @param sortField
	 * @param desc
	 * @return
	 */
	public List<T> findObjects(int offset, int batchSize, String filterColumn, String filterValue, String sortField, boolean desc);
	
	/**
	 * update the model
	 * @param t
	 */
	public void update(T t);
	
	/**
	 * 
	 * @param t
	 */
	public void refresh(T t);
	
	/**
	 * 
	 * @param t
	 */
	public void delete(T t);
	
	/**
	 * Destroy/Remove method - needed for stateful EJBS
	 */
	public void destroy();

}

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

@Local
public interface RecordAction {

	/**
	 * 
	 */
	public void init();

	/**
	 * 
	 */
	public void initRecords();

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#addRecord(com.nicmus.pdns.Record)
	 */
	public void addRecord(Record record);

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#editRecord(com.nicmus.pdns.Record)
	 */
	public String edit();

	/*
	 * 
	 */
	public String editSOA();

	/**
	 * Cancel the ongoing edit
	 * @return
	 */
	public abstract String cancelEdit();

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#deleteSelected()
	 */
	public void deleteSelected();

	/**
	 * @return the domain
	 */
	public Domain getDomain();

	/**
	 * Get the selected records 
	 * @return a list of selected records
	 */
	public List<Record> getSelected();

	/**
	 * 
	 */
	public void destroy();

}
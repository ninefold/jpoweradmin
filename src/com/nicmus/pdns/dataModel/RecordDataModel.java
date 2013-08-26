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
package com.nicmus.pdns.dataModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import com.nicmus.pdns.dao.PaginationDAO;
import com.nicmus.pdns.entities.Record;



/**
 * Paginatable DATA model for the Record management table in the interface
 * @author jsabev
 *
 */
@Name("recordDataModel")
@Scope(ScopeType.CONVERSATION)
public class RecordDataModel extends PaginatableModel<Record> {
	private static final long serialVersionUID = 6012719211577035465L;

	@In(create=true)
	private PaginationDAO<Record> recordPaginationDAO;
	
	//holds the selected records
	private Map<Record, Boolean> selectedRecords = new LinkedHashMap<Record, Boolean>();
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(Record record) {
		return record.getId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#getNumRecords()
	 */
	@Override
	public int getNumRecords() {
		//wrapper around the EJB
		if(this.getFilterColumn() != null && this.getFilterValue() != null && !this.getFilterColumn().isEmpty() && !this.getFilterValue().isEmpty()){
			return this.recordPaginationDAO.getNumRecords(this.getFilterColumn(), this.getFilterValue());
		}
		return this.recordPaginationDAO.getNumRecords();
	}

	@Override
	public Record getRecord(int id) {
		//wrapper around the EJB
		return this.recordPaginationDAO.getRecord(id);
	}
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#update(java.lang.Object)
	 */
	@Override
	public void update(Record record) {
		// wrapper around the EJB
		this.recordPaginationDAO.update(record);
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#refresh(java.lang.Object)
	 */
	//@Override
	public void refresh(Record record) {
		//wrapper around the ejb
		this.recordPaginationDAO.refresh(record);
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#delete(java.lang.Object)
	 */
	@Override
	public void delete(Record record) {
		if(this.wrappedData.containsKey(record.getId())){
			this.wrappedData.remove(record.getId());
			this.wrappedKeys.remove(this.wrappedKeys.indexOf(record.getId()));
		}
		//wrapper around the EJB
		this.recordPaginationDAO.delete(record);
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#findObjects(int, int, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<Record> findObjects(int offset, int batchSize,String filterColumn, String filterValue, String sortField,boolean desc) {
		//wrapper around the EJB
		return this.recordPaginationDAO.findObjects(offset, batchSize, filterColumn, filterValue, sortField, desc);
	}

	/**
	 * @return the selectedRecords
	 */
	public Map<Record, Boolean> getSelectedRecords() {
		return this.selectedRecords;
	}

	/**
	 * @param selectedRecords the selectedRecords to set
	 */
	public void setSelectedRecords(Map<Record, Boolean> selectedRecords) {
		this.selectedRecords = selectedRecords;
	}

	/**
	 * Return the list of records to delete
	 * @return list of records selected for deletion
	 */
	public List<Record> getRecordsToDelete(){
		List<Record> records = new ArrayList<Record>();
		Set<Record> recordKeySet = this.getSelectedRecords().keySet();
		for(Record r : recordKeySet){
			if(this.selectedRecords.get(r)){
				records.add(r);
			}
		}
		return records;
	}
}

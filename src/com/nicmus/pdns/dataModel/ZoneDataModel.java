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
import com.nicmus.pdns.entities.Domain;

/**
 * 
 * @author jsabev
 *
 */
@Name("zoneDataModel")
@Scope(ScopeType.CONVERSATION)
public class ZoneDataModel extends PaginatableModel<Domain> {
	private static final long serialVersionUID = 1L;
	@In(create=true)
	private PaginationDAO<Domain> zonePaginationDAO;

	private Map<Domain, Boolean> selectedZones = new LinkedHashMap<Domain, Boolean>();
	
	private int pageNumber;
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#getId(java.lang.Object)
	 */
	@Override
	public Integer getId(Domain object) {
		return object.getId();
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#findObjects(int, int, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<Domain> findObjects(int offset, int batchSize, String filterColumn, String filterValue, String sortField, boolean desc) {
		//Wrapper around EJB
		return this.zonePaginationDAO.findObjects(offset, batchSize, filterColumn, filterValue, sortField, desc);
	}

	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#getNumRecords()
	 */
	@Override
	public int getNumRecords() {
		//wrapper around the EJB
		if(this.getFilterColumn() != null && !this.getFilterColumn().isEmpty() && 
				this.getFilterValue() != null && !this.getFilterValue().isEmpty()){
			return this.zonePaginationDAO.getNumRecords(this.getFilterColumn(),this.getFilterValue());
		} else {
			return this.zonePaginationDAO.getNumRecords();
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#getRecord(int)
	 */
	@Override
	public Domain getRecord(int id) {
		return this.zonePaginationDAO.getRecord(id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#update(java.lang.Object)
	 */
	@Override
	public void update(Domain domain) {
		this.zonePaginationDAO.update(domain);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(Domain t) {
		this.zonePaginationDAO.refresh(t);
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.PaginatableModel#delete(java.lang.Object)
	 */
	@Override
	public void delete(Domain record) {
		if(this.wrappedData.containsKey(record.getId())){
			this.wrappedData.remove(record.getId());
			this.wrappedKeys.remove(this.wrappedKeys.indexOf(record.getId()));
		}
		//wrapper around the EJB
		this.zonePaginationDAO.delete(record);
	}

	/**
	 * @return the pageNumber
	 */
	public int getPageNumber() {
		return this.pageNumber;
	}

	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}


	/**
	 * @return the selectedZones
	 */
	public Map<Domain, Boolean> getSelectedZones() {
		return this.selectedZones;
	}

	/**
	 * @param selectedZones the selectedZones to set
	 */
	public void setSelectedZones(Map<Domain, Boolean> selectedZones) {
		this.selectedZones = selectedZones;
	}
	
	/**
	 * Return the list of selected domains in the zones table
	 * @return
	 */
	public List<Domain> getSelectedDomains(){
		List<Domain> selectedDomains = new ArrayList<Domain>();
		Set<Domain> zonesSet = this.selectedZones.keySet();
		for(Domain domain : zonesSet){
			if(this.selectedZones.get(domain)){
				selectedDomains.add(domain);
			}
		}
		return selectedDomains;
	}

	
	
}

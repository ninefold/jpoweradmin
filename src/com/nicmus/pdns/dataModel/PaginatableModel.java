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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.el.Expression;
import javax.faces.context.FacesContext;

import org.ajax4jsf.model.DataVisitor;
import org.ajax4jsf.model.Range;
import org.ajax4jsf.model.SequenceRange;
import org.ajax4jsf.model.SerializableDataModel;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Log;
import org.richfaces.model.FilterField;
import org.richfaces.model.Modifiable;
import org.richfaces.model.Ordering;
import org.richfaces.model.SortField2;

/**
 * Provide a generic model that supports true pagination in RichFaces.
 * Credit goes to http://eclecticprogrammer.com/about/ for original idea. 
 *  
 * @author jsabev
 *
 */
public abstract class PaginatableModel<T> extends SerializableDataModel implements Modifiable  {
	private static final long serialVersionUID = -3746850950595085637L;
	
	@Logger
	protected Log logger;
	private int rowIndex;//the rowIndex
	
	private Integer currentPK; //primary key of persistent object
	private boolean desc = false; //default sorting order
	private Integer rowCount; //total number of rows in the data
	
	protected List<Integer> wrappedKeys = new ArrayList<Integer>();
	
	//map of key-> object 
	protected Map<Integer, T> wrappedData = new LinkedHashMap<Integer, T>();
	
	//the name of the request parameter that will hold the field name that 
	//the model will by sorted by.
	private String sortField; //sort field
	
	private boolean detached = false;
	
	private String filterColumn;
	private String filterValue;
	
	
	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.SerializableDataModel#update()
	 */
	@Override
	public void update() {
		this.logger.debug("Updating model");
		this.detached = false;
	}

	
	
	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#setRowKey(java.lang.Object)
	 */
	@Override
	public void setRowKey(Object key) {
		this.currentPK = (Integer)key;
		
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#getRowKey()
	 */
	@Override
	public Object getRowKey() {
		return this.currentPK;
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#getSerializableModel(org.ajax4jsf.model.Range)
	 */
	@Override
	public SerializableDataModel getSerializableModel(Range range) {
		if(this.wrappedKeys.isEmpty()){
			this.detached = false;
			return null;
		}
		this.detached = true;
		this.logger.debug("Returning serialized model");
		return this;
	}

	/* (non-Javadoc)
	 * @see org.ajax4jsf.model.ExtendedDataModel#walk(javax.faces.context.FacesContext, org.ajax4jsf.model.DataVisitor, org.ajax4jsf.model.Range, java.lang.Object)
	 */
	@Override
	public void walk(FacesContext context, DataVisitor visitor, Range range, Object argument) throws IOException {
		if(this.detached){
			for(Integer key : this.wrappedKeys){
				this.setRowKey(key);
				visitor.process(context, key, argument);
			}
		} else {
			int firstRow = ((SequenceRange)range).getFirstRow();
			int numRows = ((SequenceRange)range).getRows();
			this.logger.debug("Loading records for model from db. Offset: {0}, size: {1}. Sorting the results by {2} descending {3}. Filtering column {4} with value {5}", firstRow, numRows, this.sortField, this.desc, this.filterColumn, this.filterValue);
			this.wrappedKeys.clear();
			this.wrappedData.clear();
			List<T> objects = this.findObjects(firstRow, numRows, this.filterColumn, this.filterValue, this.sortField, this.desc);
			for(T t : objects){
				Integer id = this.getId(t);
				this.wrappedData.put(id, t);
				this.wrappedKeys.add(id);
				visitor.process(context, id, argument);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.richfaces.model.Modifiable#modify(java.util.List, java.util.List)
	 */
	@Override
	public void modify(List<FilterField> filterFields, List<SortField2> sortFields) {
		//deal with the sorting
		if(sortFields != null && !sortFields.isEmpty()){
			SortField2 sortField = sortFields.get(0);
			Expression expression = sortField.getExpression();
			String expressionString = expression.getExpressionString();
			this.logger.debug("Sorting expression string: {0}", expressionString);
			if(!expression.isLiteralText()){
				expressionString = expressionString.replaceAll("[#|$]{1}\\{.*?\\.", "").replaceAll("\\}", "");
			}
			this.sortField = expressionString;
			
			Ordering ordering = sortField.getOrdering();
			if(ordering == Ordering.DESCENDING){
				this.desc = true;
			} else {
				this.desc = false;
			}
		}
		//deal with the filtering
		if(filterFields != null && !filterFields.isEmpty()){
			FilterField filterField = filterFields.get(0);
			Expression expression = filterField.getExpression();
			String expressionString = expression.getExpressionString();
			if(!expression.isLiteralText()){
				expressionString = expressionString.replaceAll("[#|$]{1}\\{.*?\\.", "").replaceAll("\\}", "");
			}
			this.filterColumn = expressionString;
			this.logger.debug("Filter expression string: {0}, Filter Columng {1}, Filter Value {2}" , expressionString, this.filterColumn, this.filterValue);
			
		}
	}



	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowData()
	 */
	@Override
	public Object getRowData() {
		if(this.currentPK == 0){
			return null;
		}
		if(this.wrappedData.containsKey(this.currentPK)){
			return this.wrappedData.get(this.currentPK);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getRowIndex()
	 */
	@Override
	public int getRowIndex() {
		return this.rowIndex;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.model.DataModel#setRowIndex(int)
	 */
	@Override
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	
	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#getWrappedData()
	 */
	@Override
	public Object getWrappedData() {
		return this;
	}

	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#setWrappedData(java.lang.Object)
	 */
	@Override
	public void setWrappedData(Object arg0) {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see javax.faces.model.DataModel#isRowAvailable()
	 */
	@Override
	public boolean isRowAvailable() {
		return this.currentPK != null;
	}



	/**
	 * Get the sorting field to sort by
	 * @return
	 */
	public String getSortField(){
		return this.sortField;
	}
	
	/**
	 * @param sortField the sortField to set
	 */
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}

	/**
	 * @return the desc
	 */
	public boolean isDesc() {
		return this.desc;
	}

	/**
	 * @param desc the desc to set
	 */
	public void setDesc(boolean desc) {
		this.desc = desc;
	}

	/**
	 * @return the filterColumn
	 */
	public String getFilterColumn() {
		return this.filterColumn;
	}

	/**
	 * @param filterColumn the filterColumn to set
	 */
	public void setFilterColumn(String filterColumn) {
		this.filterColumn = filterColumn;
	}



	/**
	 * @return the filterValue
	 */
	public String getFilterValue() {
		return this.filterValue;
	}


	/**
	 * @param filterValue the filterValue to set
	 */
	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}



	/**
	 * @return the rowCount
	 */
	public int getRowCount() {
		if(!this.detached){
			this.rowCount = this.getNumRecords();
		}
		if(this.rowCount == null){
			this.rowCount = this.getNumRecords();
		}
		return this.rowCount;
	}
	
	/**
	 * 
	 * @return
	 */
	public Collection<T> getWrappedObjects(){
		return this.wrappedData.values();
	}
	

	/**
	 * refresh the model values
	 */
	public void refreshModel(){
		Collection<T> values = this.wrappedData.values();
		for(T t : values){
			this.refresh(t);
		}
	}
	
	/**
	 * Get the id of the given object
	 * @param object
	 * @return  the id of the given object
	 */
	public abstract Integer getId(T object);


	/**
	 * Get the total number of records for the given model
	 * @return the total number of records for the given model
	 */
	public abstract int getNumRecords();


	/**
	 * Get the record with the given id;
	 * @param id the database primary key for the given record
	 * @return the record corresponding to the given id
	 */
	public abstract T getRecord(int id);

	/**
	 * Update the given record
	 * @param t
	 */
	public abstract void update(T t);
	
	/**
	 * Refresh the given record from the db
	 * @param t
	 */
	public abstract void refresh(T t);

	
	/**
	 * Delete the given record
	 * @param record
	 */
	public abstract void delete(T record);

	/**
	 * Load on demand the model records from the database matching the given c
	 * criteria
	 * @param offset where to start loading records from 
	 * @param batchSize how many records to get
	 * @param filterColumn the column that is used for filtering
	 * @param filterValue the filtering value of the record
	 * @param sortField the sorting column
	 * @param desc the sorting type - true for descending, false otherwise
	 * @return
	 */
	public abstract List<T> findObjects(int offset, int batchSize, String filterColumn, String filterValue, String sortField, boolean desc);
	
	
}

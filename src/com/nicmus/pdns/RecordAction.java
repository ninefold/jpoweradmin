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

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.dao.RecordDAO;
import com.nicmus.pdns.dataModel.RecordDataModel;
import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;

/**
 * Record form controller for the records portion of the user interface
 * @author jsabev
 *
 */
@Name("recordAction")
@Scope(ScopeType.CONVERSATION)
public class RecordAction implements Serializable {
	private static final long serialVersionUID = -6117102355127159628L;

	@Logger
	private Log logger;

	@In(create=true)
	@Out(required=false)
	private RecordDataModel recordDataModel;

	@In(required=true)
	private Domain domain;

	@In(create=true)
	private RecordDAO recordDAO;

	@In(create=true)
	private DNSValidator dnsValidator;

	@In(create=true)
	private FacesMessages facesMessages;

	/**
	 * Add the given record to the list of records for the zone
	 * @param record
	 */
	public void addRecord(Record record){
		//lowercase the record
		record.setName(record.getName().toLowerCase().trim());

		//add the domain name if necessary
		if(record.getName() == null || record.getName().isEmpty()){
			record.setName(this.domain.getName());
		}
		//add the domain suffix, if necessary
		//if(!record.getName().toLowerCase().endsWith(this.domain.getName())){
		//	record.setName(record.getName().concat(".").concat(this.domain.getName()));
		//}

		if(!this.dnsValidator.isValid(record, this.domain.getId())){
			return;
		}

		//add the record
		this.recordDAO.createRecord(record, this.domain.getId());
		this.recordDAO.incrementSOASerial(this.domain.getId());
		//refresh the model
		this.recordDataModel.update();
		this.logger.debug("Record {0} successfully added" , record);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.RecordAdded", record);
		record = (Record) Component.getInstance("newRecord", ScopeType.STATELESS);
	}

	/**
	 * Navigate to the record edit page
	 * @param record the record to edit
	 * @return the view name of the record edit page
	 */
	public String viewEdit(Record record){
		Contexts.getConversationContext().set("selectedRecord", record);
		return "/record.xhtml";
	}

	/**
	 * Edit the Start of Authority record
	 * @param record
	 * @return
	 */
	public String viewSOA(Record record){
		if(record.getType() != Record.Type.SOA){
			throw new JPowerAdminException("SOA record expected. Found: " + record.getType().name());
		}
		Contexts.getConversationContext().set("selectedRecord", record);
		String soaContent = record.getContent();
		String[] soaFields = soaContent.split("\\s+");
		if(soaFields.length != 7){
			throw new JPowerAdminException("BAD SOA format. Expected 7 fields. Found " + soaFields.length);
		}

		SOARecord soaRecord = new SOARecord();
		soaRecord.setPrimary(soaFields[0]);
		soaRecord.setHostmaster(soaFields[1]);
		try {
			soaRecord.setSerial(Integer.parseInt(soaFields[2]));
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		try {
			soaRecord.setRefresh(Integer.parseInt(soaFields[3]));
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		try {
			soaRecord.setRetry(Integer.parseInt(soaFields[4]));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		try {
			soaRecord.setExpire(Integer.parseInt(soaFields[5]));
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		try {
			soaRecord.setDefaultTTL(Integer.parseInt(soaFields[6]));
		} catch (NumberFormatException e){
			e.printStackTrace();
		}
		Contexts.getConversationContext().set("soaRecord", soaRecord);
		return "/soa.xhtml";
	}

	/**
	 * Edit the given record
	 * @param record
	 * @return
	 */
	public String edit(Record record){
		//validate the record
		if(!this.dnsValidator.isValid(record, this.domain.getId())){
			return null;
		}

		this.recordDataModel.update(record);
		this.recordDAO.incrementSOASerial(domain.getId());
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.RecordUpdated", record);
		return "/records.xhtml";
	}

	/**
	 * Cancel any edits to the given record
	 * @param record the record whose edit to cancel
	 * @return the view of the records form
	 */
	public String cancelEdit(Record record){
		this.recordDataModel.refresh(record);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.EditCancelled", record);
		return "/records.xhtml";
	}

	/**
	 * Edit the SOA record for the given zone
	 * @param soaRecord
	 * @return
	 */
	public String editSOA(SOARecord soaRecordFields){
		Record soaRecord = (Record) Contexts.getConversationContext().get("selectedRecord");
		soaRecord.setContent(soaRecordFields.toString());
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.SOAUpdated");
		return "/records.xhtml";
	}

	/**
	 * Delete the selected records
	 */
	public void deleteRecords(){
		Map<Record, Boolean> selectedRecords = this.recordDataModel.getSelectedRecords();
		Set<Record> records = selectedRecords.keySet();
		Iterator<Record> recordIterator = records.iterator();
		while(recordIterator.hasNext()){
			Record record = recordIterator.next();
			if(selectedRecords.get(record)){
				this.recordDataModel.delete(record);
				recordIterator.remove();
				this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.RecordDeleted", record);
			}
		}
		this.recordDAO.incrementSOASerial(this.domain.getId());
	}


}

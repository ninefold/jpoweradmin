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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

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
@Name("recordAction")
@Scope(ScopeType.CONVERSATION)
@Stateful
public class RecordActionImpl implements Serializable, RecordAction {
	private static final long serialVersionUID = -5443881545541066884L;
	@Logger
	private Log logger;

	@In
	private FacesMessages facesMessages;
	
	@RequestParameter
	private String zone;
	
	@DataModel
	private List<Record> records = new ArrayList<Record>();
	
	@Out(required=false)
	private SOARecord soaRecordFields = null;
	
	private Record soa = null;
	
	@Out(required=false)
	@DataModelSelection
	private Record selectedRecord;
	
	@In(create=true)
	private ZoneDAO zoneDAO;
	
	@In(required=true)
	private int userId;
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	private User user;
	
	private Domain domain;
	
	@In(create=true)
	private DNSValidator dnsValidator;
	
	@Out(required=false)
	private Map<Record, Boolean> selectedRecords = new LinkedHashMap<Record, Boolean>();
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#init()
	 */
	@Override
	@Create
	public void init() {
		this.user = this.entityManager.find(User.class, this.userId);
		
		if(this.zone == null){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "RecordAction.NoZone");
			this.logger.warn("User {0} tried to access a null zone", this.user.getUserName());
			throw new JPowerAdminException("Null Zone");
		}
		//get the domain object
		
		this.domain = this.zoneDAO.getDomain(this.zone);
		if(this.domain == null){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "RecordAction.NoZone");
			this.logger.warn("User {0} tried to access a null zone", this.user.getUserName());
			throw new JPowerAdminException("No such zone " + this.zone);
		}

		//check to see that the domain is indeed a valid user's domain
		if(!this.domain.getUser().equals(this.user)){
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "RecordAction.OtherDomains", this.user.getUserName());
			this.logger.warn("User {0} is accessing other users domains. Attempt has been logged", this.user.getUserName());
			throw new JPowerAdminException("Bad domain");
		}
		
		if(this.domain.getType().equals(Domain.Type.SLAVE)){
			this.facesMessages.addFromResourceBundle(Severity.ERROR, "RecordAction.SlaveDomainSelected", this.domain.getName());
			this.logger.warn("Slave zone {0} selected. Records cannot be listed", this.domain.getName());
			throw new JPowerAdminException("Slave zone selected");
		}
		
		if(this.domain.getParent() != null){
			throw new JPowerAdminException("Child zone selected");
		}
		
		//create the soa record
		this.soa =  this.zoneDAO.getSOARecord(this.domain.getName());
		
		if(this.soa == null){
			this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.NOSOA");
			this.logger.info("Zone {0} has no SOA record configured", this.domain.getName());
			throw new JPowerAdminException(this.domain.getName() + " has no SOA record");
		}
		
		//parse the soa record
		String content = this.soa.getContent();
		String[] soaString = content.split("\\s+");
		if(soaString.length != 7){
			throw new JPowerAdminException("BAD SOA");
		}
		
		this.soaRecordFields = new SOARecord();
		this.soaRecordFields.setPrimary(soaString[0]);
		this.soaRecordFields.setHostmaster(soaString[1]);
		this.soaRecordFields.setSerial(Integer.parseInt(soaString[2]));
		this.soaRecordFields.setRefresh(Integer.parseInt(soaString[3]));
		this.soaRecordFields.setRetry(Integer.parseInt(soaString[4]));
		this.soaRecordFields.setExpire(Integer.parseInt(soaString[5]));
		this.soaRecordFields.setDefaultTTL(Integer.parseInt(soaString[6]));
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#initRecords()
	 */
	@Override
	@Factory("records")
	public void initRecords(){
		this.records = this.zoneDAO.getRecords(this.domain.getName());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#addRecord(com.nicmus.pdns.Record)
	 */
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#addRecord(com.nicmus.pdns.entities.Record)
	 */
	@Override
	public void addRecord(Record record) {
		//add the domain if necessary
		if(record.getName().isEmpty() || record.getName() == null){
			record.setName(this.domain.getName());
		}
		//add the domain suffix if necessary
		if(!record.getName().toLowerCase().endsWith(this.domain.getName())){
			record.setName(record.getName().concat("." + this.domain.getName()));
		}
		if(this.records.contains(record)){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "RecordAction.RecordExists", record);
			return;
		}
		
		if(!this.dnsValidator.isValid(record, this.records)){
			return;
		}
		
		record.setDomain(this.domain);
		this.domain.getRecords().add(record);
		
		this.records.add(record);
		
		this.entityManager.persist(record);
		this.entityManager.persist(this.domain);
		
		this.incrementSOA();

		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.RecordAdded", record);
		record = (Record) Component.getInstance(Record.class, ScopeType.STATELESS);
		
		//FIXME: make it more efficient - i.e. do not recreate all the zones from scratch
		if(this.domain.getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(this.domain.getName());
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#editRecord(com.nicmus.pdns.Record)
	 */
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#edit()
	 */
	@Override
	public String edit() {
		int firstIndex = this.records.indexOf(this.selectedRecord);
		int lastIndex = this.records.lastIndexOf(this.selectedRecord);
		if(firstIndex != lastIndex){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "RecordAction.RecordConflict",this.selectedRecord);
			this.entityManager.refresh(this.selectedRecord);
			return "failure";
		}
		
		//try to edit the record
		if(!this.dnsValidator.isValid(this.selectedRecord, this.records)){
			this.entityManager.refresh(this.selectedRecord);
			return "failure";
		} 

		this.incrementSOA();
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.RecordUpdated", this.selectedRecord);
		this.entityManager.flush();
		
		//FIXME: make it more efficient - i.e. do not recreate all the zones from scratch
		if(this.domain.getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(this.domain.getName());
		}
		
		return "success";
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#editSOA()
	 */
	@Override
	public String editSOA(){
		//find the SOA record
		//save the domain
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.SOAUpdated");
		//find the soa record
		this.soa.setContent(this.soaRecordFields.toString());

		//FIXME: make it more efficient - i.e. do not recreate all the zones from scratch
		if(this.domain.getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(this.domain.getName());
		}

		return "success";
	}

	/**
	 * Cancel the ongoing edit
	 */
	@Override
	public String cancelEdit(){
		this.entityManager.refresh(this.selectedRecord);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.EditCancelled", this.selectedRecord);
		return "/records.xhtml";
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#deleteSelected()
	 */
	@Override
	public void deleteSelected() {
		this.incrementSOA();
		Set<Record> recordsToDelete = this.selectedRecords.keySet();
		Iterator<Record> recordsIterator = recordsToDelete.iterator();
		while(recordsIterator.hasNext()){
			Record toDeleteRecord = recordsIterator.next();
			if(this.selectedRecords.get(toDeleteRecord)){
				this.records.remove(toDeleteRecord);
				this.entityManager.remove(toDeleteRecord);
				recordsIterator.remove();
				this.facesMessages.addFromResourceBundle(Severity.INFO, "RecordAction.RecordDeleted", toDeleteRecord);
			}
		}
		
		//FIXME: make it more efficient - i.e. do not recreate all the zones from scratch
		if(this.domain.getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(this.domain.getName());
		}

		
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#getDomain()
	 */
	@Override
	public Domain getDomain() {
		return this.domain;
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#getSelected()
	 */
	@Override
	public List<Record> getSelected(){
		List<Record> toDelete = new ArrayList<Record>();
		Set<Record> selectedRecords = this.selectedRecords.keySet();
		for(Record r : selectedRecords){
			if(this.selectedRecords.get(r)){
				toDelete.add(r);
			}
		}
		return toDelete;
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.RecordAction#destroy()
	 */
	@Override
	@Remove
	@Destroy
	public void destroy(){
		this.logger.debug("Destroying {0}", RecordActionImpl.class.getName());
	}

	
	/**
	 * Increment the SOA record by 1
	 */
	private void incrementSOA(){
		this.soaRecordFields.setSerial(this.soaRecordFields.getSerial()+1);
		this.soa.setContent(this.soaRecordFields.toString());
	}

}

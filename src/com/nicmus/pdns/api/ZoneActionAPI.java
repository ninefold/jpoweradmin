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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import com.nicmus.pdns.DNSValidator;
import com.nicmus.pdns.ZoneDAO;
import com.nicmus.pdns.api.LinkRequest.Action;
import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Domain.Type;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

/**
 * Provide RESTFUL web services over HTTP as pertinent to zones
 * @author jsabev
 *
 */
@Name("zoneActionAPI")
@Path("/")
public class ZoneActionAPI {

	@In(create = true)
	private ApiDao apiDao;
	
	@In(create=true)
	private ZoneDAO zoneDAO;
	
	@In(create=true)
	private DNSValidator dnsValidator;
	
	@HeaderParam("X-JPowerAdmin-API-Key")
	private String apiKey;
	
	/**
	 * List all zones for a user as identified by the apiKey
	 * @param apiKey
	 * @return XML mark up for all zones the user has
	 */
	@GET
	@Path("/zones")
	@Produces("application/xml")
	public List<Domain> listZones(){
		User user = this.apiDao.getUser(this.apiKey);
		if(user == null){
			throw new WebApplicationException(new RuntimeException("Invalid api key"), 404);
		}
		return this.zoneDAO.getZones(user.getId());
	}

	/**
	 * Get the details of the requested zone
	 * @param zoneName The zone name
	 * @return the XML representation of the requested zone
	 * 
	 */
	@GET
	@Path("/zone/{zoneName}")
	@Produces("application/xml")
	public Domain getZone(@PathParam("zoneName") String zoneName){
		Domain domain = (Domain) this.apiDao.getDomain(zoneName);
		if(domain == null){
			throw new WebApplicationException(new RuntimeException("Invalid zone name"),404);
		}
		if(domain.getUser().getApiCode().equals(this.apiKey)){
			return domain;
		} else {
			throw new WebApplicationException(new RuntimeException("Invalid api key"),404);
		}
	}

	/**
	 * Add the given zone to the list of zones for the given user as identified
	 * by the API key
	 * @param domain
	 */
	@POST
	@Path("/zone")
	@Consumes("application/xml")
	public void addZone(Domain domain){
		User user = this.apiDao.getUser(this.apiKey);

		//make sure that the zone does not exist
		Domain existingDomain = this.zoneDAO.getDomain(domain.getName());

		if(existingDomain != null){
			throw new WebApplicationException(new RuntimeException("Domain exists"),409);
		}
		
		if(!this.dnsValidator.isValidFQDN(domain.getName())){
			throw new WebApplicationException(new RuntimeException("Invalid domain name"),409);
		}
		
		Domain newdomain = (Domain) Component.getInstance(Domain.class);
		newdomain.setName(domain.getName());
		newdomain.setType(domain.getType());
		newdomain.setMaster(domain.getMaster());
		newdomain.setNotifiedSerial(domain.getNotifiedSerial());
		user.getDomains().add(newdomain);
		newdomain.setUser(user);
		this.apiDao.saveObject(newdomain);

		if(domain.getType() == Type.MASTER){
			this.zoneDAO.createSOA(newdomain.getName());
		}
	}
	
	
	/**
	 * Edit the given zone.
	 * @param domain
	 */
	@PUT
	@Path("/zone")
	@Consumes("application/xml")
	public void editZone(Domain domain){
		Domain domainToUpdate = this.apiDao.getDomain(domain.getName());

		if(domainToUpdate == null){
			throw new WebApplicationException(404);
		}

		if(!domainToUpdate.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}

		Type newType = domain.getType();
		Type oldType = domainToUpdate.getType();

		domainToUpdate.setMaster(domain.getMaster());
		domainToUpdate.setNotifiedSerial(domain.getNotifiedSerial());
		domainToUpdate.setType(newType);

		if(oldType == Type.SLAVE && newType == Type.MASTER){
			this.zoneDAO.convertToMaster(domainToUpdate.getName());
		} 
	}
	
	/**
	 * Perform the linking of the given domains
	 * @param linkRequest
	 */
	@PUT
	@Path("/zone/link")
	@Consumes("application/xml")
	public void link(LinkRequest linkRequest){
		String parent = linkRequest.getParent();
		String child = linkRequest.getChild();
		Action action = linkRequest.getAction();
		
		Domain parentDomain = this.apiDao.getDomain(parent);
		Domain childDomain = this.apiDao.getDomain(child);
		if(parentDomain == null || childDomain == null){
			throw new WebApplicationException(new RuntimeException("No such zone"),404);
		}
		if(!parentDomain.getUser().getApiCode().equals(this.apiKey) || !childDomain.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(new RuntimeException("Invalid zone name"),409);
		}
		
	
		//do the linking/unlinking
		switch(action){
			case LINK:
				if(childDomain.getParent() !=null){
					throw new WebApplicationException(new RuntimeException("zone already linked"), 409);
				}
				if(parentDomain.getParent() !=null){
					throw new WebApplicationException(new RuntimeException("cannot link to a child zone"), 409);
				}
				parentDomain.getChildren().add(childDomain);
				childDomain.setParent(parentDomain);
				break;
			case UNLINK:
				parentDomain.getChildren().remove(childDomain);
				childDomain.setParent(null);
				break;
		}
		this.apiDao.saveObject(parentDomain);
		this.apiDao.saveObject(childDomain);
		if(action == Action.LINK){
			this.zoneDAO.updateChildrenDomains(parent);
		}
	}

	/**
	 * Get all children for the given parent domain name 
	 * @param zoneName
	 * @return list of children domains
	 */
	@GET
	@Path("/zone/{zoneName}/children")
	public List<Domain> getChildren(@PathParam("zoneName") String zoneName){
		Domain domain = this.apiDao.getDomain(zoneName);
		if(domain == null){
			throw new WebApplicationException(new RuntimeException("No such zone"), 404);
		}
		if(!domain.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(new RuntimeException("Invalid api key/zoneName"),404);
		}
		if(domain.getParent() != null){
			throw new WebApplicationException(new RuntimeException("Not a parent domain"),404);
		}
		return new ArrayList<Domain>(domain.getChildren());
	}
	
	/**
	 * Delete the given zone
	 * @param zoneName
	 */
	@DELETE
	@Path("/zone/{zoneName}")
	public void delete(@PathParam("zoneName") String zoneName){
		Domain domain = this.apiDao.getDomain(zoneName);
		if(domain.getUser().getApiCode().equals(this.apiKey)){
			this.apiDao.deleteObject(domain);
		} else {
			throw new WebApplicationException(404);
		}
	}
	
	
	/**
	 * Get all records for the given zone
	 * @param zoneName zone name
	 * @return List of zone records
	 */
	@GET
	@Path("/zone/{zoneName}/records")
	@Produces("application/xml")
	public List<Record> getRecords(@PathParam("zoneName") String zoneName){
		Domain domain = this.apiDao.getDomain(zoneName);
		if(domain == null){
			throw new WebApplicationException(404);
		}
		if(!domain.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}
		return this.zoneDAO.getRecords(domain.getName());
	}

	
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	@GET
	@Path("/zone/{zoneName}/record/{id}")
	@Produces("application/xml")
	public Record getRecord(@PathParam("id") int id){
		Record record = this.apiDao.getRecord(id);
		if(record == null){
			throw new WebApplicationException(404);
		}
		if(!record.getDomain().getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}
		return record;
	}

	/**
	 * 
	 * @param record
	 * @param zoneName
	 * @return
	 */
	@POST
	@Path("/zone/{zoneName}")
	@Consumes("application/xml")
	@Produces("text/plain")
	public int createRecord(Record record, @PathParam("zoneName") String zoneName){
		Domain domain = this.apiDao.getDomain(zoneName);
		
		if(domain == null){
			throw new WebApplicationException(404);
		}
		
		if(!domain.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}
		
		//only create record if not in a child zone
		if(domain.getParent() != null){
			throw new WebApplicationException(new RuntimeException("Cannot create records in a child zone."),409);
		}
		
		//create the record
		// if(!record.getName().toLowerCase().endsWith(domain.getName().toLowerCase())){
		// 	record.setName(record.getName().concat("." + domain.getName()));
		// }
		
		Set<Record> records = domain.getRecords();
		//verify the record
		if(!this.dnsValidator.isValid(record, records)){
			throw new WebApplicationException(400);
		}

		//make sure that the record does not exist already
		if(domain.getRecords().contains(record)){
			throw new WebApplicationException(409);
		}
		
		Record newRecord = new Record();
		newRecord.setName(record.getName());
		newRecord.setContent(record.getContent());
		newRecord.setType(record.getType());
		newRecord.setTtl(record.getTtl());
		newRecord.setPrio(record.getPrio());
		
		domain.getRecords().add(newRecord);
		newRecord.setDomain(domain);

		domain.getRecords().add(record);
		record.setDomain(domain);
		
		this.apiDao.saveObject(newRecord);
		
		//verify if we are creating a record in a parent zone
		if(domain.getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(zoneName);
		}
		
		return newRecord.getId();
	}
	
	/**
	 * 
	 * @param record
	 * @param zoneName
	 */
	@PUT
	@Path("/zone/{zoneName}")
	@Consumes("application/xml")
	public void updateRecord(Record record, @PathParam("zoneName") String zoneName){
		Domain domain = this.apiDao.getDomain(zoneName);
		
		if(domain == null){
			throw new WebApplicationException(404);
		}
		
		if(!domain.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}

		//verify that the record does not belong to a child zone
		if(domain.getParent() != null){
			throw new WebApplicationException(new RuntimeException("Cannot update record of a child zone"),409);
		}
		
		if(!record.getName().toLowerCase().endsWith(zoneName.toLowerCase())){
			record.setName(record.getName().concat("." + zoneName));
		}


		//make sure the new record is valid
		Set<Record> records = domain.getRecords();
		if(!this.dnsValidator.isValid(record, records)){
			throw new WebApplicationException(400);
		}
		
		//make sure that the edited record does not clash an existing one
		if(domain.getRecords().contains(record)){
			throw new WebApplicationException(409);
		}
		
		Record recordToUpdate = (Record) this.apiDao.getRecord(record.getId());
		
		if(recordToUpdate == null){
			throw new WebApplicationException(404);
		}
		
		if(!recordToUpdate.getDomain().getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}
		
		recordToUpdate.setName(record.getName());
		recordToUpdate.setContent(record.getContent());
		recordToUpdate.setType(record.getType());
		recordToUpdate.setTtl(record.getTtl());
		recordToUpdate.setPrio(record.getPrio());
		
		this.apiDao.saveObject(recordToUpdate);
		//update the children if necessary
		if(domain.getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(zoneName);
		}
		
	}
	
	/**
	 * 
	 * @param id
	 */
	@DELETE
	@Path("/zone/{zoneName}/record/{id}")
	public void deleteRecord(@PathParam("id") int id){
		Record record = this.apiDao.getRecord(id);
		if(record == null){
			throw new WebApplicationException(404);
		}
		
		if(record.getDomain().getParent() != null){
			throw new WebApplicationException(new RuntimeException("Cannot delete a record from a child zone"),409);
		}
		
		if(record.getDomain().getUser().getApiCode().equals(this.apiKey)){
			this.apiDao.deleteObject(record);
		}
		
		if(record.getDomain().getChildren().size() > 0){
			this.zoneDAO.updateChildrenDomains(record.getDomain().getName());
		}
		
	}
}

package com.nicmus.pdns.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.DNSValidator;
import com.nicmus.pdns.dao.RecordDAO;
import com.nicmus.pdns.dao.ZoneDAO;
import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Domain.Type;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;


@Name("zoneActionAPI")
@Path("/")
public class ZoneActionAPI {

	@Logger
	private Log logger;

	private static final int MAX_RESULTS = 1000;

	@In(create=true)
	private DNSValidator dnsValidator;

	@In(create=true)
	private ZoneDAO zoneDAO;

	@In(create=true)
	private ApiDao apiDao;

	@In(create=true)
	private RecordDAO recordDAO;

	@HeaderParam("X-JPowerAdmin-API-Key")
	private String apiKey;

	/**
	 * Get the total number of zones for the given account
	 * @return
	 */
	@GET
	@Path("/zones/count")
	@Produces("text/plain")
	public int getNumZones(){
		return this.apiDao.getNumZones(this.apiKey);
	}

	/**
	 * Get the number of DNS records for the given zone
	 * @param zoneName
	 * @return number of DNS records for the given zone
	 */
	@GET
	@Path("/zone/{zoneName}/count")
	public int getNumRecords(@PathParam("zoneName") String zoneName){
		return this.apiDao.getNumRecords(this.apiKey, zoneName);
	}

	/**
	 * List all zones for a user as identified by the apiKey
	 * @param offset of the record
	 * @param count the number of results
	 * @return XML markkup for all zones the user has
	 */
	@GET
	@Path("/zones")
	@Produces("application/xml")
	public List<Domain> listZones(@QueryParam("offset") int offset, @QueryParam("count") int count){
		if(offset < 0){
			offset = 0;
		}
		if(count > MAX_RESULTS || count == 0){
			count = MAX_RESULTS;
		}
		return this.apiDao.getDomains(this.apiKey, offset, count);
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
		Domain domain = this.apiDao.getDomain(zoneName);
		if(domain.getUser().getApiCode().equals(apiKey)){
			return domain;
		} else {
			throw new WebApplicationException(404);
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
		Domain existingDomain = this.apiDao.getDomain(domain.getName());

		if(existingDomain != null){
			//check if the domain belongs to the given user and is deleted.
			//if it is, then mark it as non deleted.
			throw new WebApplicationException(409);
		} else {
			if(!this.dnsValidator.isValidFQDN(domain.getName())){
				throw new WebApplicationException(400);
			}
			Domain newdomain = (Domain) Component.getInstance(Domain.class);
			newdomain.setName(domain.getName().trim().toLowerCase());
			newdomain.setType(domain.getType());
			newdomain.setMaster(domain.getMaster());
			newdomain.setNotifiedSerial(domain.getNotifiedSerial());

			newdomain.setUser(user);
			this.apiDao.saveObject(newdomain);
			this.apiDao.updateObject(user);

			if(domain.getType() == Type.MASTER){
				this.zoneDAO.createSOA(newdomain.getName());
			}
		}
	}

	/**
	 * Edit the given zone
	 * @param domain
	 */
	@PUT
	@Path("/zone")
	@Consumes("application/xml")
	public void editZone(Domain domain){

		Domain domainToUpdate = this.apiDao.getDomain(domain.getName());
		Type newType = domain.getType();
		Type oldType = domainToUpdate.getType();

		if(!domainToUpdate.getUser().getApiCode().equals(apiKey)){
			throw new WebApplicationException(404);
		}

		domainToUpdate.setMaster(domain.getMaster());
		domainToUpdate.setNotifiedSerial(domain.getNotifiedSerial());
		domainToUpdate.setType(newType);

		this.apiDao.updateObject(domainToUpdate);

		if(newType != oldType && newType == Type.MASTER){
			this.zoneDAO.convertToMaster(domainToUpdate.getName());
		}

	}

	/**
	 * Delete the given zone
	 * @param apiKey
	 * @param zoneName
	 */
	@DELETE
	@Path("/zone/{zoneName}")
	public void delete(@PathParam("zoneName") String zoneName){
		Domain domain = this.apiDao.getDomain(zoneName);
		if(domain == null){
			throw new WebApplicationException(404);
		}
		this.apiDao.deleteObject(Domain.class,domain.getId());

	}

	/**
	 * Get all records for the given zone
	 * @param zoneName zone name
	 * @return List of zone records
	 */
	@GET
	@Path("/zone/{zoneName}/records")
	@Produces("application/xml")
	public List<Record> getRecords(@PathParam("zoneName") String zoneName, @QueryParam("offset") int offset, @QueryParam("count") int count){
		Domain domain = this.zoneDAO.getDomain(zoneName);
		if(domain == null){
			throw new WebApplicationException(404);
		}
		if(!domain.getUser().getApiCode().equals(this.apiKey)){
			throw new WebApplicationException(404);
		}

		if(offset < 0){
			offset = 0;
		}

		if(count == 0 || count > 1000){
			count = 1000;
		}

		return this.zoneDAO.getRecords(domain.getName(), offset, count);
	}

	/**
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
		if(!record.getDomain().getUser().getApiCode().equals(apiKey)){
			throw new WebApplicationException(404);
		}
		return record;
	}

	/**
	 * Create the given record
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

		if(!domain.getUser().getApiCode().equals(apiKey)){
			throw new WebApplicationException(404);
		}

		//create the record
		//if(!record.getName().toLowerCase().endsWith(domain.getName().toLowerCase())){
		//	record.setName(record.getName().concat("." + domain.getName()));
		//}

		//verify the record
		if(!this.dnsValidator.isValid(record,domain.getId())){
			throw new WebApplicationException(400);
		}


		Record newRecord = (Record) Component.getInstance(Record.class);
		newRecord.setName(record.getName());
		newRecord.setContent(record.getContent());
		newRecord.setType(record.getType());
		newRecord.setTtl(record.getTtl());
		newRecord.setPrio(record.getPrio());
		newRecord.setDomain(domain);
		this.apiDao.saveObject(newRecord);
		this.recordDAO.incrementSOASerial(domain.getId());
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

		if(!domain.getUser().getApiCode().equals(apiKey)){
			throw new WebApplicationException(404);
		}

		if(!record.getName().toLowerCase().endsWith(zoneName.toLowerCase())){
			record.setName(record.getName().concat("." + zoneName));
		}


		//verfiy the record is correct
		if(!(this.dnsValidator.isValid(record, domain.getId()))){
			throw new WebApplicationException(400);
		}

		this.logger.debug("Loading record {0}", record.getId());
		Record recordToUpdate = this.apiDao.getRecord(record.getId());

		if(recordToUpdate == null){
			throw new WebApplicationException(404);
		}

		if(!recordToUpdate.getDomain().getUser().getApiCode().equals(apiKey)){
			throw new WebApplicationException(404);
		}

		recordToUpdate.setName(record.getName());
		recordToUpdate.setContent(record.getContent());
		recordToUpdate.setType(record.getType());
		recordToUpdate.setTtl(record.getTtl());
		recordToUpdate.setPrio(record.getPrio());

		this.apiDao.updateObject(recordToUpdate);
		this.recordDAO.incrementSOASerial(domain.getId());
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

		if(record.getDomain().getUser().getApiCode().equals(apiKey)){
			this.apiDao.deleteObject(Record.class, id);
		}
	}

}
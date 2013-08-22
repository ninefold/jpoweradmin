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
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;

@Name("zoneDAO")
@Stateless
public class ZoneDAOImpl implements ZoneDAO, Serializable {
	private static final long serialVersionUID = 3420522065230443823L;

	@PersistenceContext
	private EntityManager entityManager;

	@Logger
	private Log logger;

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.ZoneDAO#getZones(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Domain> getZones(int userId) {
		String sql = "from Domain d where d.user.id = :userId";
		Query zonesQuery = this.entityManager.createQuery(sql);
		zonesQuery.setParameter("userId", userId);
		return zonesQuery.getResultList();	
	}

	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.ZoneDAO#getDomain(java.lang.String)
	 */
	@Override
	public Domain getDomain(String domainName) {
		Query domainQuery = this.entityManager.createQuery("from Domain d where d.name = :domainName");
		domainQuery.setParameter("domainName", domainName);
		try {
			Domain domain = (Domain) domainQuery.getSingleResult();
			return domain;
		} catch (NoResultException e){
			//silently fail
		}
		return null;
	}

	

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.ZoneDAO#createSOA(int)
	 */
	@Override
	public void createSOA(String domainName) {

		//load the domain
		Query domainQuery = this.entityManager.createQuery("from Domain d where d.name = ?");
		domainQuery.setParameter(1, domainName);
		Domain domain = (Domain)domainQuery.getSingleResult();
		
		SOARecord soaRecordFields = (SOARecord) Component.getInstance(SOARecord.class, ScopeType.STATELESS);
		soaRecordFields.setHostmaster("hostmaster.".concat(domainName));

		//create the soa record for the zone
		Record soa = (Record) Component.getInstance(Record.class, ScopeType.STATELESS);
		soa.setType(Record.Type.SOA);
		soa.setName(domainName);
		soa.setContent(soaRecordFields.toString());
		soa.setDomain(domain);
		domain.getRecords().add(soa);

		this.entityManager.persist(soa);
		
		//create the two ns records
		Record ns1 = (Record) Component.getInstance("primaryNS", ScopeType.STATELESS);
		Record ns2 = (Record) Component.getInstance("secondaryNS", ScopeType.STATELESS);
		Record ns3 = (Record) Component.getInstance("thernaryNS", ScopeType.STATELESS);
		
		ns1.setName(domainName);
		ns2.setName(domainName);
		ns3.setName(domainName);
		
		ns1.setDomain(domain);
		ns2.setDomain(domain);
		ns3.setDomain(domain);
		
		domain.getRecords().add(ns1);
		domain.getRecords().add(ns2);
		domain.getRecords().add(ns3);
		
		this.entityManager.persist(ns1);
		this.entityManager.persist(ns2);
		this.entityManager.persist(ns3);
		this.entityManager.persist(domain);
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.ZoneDAO#convertToMaster(com.nicmus.pdns.entities.Domain)
	 */
	@Override
	public void convertToMaster(String domainName) {
		Domain domain = this.getDomain(domainName);
		//delete the SOA record for the given domain
		Query deleteRecordsQuery = this.entityManager.createQuery("delete from Record r where r.type = :type and r.domain = :domain");
		deleteRecordsQuery.setParameter("domain", domain);
		deleteRecordsQuery.setParameter("type", Record.Type.SOA);
		deleteRecordsQuery.executeUpdate();
		this.logger.debug("Deleted SOA Record for domain {0}", domainName);
		//delete the NS records for the given domain
		deleteRecordsQuery.setParameter("type", Record.Type.NS);
		deleteRecordsQuery.executeUpdate();
		this.logger.debug("Deleted NS Records for domain {0}", domainName);
		this.createSOA(domainName);
		
		//update all the records
		Query updateRecordsQuery = this.entityManager.createQuery("update Record r set r.dateCreated= :now, r.dateModified = :now where r.domain = :domain");
		updateRecordsQuery.setParameter("now", new Date());
		updateRecordsQuery.setParameter("domain", domain);
		updateRecordsQuery.executeUpdate();
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.ZoneDAO#getRecords(com.nicmus.pdns.entities.Domain)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Record> getRecords(String domainName) {
		Query recordsQuery = this.entityManager.createQuery("from Record r where r.domain.name = :domainName");
		recordsQuery.setParameter("domainName", domainName);
		return recordsQuery.getResultList();
	}



	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.ZoneDAO#getSOARecord(com.nicmus.pdns.entities.Domain)
	 */
	@Override
	public Record getSOARecord(String domainName) {
		Query soaRecordQuery = this.entityManager.createQuery("from Record r where r.domain.name = :domainName and r.type = :type");
		soaRecordQuery.setParameter("domainName", domainName);
		soaRecordQuery.setParameter("type", Record.Type.SOA);
		
		try {
			Record soaRecord = (Record) soaRecordQuery.getSingleResult();
			return soaRecord;
		} catch (NoResultException e){
			//silently fail;
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneDAO#updateChildrenDomains(java.lang.String)
	 */
	@Override
	public void updateChildrenDomains(String parentDomainName){
		Domain parentDomain = this.getDomain(parentDomainName);
		String sql = "delete from Record r where r.domain.id = ?";
		Query deleteRecordsQuery = this.entityManager.createQuery(sql);
		List<Record> parentRecords = this.getRecords(parentDomainName);
		Set<Domain> childrenDomains = parentDomain.getChildren();
		for(Domain d : childrenDomains){
			deleteRecordsQuery.setParameter(1, d.getId());
			deleteRecordsQuery.executeUpdate();
			//copy the record over
			for(Record r : parentRecords){
				String name = r.getName();
				int parentRecordNameIndex = name.lastIndexOf(parentDomainName);
				String newname = name.substring(0, parentRecordNameIndex).concat(d.getName());
				Record newRecord = new Record();
				newRecord.setName(newname);
				newRecord.setType(r.getType());
				newRecord.setContent(r.getContent());
				newRecord.setPrio(r.getPrio());
				newRecord.setTtl(r.getTtl());
				d.getRecords().add(newRecord);
				newRecord.setDomain(d);
				this.entityManager.persist(newRecord);
				this.entityManager.persist(d);
			}
		}
	}

}

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
package com.nicmus.pdns.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import com.nicmus.pdns.JPowerAdminException;
import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.Record.Type;

/**
 * @author jsabev
 *
 */
@Name("recordDAO")
@Stateless
public class RecordDAOImpl implements RecordDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.RecordDAO#createRecord(com.nicmus.pdns.entities.Record, int)
	 */
	@Override
	public void createRecord(Record record, int domainId) {
		Domain domain = this.entityManager.find(Domain.class, domainId);
		record.setDomain(domain);
		this.entityManager.persist(domain);
		this.entityManager.persist(record);
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.RecordDAO#findRecord(java.lang.String, java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean recordExists(String name, String content, Type type, int domainId) {
		Query recordQuery = this.entityManager.createQuery("from Record r where r.name = :name and r.type = :type and r.content= :content and r.domain.id = :domainId");
		recordQuery.setParameter("name", name);
		recordQuery.setParameter("type", type);
		recordQuery.setParameter("content", content);
		recordQuery.setParameter("domainId", domainId);
		List<Record> resultList = recordQuery.getResultList();
		if(resultList.size() > 0){
			return true;
		}
		return false;
	}

	
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.RecordDAO#recordNameExists(java.lang.String, com.nicmus.pdns.entities.Record.Type)
	 */
	@Override
	public boolean recordNameExists(String name, Type type, int domainId) {
		String sql = "select r.name from Record r where r.name = :name and r.type = :type and r.domain.id = :domainId";
		Query recordQuery = this.entityManager.createQuery(sql);
		recordQuery.setParameter("name", name);
		recordQuery.setParameter("type", type);
		recordQuery.setParameter("domainId", domainId);
		return recordQuery.getResultList().size() > 0;
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.RecordDAO#recordContentExists(java.lang.String, com.nicmus.pdns.entities.Record.Type)
	 */
	@Override
	public boolean recordContentExists(String content, Type type, int domainId) {
		String sql = "select r.id from Record r where r.content = :content and r.type = :type and r.domain.id = :domainId";
		Query recordQuery = this.entityManager.createQuery(sql);
		recordQuery.setParameter("content", content);
		recordQuery.setParameter("type", type);
		recordQuery.setParameter("domainId", domainId);
		return recordQuery.getResultList().size() > 0;
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.dao.RecordDAO#incrementSOASerial(int)
	 */
	@Override
	public void incrementSOASerial(int domainId) {
		Query soaQuery = this.entityManager.createQuery("from Record r where r.domain.id = :domainId and r.type = :type");
		soaQuery.setParameter("domainId", domainId);
		soaQuery.setParameter("type", Type.SOA);
		try {
			Record record= (Record) soaQuery.getSingleResult();
			String content = record.getContent();
			String[] fields = content.split("\\s+");
			try {
				int parseInt = Integer.parseInt(fields[2]);
				parseInt = parseInt + 1;
				fields[2] = "" + parseInt;
				String soa = "";
				for(int i = 0; i < fields.length; i++){
					soa += fields[i] + " ";
				}
				soa = soa.trim();
				record.setContent(soa);
				this.entityManager.persist(record);
			} catch (IndexOutOfBoundsException e){
				throw new JPowerAdminException("Cannot find SOA serial");
			} catch (NumberFormatException e){
				throw new JPowerAdminException("Cannot parse SOA serial");
			}
		} catch (NoResultException e){
			throw new JPowerAdminException("No SOA record for domain id: " + domainId);
		} catch (NonUniqueResultException e){
			throw new JPowerAdminException("Multiple SOA records for domain id: "  + domainId);
		}
	}


	
	
	
}

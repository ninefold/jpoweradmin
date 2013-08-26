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
package com.nicmus.pdns.api;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

@Name("apiDao")
@Stateless
public class ApiDAOImpl implements ApiDao {

	@PersistenceContext
	private EntityManager enityManager;

	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#getNumZones(java.lang.String)
	 */
	@Override
	public int getNumZones(String apiCode) {
		String sql = "select count(d.id) from Domain d where d.user.apiCode = :apiCode";
		Query numRecordsQuery = this.enityManager.createQuery(sql);
		numRecordsQuery.setParameter("apiCode", apiCode);
		return ((Long) numRecordsQuery.getSingleResult()).intValue();
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#getDomains(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Domain> getDomains(String apiCode, int offset, int count) {
		String sql = "from Domain d where d.user.apiCode = :apiCode";
		Query domainQuery = this.enityManager.createQuery(sql);
		domainQuery.setParameter("apiCode", apiCode);
		domainQuery.setFirstResult(offset);
		domainQuery.setMaxResults(count);
		return domainQuery.getResultList();
	}

		/* (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#getNumRecords(java.lang.String)
	 */
	@Override
	public int getNumRecords(String apiCode, String zoneName) {
		String sql = "select count(r.id) from Record r where r.domain.name = :name and r.domain.user.apiCode = :apiCode";
		Query numRecordsQuery = this.enityManager.createQuery(sql);
		numRecordsQuery.setParameter("name", zoneName);
		numRecordsQuery.setParameter("apiCode", apiCode);
		return ((Long)numRecordsQuery.getSingleResult()).intValue();
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String apiKey) {
		Query userQuery = this.enityManager.createQuery("from User u where u.apiCode = ?");
		userQuery.setParameter(1, apiKey);
		return (User)userQuery.getSingleResult();
	}
	
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#getDomain(java.lang.String)
	 */
	@Override
	public Domain getDomain(String domainName) {
		Query domainQuery = this.enityManager.createQuery("from Domain d where d.name = ?");
		domainQuery.setParameter(1, domainName);
		try {
			return (Domain) domainQuery.getSingleResult();
		} catch (NoResultException e){
			return null;
		}
	}



	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#getRecord(int)
	 */
	@Override
	public Record getRecord(int recordId) {
		return this.enityManager.find(Record.class, recordId);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#saveObject(java.lang.Object)
	 */
	@Override
	public void saveObject(Object obj) {
		this.enityManager.persist(obj);
	}
	

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#updateObject(java.lang.Object)
	 */
	@Override
	public void updateObject(Object obj){
		this.enityManager.merge(obj);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#deleteObject(java.lang.Object)
	 */
	@Override
	public void deleteObject(Class<?extends Object> clazz, int recordId) {
		this.enityManager.remove(this.enityManager.find(clazz, recordId));
	}

	
}

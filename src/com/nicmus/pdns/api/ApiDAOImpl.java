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

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;
import com.nicmus.pdns.entities.User;

/**
 * Wrapper DAO class around the entity manager. This is necessary as the RESTEASY
 * implementation does not support stateful EJBs.
 * @author jsabev
 *
 */
@Name("apiDao")
@Stateful
public class ApiDAOImpl implements ApiDao {

	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager enityManager;
	
	@Logger
	private Log logger;
	
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
	 * @see com.nicmus.pdns.api.ApiDao#deleteObject(java.lang.Object)
	 */
	@Override
	public void deleteObject(Object obj) {
		this.enityManager.remove(obj);
	}


	/* (non-Javadoc)
	 * @see com.nicmus.pdns.api.ApiDao#destroy()
	 */
	@Override
	@Destroy
	@Remove
	public void destroy() {
		this.logger.debug("Destroying {0}", ApiDAOImpl.class.getName());
	}

	
}

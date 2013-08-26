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
package com.nicmus.pdns.dao;

import java.io.Serializable;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Record;

@Name("recordPaginationDAO")
@Scope(ScopeType.CONVERSATION)
@Stateful
public class RecordPaginationDAO implements PaginationDAO<Record>, Serializable {
	private static final long serialVersionUID = -3384101787476311329L;
	
	@Logger
	private Log logger;
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	@In(required=true)
	private Domain domain;
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#getNumRecords()
	 */
	@Override
	public int getNumRecords() {
		//FIXME: INVESTIGATE THE SQL AND THE FORM SUBMISSION - DO WE NEED TO PASS THE userId?
		String sql = "select count(r.id) from Record r where r.domain.user.id = :userId and r.domain.id = :domainId";
		Query recordsQuery = this.entityManager.createQuery(sql);
		recordsQuery.setParameter("userId", this.domain.getUser().getId());
		recordsQuery.setParameter("domainId", this.domain.getId());
		return ((Long) recordsQuery.getSingleResult()).intValue();
	}

	@Override
	public int getNumRecords(String filterBy, String filterValue) {
		Session session = (Session) this.entityManager.getDelegate();
		Criteria recordCriteria = session.createCriteria(Record.class);
		recordCriteria.createCriteria("domain").add(Restrictions.idEq(this.domain.getId()));
		if(filterBy != null && 
			filterValue != null && 
			!filterBy.isEmpty() &&
			!filterValue.isEmpty()){
			recordCriteria.add(Restrictions.like(filterBy, filterValue.concat("%")));
		}
		recordCriteria.setProjection(Projections.rowCount());
		return ((Long) recordCriteria.list().get(0)).intValue();
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#getRecord(int)
	 */
	@Override
	public Record getRecord(int id) {
		return this.entityManager.find(Record.class, id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#findObjects(int, int, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public List<Record> findObjects(int offset, int batchSize, String filterColumn, String filterValue, String sortField,boolean desc) {
		Session session = (Session) this.entityManager.getDelegate();
		Criteria recordCriteria = session.createCriteria(Record.class);
		recordCriteria.createCriteria("domain").add(Restrictions.idEq(this.domain.getId()));
		recordCriteria.setFirstResult(offset);
		recordCriteria.setFetchSize(batchSize);
		recordCriteria.setMaxResults(batchSize);
		
		//deal with the filtering
		if(filterColumn != null && !filterColumn.isEmpty() &&
				filterValue != null && !filterValue.isEmpty()){
			recordCriteria.add(Restrictions.like(filterColumn, filterValue.concat("%")));
		}
		
		//deal with the sorting
		if(sortField != null && !sortField.isEmpty()){
			if(desc){
				recordCriteria.addOrder(Property.forName(sortField).desc());
			} else {
				recordCriteria.addOrder(Property.forName(sortField).asc());
			}
		}
		return recordCriteria.list();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#update(java.lang.Object)
	 */
	@Override
	public void update(Record record) {
		this.entityManager.persist(record);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#refresh(java.lang.Object)
	 */
	@Override
	public void refresh(Record record) {
		this.entityManager.refresh(record);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#delete(java.lang.Object)
	 */
	@Override
	public void delete(Record remove) {
		this.entityManager.remove(remove);
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.dao.PaginationDAO#destroy()
	 */
	@Remove
	@Destroy
	@Override
	public void destroy() {
		this.logger.debug("Destroying {0}", RecordPaginationDAO.class.getName());
	}

}

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
package com.nicmus.pdns.fancyrecords;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import com.nicmus.pdns.entities.Record;

@Stateless(name="UrlSearcher")
public class UrlSearcherImpl implements URLSearcher {
	@PersistenceContext(name="entityManager")
	private EntityManager entityManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.fancyrecords.URLSearcher#getURLToRedirectTo(java.lang.String)
	 */
	public String getURLToRedirectTo(String recordName) {
		Query recordURLQuery = this.entityManager.createQuery("select r.content from Record r where r.name = :recordName and r.type = :recordType");
		recordURLQuery.setParameter("recordName", recordName);
		recordURLQuery.setParameter("recordType", Record.Type.URL);
		try {
			String content = (String) recordURLQuery.getSingleResult();
			return content;
		} catch (NoResultException e){
			
		} catch (NonUniqueResultException e){
			
		}
		
		//get just the domain name
		int lastIndexchar = recordName.lastIndexOf(".");
		
		if(lastIndexchar == -1){
			return null;
		}
		
		//search backwards for the first occurrence of .
		while(lastIndexchar > 0 && recordName.charAt(--lastIndexchar) != '.');
		
		recordName = "*" + recordName.substring(lastIndexchar);
		recordURLQuery.setParameter("recordName", recordName);
		try {
			String content = (String)recordURLQuery.getSingleResult();
			return content;
		} catch (NonUniqueResultException e){
			
		} catch (NoResultException e){
			
		}
		
		
		return null;
	}

}

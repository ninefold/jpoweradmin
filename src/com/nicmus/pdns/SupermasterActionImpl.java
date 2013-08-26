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
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.Supermaster;

/**
 * @author jsabev
 *
 */
@Name("superMasterAction")
@Scope(ScopeType.CONVERSATION)
@Stateful
public class SupermasterActionImpl implements Serializable, SupermasterAction {
	private static final long serialVersionUID = -8545623194412605569L;
	
	@In
	private FacesMessages facesMessages;
	
	@DataModel
	private List<Supermaster> supermasters;
	
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	@Logger
	private Log logger;
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.SupermasterAction#initSupermasters()
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Factory("supermasters")
	public void initSupermasters(){
		this.supermasters = this.entityManager.createQuery("from Supermaster s").getResultList();
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.SupermasterAction#add(com.nicmus.pdns.entities.Supermaster)
	 */
	@Override
	public void add(Supermaster supermaster){
		if(!this.supermasters.contains(supermaster)){
			this.supermasters.add(supermaster);
			this.entityManager.persist(supermaster);
			this.facesMessages.addFromResourceBundle(Severity.INFO, "SuperMasterService.SupermasterAdded", supermaster.getIp());
			supermaster = (Supermaster)Component.getInstance(Supermaster.class, ScopeType.STATELESS);
		} else {
			this.facesMessages.addFromResourceBundle(Severity.WARN, "SuperMasterService.SupermasterExists", supermaster.getIp());
		}
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.SupermasterAction#delete(com.nicmus.pdns.entities.Supermaster)
	 */
	@Override
	public void delete(Supermaster supermaster){
		this.supermasters.remove(supermaster);
		this.entityManager.remove(supermaster);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "SuperMasterService.SupermasterDeleted", supermaster.getIp());
	}
	
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.SupermasterAction#destroy()
	 */
	@Override
	@Remove
	@Destroy
	public void destroy(){
		this.logger.debug("Destroying {0}", SupermasterActionImpl.class.getName());
	}
}

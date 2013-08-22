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
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.Domain.Type;
import com.nicmus.pdns.entities.User;

@Name("zoneAction")
@Scope(ScopeType.CONVERSATION)
@Stateful
public class ZoneActionImpl implements Serializable, ZoneAction {
	private static final long serialVersionUID = 814728607058759517L;

	@In(create=true)
	private ZoneDAO zoneDAO;
	
	@DataModel
	private List<Domain> zones = new ArrayList<Domain>();
	
	@DataModelSelection
	@Out(required=false)
	private Domain selectedDomain;
	
	@In
	private FacesMessages facesMessages;
	
	@Out(required=false)
	private Map<Domain, Boolean> selectedDomains = new LinkedHashMap<Domain, Boolean>();
	
	@In(value="dnsValidator")
	private DNSValidator dnsValidator;
	
	@In(required=true)
	private int userId;
	
	@In(required=false)
	private List<Domain> parentDomains = new ArrayList<Domain>();
	
	@PersistenceContext(type=PersistenceContextType.EXTENDED)
	private EntityManager entityManager;
	
	@Logger
	private Log logger;
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#initZones()
	 */
	@Override
	@Factory("zones")
	public void initZones() {
		this.zones = this.zoneDAO.getZones(this.userId);
	}


	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#createZone(com.nicmus.pdns.entities.Domain)
	 */
	@Override
	public void createZone(Domain domain, String parentDomainName) {
		User user = this.entityManager.find(User.class, this.userId);
		//validate the zone
		if(!this.dnsValidator.isValidFQDN(domain.getName())){
			return;
		}

		if(this.zones.contains(domain)){
			this.facesMessages.addFromResourceBundle(Severity.WARN, "ZoneAction.ZoneExists", domain.getName());
			return;
		}
		
		//check to see if zone exists in the database
		Domain existingDomain = this.zoneDAO.getDomain(domain.getName());
		if(existingDomain == null){
			//no such domain - can be safely added
			user.getDomains().add(domain);
			domain.setUser(user);

			this.zones.add(domain);

			this.entityManager.persist(domain);
			this.entityManager.persist(user);
			
			this.zoneDAO.createSOA(domain.getName());
			
			//check to see if we have a parent domain
			if(!parentDomainName.isEmpty() && domain.getType() != Domain.Type.SLAVE){
				Domain parentDomain = this.zoneDAO.getDomain(parentDomainName);
				if(parentDomain == null){
					this.facesMessages.addFromResourceBundle(Severity.ERROR, "ZoneAction.NoSuchParentDomain", parentDomainName);
					return;
				}
				
				if(parentDomain.getParent() != null){
					this.facesMessages.addFromResourceBundle(Severity.ERROR, "ZoneAction.InvalidParentDomain", parentDomainName);
					return;
				}
				
				if(parentDomain.getUser().getId() != this.userId){
					throw new JPowerAdminException("Invalid parent domain");
				}
				parentDomain.getChildren().add(domain);
				domain.setParent(parentDomain);
				this.entityManager.persist(parentDomain);
				this.entityManager.persist(domain);
				this.zoneDAO.updateChildrenDomains(parentDomainName);
				this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ParentDomainLinked", parentDomainName);
				//refresh all the zones - to get the correct number of records displayed
				for(Domain d : this.zones){
					this.entityManager.refresh(d);
				}
				parentDomainName = "";
			} else {
				if(domain.getType().equals(Domain.Type.SLAVE)){
					this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.SlaveZoneAdded", domain.getName());
				} else {
					this.parentDomains.add(domain);
					this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ZoneAdded", domain.getName());
				}
			}
		} else {
			this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ZoneRegistered", existingDomain.getName());
		}
		domain = (Domain) Component.getInstance(Domain.class, ScopeType.STATELESS);
	}

	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#viewSlave()
	 */
	@Override
	public String viewSlave() {
		return "/slavezone.xhtml";
	}


	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#convertToMaster(com.nicmus.pdns.entities.Domain)
	 */
	@Override
	public String convertToMaster(Domain domain) {
		this.zoneDAO.convertToMaster(domain.getName());
		domain.setType(Type.MASTER);
		this.entityManager.flush();
		this.parentDomains.add(domain);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ConvertedToMaster", domain.getName());
		return "success";
	}


	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#updateMaster()
	 */
	@Override
	public String updateMaster() {
		this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.MasterIPChanged", this.selectedDomain.getName());
		this.entityManager.flush();
		return "success";
	}

	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#getSelectedDomains()
	 */
	@Override
	public List<Domain> getSelectedDomains(){
		List<Domain> selectedDomains = new ArrayList<Domain>();
		for(Domain domain : this.selectedDomains.keySet()){
			if(this.selectedDomains.get(domain)){
				selectedDomains.add(domain);
			}
		}
		return selectedDomains;
	}

	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#unlink(com.nicmus.pdns.entities.Domain, com.nicmus.pdns.entities.Domain)
	 */
	@Override
	public void unlink(Domain parent, Domain child){
		parent.getChildren().remove(child);
		child.setParent(null);
		this.parentDomains.add(child);
		this.entityManager.persist(parent);
		this.entityManager.persist(child);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.Unlinked", parent,child);
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#deleteZones()
	 */
	@Override
	public void deleteZones() {
		Set<Domain> domainsToDelete = this.selectedDomains.keySet();
		Iterator<Domain> iterator = domainsToDelete.iterator();
		while(iterator.hasNext()){
			Domain toDelete = iterator.next();
			if(!this.zones.contains(toDelete)){
				continue;
			}
			if(this.selectedDomains.get(toDelete)){
				Set<Domain> children = toDelete.getChildren();
				//delete any children
				if(children.size() > 0){
					Iterator<Domain> childIterator = children.iterator();
					while(childIterator.hasNext()){
						Domain child = childIterator.next();
						this.zones.remove(child);
						childIterator.remove();
					}
				}

				this.zones.remove(toDelete);
				this.parentDomains.remove(toDelete);
				this.entityManager.remove(toDelete);
				iterator.remove();
			}
		}
	}


	/*
	 * (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#getSuggestedDomains(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Factory("parentDomains")
	public List<Domain> getParentDomains(){
		String sql = "from Domain d where d.user.id = :userId and d.parent is null";
		Query parentDomainQuery = this.entityManager.createQuery(sql);
		parentDomainQuery.setParameter("userId", this.userId);
		List<Domain> resultList = parentDomainQuery.getResultList();
		return resultList;
	}
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.ZoneAction#destroy()
	 */
	@Override
	@Remove
	@Destroy
	public void destroy(){
		this.logger.debug("Destroying {0}", ZoneActionImpl.class.getName());
	}
	
}

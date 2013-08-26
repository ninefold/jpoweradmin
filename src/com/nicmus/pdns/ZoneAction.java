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
package com.nicmus.pdns;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.dao.ZoneDAO;
import com.nicmus.pdns.dataModel.ZoneDataModel;
import com.nicmus.pdns.entities.Domain;
import com.nicmus.pdns.entities.User;
import com.nicmus.pdns.entities.Domain.Type;


/**
 * Manage the zones 
 * @author jsabev
 *
 */
@Name("zoneAction")
@Scope(ScopeType.CONVERSATION)
public class ZoneAction implements Serializable {
	private static final long serialVersionUID = 212303795943098723L;

	@Logger
	private Log logger;
	
	@In
	private FacesMessages facesMessages;
	
	@Out(required=false)
	@In(create=true)
	private ZoneDataModel zoneDataModel;
	
	@In(required=true)
	private int userId;
	
	@In(create=true)
	private DNSValidator dnsValidator;
	
	@In(create=true)
	private ZoneDAO zoneDAO;
	
	/**
	 * redirect to the zones listing if logged in
	 * @return
	 */
	public String viewZones(){
		return "/zones.xhtml";
	}
	
	/**
	 * Add the given domain to the list of zones for the current user
	 * @param domain the zone to add
	 */
	public void createZone(Domain domain){
		if(!this.dnsValidator.isValidFQDN(domain.getName())){
			return;
		}
		//verify that the zone does not exist
		Domain existingDomain = this.zoneDAO.getDomain(domain.getName());
		if(existingDomain == null){
			this.zoneDAO.createDomain(domain, this.userId);
			if(domain.getType().equals(Domain.Type.SLAVE)){
				this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.SlaveZoneAdded", domain.getName());
			} else {
				this.zoneDAO.createSOA(domain.getName());
				this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ZoneAdded", domain.getName());
			}
			this.zoneDataModel.update();
		} else {
			this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ZoneRegistered", existingDomain.getName());
		}
		domain = (Domain) Component.getInstance("newDomain", ScopeType.STATELESS);
	}

	/**
	 * Delete the given domain
	 * @param domain the domain to delete
	 */
	public void delete(Domain domain){
		this.zoneDataModel.delete(domain);
		this.zoneDataModel.update();
		this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ZoneDeleted", domain.getName());
	}
	
	/**
	 * Delete the selected zones
	 */
	public void deleteZones(){
		Map<Domain, Boolean> selectedZones = this.zoneDataModel.getSelectedZones();
		Set<Domain> domains = selectedZones.keySet();
		Iterator<Domain> domainIterator = domains.iterator();
		while(domainIterator.hasNext()){
			Domain toDelete = domainIterator.next();
			if(selectedZones.get(toDelete)){
				domainIterator.remove();
				this.delete(toDelete);
			}
		}
	}
	
	/**
	 * View the zone records for the given domain
	 * @param domain
	 * @return the zonename
	 */
	@Begin(nested=true)
	public String viewRecords(Domain domain){
		Contexts.getConversationContext().set("domain", domain);
		return "/records.xhtml";
	}

	/**
	 * View the slave zone
	 * @param domain
	 * @return
	 */
	@Begin(nested=true)
	public String viewSlave(Domain domain){
		Contexts.getConversationContext().set("selectedDomain", domain);
		return "/slavezone.xhtml";
	}
	

	/**
	 * Update the master ip address field of the domain
	 * @param domain the slave domain to update
	 * @return 
	 */
	@End
	public String updateMaster(Domain domain){
		this.zoneDataModel.update(domain);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.MasterIPChanged", domain.getName());
		return "/zones.xhtml";
	}
	
	/**
	 * Convert the given slave domain to master
	 * @param domain
	 * @return
	 */
	@End
	public String convertToMaster(Domain domain){
		this.zoneDAO.convertToMaster(domain.getName());
		domain.setType(Type.MASTER);
		this.zoneDataModel.update(domain);
		this.facesMessages.addFromResourceBundle(Severity.INFO, "ZoneAction.ConvertedToMaster", domain.getName());
		return "/zones.xhtml";
	}
}

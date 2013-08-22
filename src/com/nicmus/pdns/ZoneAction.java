package com.nicmus.pdns;

import java.util.List;

import com.nicmus.pdns.entities.Domain;

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
public interface ZoneAction {

	/**
	 * 
	 */
	public void initZones();

	/**
	 * 
	 * @param domain
	 * @param parentDomainName TODO
	 */
	public void createZone(Domain domain, String parentDomainName);

	/**
	 * 
	 * @return
	 */
	public String viewSlave();

	/**
	 * 
	 * @param domain
	 * @return
	 */
	public String convertToMaster(Domain domain);

	/**
	 * 
	 * @return
	 */
	public String updateMaster();

	/**
	 * Get the domains that have been checked for deletion
	 * @return
	 */
	public List<Domain> getSelectedDomains();

	/**
	 * 
	 */
	public void deleteZones();

	/**
	 * Unlink child from parent
	 * @param parent
	 * @param child
	 */
	public abstract void unlink(Domain parent, Domain child);

	
	/**
	 * Get a list of all parent domains for the current user 
	 * that is domains that have no ochildren themselves and have other 
	 * domains symlinked to them.
	 * @return
	 */
	public abstract List<Domain> getParentDomains();
	
	/**
	 * 
	 */
	public void destroy();

	


}
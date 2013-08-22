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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates the properties of a link/unlink request to link a zone to a 
 * parent zone. 
 * @author jsabev
 *
 */
@XmlRootElement
public class LinkRequest {
	public enum Action{LINK,UNLINK}
	private String parent;
	private String child;
	private Action action;
	
	/**
	 * @return the parent
	 */
	public String getParent() {
		return this.parent;
	}
	
	/**
	 * @param parent the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}
	
	/**
	 * @return the child
	 */
	public String getChild() {
		return this.child;
	}
	
	/**
	 * @param child the child to set
	 */
	public void setChild(String child) {
		this.child = child;
	}
	
	/**
	 * @return the action
	 */
	public Action getAction() {
		return this.action;
	}
	
	/**
	 * @param action the action to set
	 */
	public void setAction(Action action) {
		this.action = action;
	}
	
	
	
}

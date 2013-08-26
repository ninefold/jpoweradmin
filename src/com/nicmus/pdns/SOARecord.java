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


import org.hibernate.validator.Min;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.Name;


/**
 * Encapsulate the properties of an SOA record. See the documentation of 
 * PowerDNS for more information: 
 * http://downloads.powerdns.com/documentation/html/types.html#AEN5081
 * To see the defaults, consult components.xml
 * @author jsabev
 *
 */
@Name("soaRecord")
public class SOARecord {
	private String primary;
	private String hostmaster;
	private long serial;
	private int refresh;
	private int retry;
	private int expire;
	private int defaultTTL;
	
	/**
	 *  
	 */
	public SOARecord() {}

	

	/**
	 * @return the primary
	 */
	@NotNull
	@NotEmpty
	public String getPrimary() {
		return this.primary;
	}
	
	/**
	 * @param primary the primary to set
	 */
	public void setPrimary(String primary) {
		this.primary = primary;
	}
	
	/**
	 * @return the hostmaster
	 */
	@NotNull
	@NotEmpty
	public String getHostmaster() {
		return this.hostmaster;
	}
	
	/**
	 * @param hostmaster the hostmaster to set
	 */
	public void setHostmaster(String hostmaster) {
		this.hostmaster = hostmaster;
	}
	
	/**
	 * @return the serial
	 */
	@NotNull
	@Min(1)
	public long getSerial() {
		return this.serial;
	}
	
	/**
	 * @param serial the serial to set
	 */
	public void setSerial(long serial) {
		this.serial = serial;
	}
	/**
	 * @return the refresh
	 */
	@NotNull
	@Min(1)
	public int getRefresh() {
		return this.refresh;
	}
	
	/**
	 * @param refresh the refresh to set
	 */
	public void setRefresh(int refresh) {
		this.refresh = refresh;
	}
	
	/**
	 * @return the retry
	 */
	@NotNull
	@Min(1)
	public int getRetry() {
		return this.retry;
	}
	
	/**
	 * @param retry the retry to set
	 */
	public void setRetry(int retry) {
		this.retry = retry;
	}
	
	/**
	 * @return the expire
	 */
	@NotNull
	@Min(1)
	public int getExpire() {
		return this.expire;
	}
	
	/**
	 * @param expire the expire to set
	 */
	public void setExpire(int expire) {
		this.expire = expire;
	}
	
	/**
	 * @return the defaultTTL
	 */
	@NotNull
	@Min(1)
	public int getDefaultTTL() {
		return this.defaultTTL;
	}
	
	/**
	 * @param defaultTTL the defaultTTL to set
	 */
	public void setDefaultTTL(int defaultTTL) {
		this.defaultTTL = defaultTTL;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.primary + " " + 
		this.hostmaster + " " + 
		this.serial + " " + 
		this.refresh + " " +
		this.retry + " " + 
		this.expire + " " + 
		this.defaultTTL;
	}
	
	
	
}

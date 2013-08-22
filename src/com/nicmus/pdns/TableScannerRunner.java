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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;

/**
 * Start services on application start up
 * @author jsabev
 *
 */
@Name("tableScannerRunner")
@Scope(ScopeType.APPLICATION)
@Startup
public class TableScannerRunner {
	private long passwordTokenScanIntervalTime;

	@In(create=true)
	private PasswordTokenScanner passwordTokenScanner;
	
	/**
	 * 
	 */
	@Create
	public void start(){
		this.passwordTokenScanner.deleteExpiredTokens(this.passwordTokenScanIntervalTime);
	}
	
	/**
	 * @param passwordTokenScanIntervalTime the passwordTokenScanIntervalTime to set
	 */
	public void setPasswordTokenScanIntervalTime(long passwordTokenScanIntervalTime) {
		this.passwordTokenScanIntervalTime = passwordTokenScanIntervalTime;
	}
	
	
}

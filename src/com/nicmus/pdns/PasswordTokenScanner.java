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

import javax.ejb.Local;

import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalDuration;

/**
 * Scan the password tokens and delete expired tokens. Expired password tokens
 * are tokens that are more than 24 hours old
 * @author jsabev
 *
 */
@Local
public interface PasswordTokenScanner {
	
	@Asynchronous
	public void deleteExpiredTokens(@IntervalDuration long scanInterval);
}

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

import javax.ejb.Stateless;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.async.Duration;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.log.Log;

import com.nicmus.pdns.entities.User;

/**
 * Asynchronous email service
 * @author jsabev
 *
 */
@Name("emailService")
@Stateless
public class EmailServiceImpl implements EmailService {
	
	@Logger
	private Log logger;
	
	@In(create=true)
	private Renderer renderer;
	
	
	/* (non-Javadoc)
	 * @see com.nicmus.pdns.EmailService#sendMessage(long, java.lang.String, com.nicmus.pdns.User)
	 */
	public void sendMessage(@Duration long delay, String message, User user){
		Contexts.getEventContext().set("user", user);
		try {
			this.renderer.render(message);
		} catch (Exception e){
			this.logger.error("Error #0 sending message", e);
		}
			
	}
}

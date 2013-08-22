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


import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.BijectionInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.log.Log;

/**
 * @author jsabev
 *
 */
@Name("executionInterceptor")
@Interceptor(stateless=false,around={BijectionInterceptor.class})
public class ExecutionTimeInterceptor {
	@Logger
	private Log logger;
	
	@AroundInvoke
	public void getMethodExecutionTime(InvocationContext ic){
		String methodname = ic.getMethod().getName();
		String clazz = ic.getClass().getName();
		this.logger.info("Calling {0} method in {1}", methodname,clazz);
		long start = System.currentTimeMillis();
		try {
			ic.proceed();
		} catch (Exception e){
			e.printStackTrace();
		}
		long end = System.currentTimeMillis();
		this.logger.info("Execution time: {0}", (end - start) + "ms");
	}
	
}

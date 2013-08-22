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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;

import com.sun.facelets.FaceletViewHandler;


public class LocalisedViewHandler extends FaceletViewHandler {

	/**
	 * 
	 * @param parent
	 */
	public LocalisedViewHandler(ViewHandler parent) {
		super(parent);
	}

	/* (non-Javadoc)
	 * @see com.sun.facelets.FaceletViewHandler#createView(javax.faces.context.FacesContext, java.lang.String)
	 */
	@Override
	public UIViewRoot createView(FacesContext context, String viewId) {
		String newView = this.getViewId(context, viewId);
		return super.createView(context, newView);
	}

	/* (non-Javadoc)
	 * @see com.sun.facelets.FaceletViewHandler#restoreView(javax.faces.context.FacesContext, java.lang.String)
	 */
	@Override
	public UIViewRoot restoreView(FacesContext context, String viewId) {
		String newview = this.getViewId(context, viewId);
		return super.restoreView(context, newview);
	}
	

	/**
	 * Get the view id of the localised page (if any). If there is no localised 
	 * page for the given view id, return the original view id. Localised pages
	 * are of the form page_<language_code>.xhtml 
	 * @param context
	 * @param viewId
	 * @return
	 */
	private String getViewId(FacesContext context, String viewId){
		String newView;
		
		LocaleSelector localeSelector = (LocaleSelector)Contexts.getSessionContext().get("org.jboss.seam.international.localeSelector");
		
		//default application language is first equal to the server language
		String requestLanguage = context.getApplication().getDefaultLocale().getLanguage();
		
		HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
		Locale requestLocale = request.getLocale();
		
		//the request language overrides the server language
		if(requestLocale != null){
			requestLanguage = requestLocale.getLanguage();
		}
		
		//the user configured application locale overrides the server/request locale
		if(localeSelector != null){
			requestLanguage = localeSelector.getLanguage();
		}
		
		String defaultSuffix = super.getDefaultSuffix(context);
		
		//kill the suffix
		int lastIndex = viewId.lastIndexOf(".");
		if(lastIndex == -1){
			return viewId;
		}
		
		String subview = viewId.substring(0,lastIndex);
		
		newView = subview.concat(".").concat(requestLanguage).concat(defaultSuffix);
		
		//check to see if the new view exists
		try {
			URL resource = context.getExternalContext().getResource(newView);
			if(resource != null){
				return newView;
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		return viewId;
		
	}

}

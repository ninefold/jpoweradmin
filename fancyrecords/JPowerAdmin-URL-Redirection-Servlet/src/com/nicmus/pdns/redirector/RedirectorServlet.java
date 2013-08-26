package com.nicmus.pdns.redirector;

import java.io.IOException;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.nicmus.pdns.fancyrecords.URLSearcher;;

public class RedirectorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	 private URLSearcher urlSearcher;
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String urlToRedirectTo = this.urlSearcher.getURLToRedirectTo(request.getServerName());
		if(urlToRedirectTo != null){
			response.setContentType("text/plain");
			response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			response.setHeader("Location", urlToRedirectTo);
			response.setHeader("connection", "close");
		}
	}

	/**
	 * @param urlSearcher the urlSearcher to set
	 */
	@EJB(mappedName="JPowerAdmin/UrlSearcher/local")
	public void setUrlSearcher(URLSearcher urlSearcher) {
		this.urlSearcher = urlSearcher;
	}

	
	
}
